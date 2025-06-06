#TODO: call this from where i need to use the sign rec model
import cv2
import os
import numpy as np
import mediapipe as mp
import io
import tensorflow as tf
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

app = Flask(__name__)

# Initialize mediapipe holistic model
mp_holistic = mp.solutions.holistic

model = tf.keras.models.load_model("sign_language_model_3.keras")

def mediapipe_detection(image, model):
    image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB) # COLOR CONVERSION BGR to RGB
    image.flags.writeable = False                  # Image is not writeable
    results = model.process(image)                 # Make prediction
    image.flags.writeable = True                   # Image is writeable
    image = cv2.cvtColor(image, cv2.COLOR_RGB2BGR) # COLOR CONVERSION RGB to BGR
    return image, results

def draw_styled_landmarks(image, results):
    mp_drawing = mp.solutions.drawing_utils

    # Draw face connections
    mp_drawing.draw_landmarks(image, results.face_landmarks,   mp_holistic.FACEMESH_CONTOURS,
                              mp_drawing.DrawingSpec(color=(80,110,10), thickness=1, circle_radius=1),
                              mp_drawing.DrawingSpec(color=(80,256,121), thickness=1, circle_radius=1)
                              )
    # Draw pose connections
    mp_drawing.draw_landmarks(image, results.pose_landmarks, mp_holistic.POSE_CONNECTIONS,
                              mp_drawing.DrawingSpec(color=(80,22,10), thickness=2, circle_radius=4),
                              mp_drawing.DrawingSpec(color=(80,44,121), thickness=2, circle_radius=2)
                              )
    # Draw left hand connections
    mp_drawing.draw_landmarks(image, results.left_hand_landmarks, mp_holistic.HAND_CONNECTIONS,
                              mp_drawing.DrawingSpec(color=(121,22,76), thickness=2, circle_radius=4),
                              mp_drawing.DrawingSpec(color=(121,44,250), thickness=2, circle_radius=2)
                              )
    # Draw right hand connections
    mp_drawing.draw_landmarks(image, results.right_hand_landmarks, mp_holistic.HAND_CONNECTIONS,
                              mp_drawing.DrawingSpec(color=(245,117,66), thickness=2, circle_radius=4),
                              mp_drawing.DrawingSpec(color=(245,66,230), thickness=2, circle_radius=2)
                              )

def extract_keypoints(results):
    pose = np.array([[res.x, res.y, res.z, res.visibility] for res in results.pose_landmarks.landmark]).flatten() if results.pose_landmarks else np.zeros(33*4)
    face = np.array([[res.x, res.y, res.z] for res in results.face_landmarks.landmark]).flatten() if results.face_landmarks else np.zeros(468*3)
    lh = np.array([[res.x, res.y, res.z] for res in results.left_hand_landmarks.landmark]).flatten() if results.left_hand_landmarks else np.zeros(21*3)
    rh = np.array([[res.x, res.y, res.z] for res in results.right_hand_landmarks.landmark]).flatten() if results.right_hand_landmarks else np.zeros(21*3)
    # Make sure the result is a float32 numpy array
    #keypoints = np.concatenate([pose, face, lh, rh])
    #return keypoints.astype(np.float32)  # Ensures the correct data type
    return np.concatenate([pose, face, lh, rh])

'''
def prob_avg(res, actions, input_frame):
    # Find the index of the highest probability
    max_prob_index = np.argmax(res)
    max_prob_value = res[max_prob_index]
    sign = actions[max_prob_index]

    # Convert probability to 2 decimal places
    prob_value = f"{max_prob_value:.2f}"

    # Return the sign and its probability value
    return sign, prob_value
'''
def prob(res, actions):
    max_index = np.argmax(res)
    predicted_action = actions[max_index]
    probability = float(res[max_index])
    return [(predicted_action, probability)]

def top_three_predictions(res, actions):
    top_indices = np.argsort(res)[-3:][::-1]
    return [(actions[i], float(res[i])) for i in top_indices]

def get_sign_recognition (video_data, action):
    # 1. New detection variables
    sequence = []
    sentence = []
    predictions = []
    threshold = 0.5
    predicted_action = None
    prob_value = None
    # Actions that we try to detect
    actions = np.array(['hello', 'i love you', 'book', 'bye', 'thank you'])

    # Check if the video file exists
    if not os.path.exists(video_data):
        print(f"Error: Video file '{video_data}' not found.")
        exit()

    cap = cv2.VideoCapture(video_data)
    if not cap.isOpened():
        print(f"Error: Failed to open video file at {video_data}")
        exit()

    # Set mediapipe model
    with mp_holistic.Holistic(min_detection_confidence=0.5, min_tracking_confidence=0.5) as holistic:
        while cap.isOpened():

            # Check if video has frames to read
            frame_count = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
            print(f"Video contains {frame_count} frames.")

            # Read feed
            ret, frame = cap.read()
            if not ret:
                break

            # Make detections
            image, results = mediapipe_detection(frame, holistic)

            # Extract keypoints and append to sequence
            keypoints = extract_keypoints(results)
            sequence.append(keypoints)
            sequence = sequence[-30:]

            if len(sequence) == 30:
                res = model.predict(np.expand_dims(sequence, axis=0))[0]
                print(res)

                top2 = np.argsort(res)[-2:][::-1]
                temp = [(actions[i], float(res[i])) for i in top2]

                if temp[0] == action:
                    cap.release()
                    cv2.destroyAllWindows()
                    print(f"Predicted action matches input action: {temp[0]}.")
                    return [(temp[0], top2[0])]

                if temp[1] == action:
                    cap.release()
                    cv2.destroyAllWindows()
                    print(f"Predicted action matches input action: {temp[0]}.")
                    return [(temp[0], top2[0]), (temp[1], top2[1])]
                predictions.append(res)  # Store the prediction even if it doesn't match

    cap.release()
    cv2.destroyAllWindows

    # Compute average probabilities across all predictions
    if predictions:
        avg_probs = np.mean(predictions, axis=0)
        top2_avg = top_three_predictions(avg_probs, actions)
        print(top2_avg)
        return top2_avg

    return [("No sign detected", 0.0)]

def get_alpha_sign_prediction(video_path, action):
    import mediapipe as mp
    from model import decode  # assumes decode returns the predicted sign
    model = tf.keras.models.load_model('asl_model.h5')

    predictions = []

    mp_hands = mp.solutions.hands
    cap = cv2.VideoCapture(video_path)

    if not cap.isOpened():
        return jsonify({"error": f"Could not open video file {video_path}"}), 400

    with mp_hands.Hands(model_complexity=0, min_detection_confidence=0.75, min_tracking_confidence=0.5) as hands:
        while cap.isOpened():
            ret, image = cap.read()
            if not ret:
                break

            image.flags.writeable = False
            image_rgb = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)
            results = hands.process(image_rgb)

            image.flags.writeable = True
            image = cv2.cvtColor(image_rgb, cv2.COLOR_RGB2BGR)

            if results.multi_hand_landmarks:
                xList, yList = [], []
                hand = results.multi_hand_landmarks[0]

                for lm in hand.landmark:
                    h, w, _ = image.shape
                    xList.append(int(lm.x * w))
                    yList.append(int(lm.y * h))

                xmin, xmax = min(xList), max(xList)
                ymin, ymax = min(yList), max(yList)
                roi_x, roi_y = max(0, xmin - 50), max(0, ymin - 50)
                roi_w, roi_h = (xmax - xmin + 100), (ymax - ymin + 100)

                roi = image[roi_y:roi_y + roi_h, roi_x:roi_x + roi_w]
                try:
                    resized = cv2.resize(roi, (128, 128))
                    img_array = np.array([resized])
                    prediction = model.predict(img_array)
                    prob_values = prediction[0]
                    top_index = np.argmax(prob_values)
                    predicted_action = decode(top_index)
                    prob_value = float(prob_values[top_index])

                    # Early return if match
                    if predicted_action == action:
                        cap.release()
                        print(f"Predicted action matches input action: {predicted_action}.")
                        return [(predicted_action, prob_value)]

                    predictions.append(prob_values)

                except Exception as e:
                    print(f"ROI issue: {e}")
                    continue

    cap.release()

    if predictions:
        avg_probs = np.mean(predictions, axis=0)
        top_indices = np.argsort(avg_probs)[-3:][::-1]
        top_preds = [(decode(i), float(avg_probs[i])) for i in top_indices]
        print(top_preds)
        return top_preds

    return [("No sign detected", 0.0)]



@app.route('/predict', methods=['POST'])
def predict():

    if 'video' not in request.files:
        return jsonify({"error": "No video file provided"}), 400

    video_file = request.files['video']

    print(model.input_shape)

    action = request.form['expectedSign']

    # Get the method query parameter (e.g., /predict?method=hands)
    method = request.args.get("method", "holistic")  # default is holistic

    # Save the video file temporarily
    filename = secure_filename(video_file.filename)
    temp_video_path = os.path.join('/tmp', filename)
    video_file.save(temp_video_path)

    cap = cv2.VideoCapture(temp_video_path)
    width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    fps = cap.get(cv2.CAP_PROP_FPS)
    frame_count = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))

    print(f"Width: {width}, Height: {height}, FPS: {fps}, Frames: {frame_count}")
    
    if method == "alpha":
        result = get_alpha_sign_prediction(temp_video_path, action)  # Returns jsonify
    else:
        result = get_sign_recognition(temp_video_path, action)

    result_list = [{'sign': r[0], 'probability': r[1]} for r in result]
    print(f"Sending response: {result_list}")
    return jsonify(result_list)

if __name__ == "__main__":
    app.run(debug=True, host='0.0.0.0', port=5000)
