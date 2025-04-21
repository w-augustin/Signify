# References
# https://github.com/nicknochnack/ActionDetectionforSignLanguage
# https://github.com/Mirwe/Real-time-ASL-alphabet-recognition/tree/main

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

model = tf.keras.models.load_model("sign_language_model2.keras")

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
    keypoints = np.concatenate([pose, face, lh, rh])
    return keypoints.astype(np.float32)  # Ensures the correct data type


def prob_viz(res, actions, input_frame):
    # Find the index of the highest probability
    max_prob_index = np.argmax(res)
    max_prob_value = res[max_prob_index]
    sign = actions[max_prob_index]

    # Convert probability to 2 decimal places
    prob_value = f"{max_prob_value:.2f}"

    # Return the sign and its probability value
    return sign, prob_value


def get_sign_recognition (video_data):
    # 1. New detection variables
    sequence = []
    sentence = []
    predictions = []
    threshold = 0.5
    predicted_action = None
    prob_value = None
    # Actions that we try to detect
    actions = np.array(['hello', 'thank you', 'book', 'name', 'goodbye'])


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
            sequence = sequence[-30:]  # Keep only the latest 30 frames

            if len(sequence) == 30:
                res = model.predict(np.expand_dims(sequence, axis=0))[0]

                # Display the predicted action
                predicted_action, prob_value = prob_viz(res, actions, frame)

                # Output the result to the console
                print(f"Predicted sign: {predicted_action}, Probability: {prob_value}")
                break

    cap.release()
    cv2.destroyAllWindows
    # Return predicted sign and probability (for function)
    return predicted_action, prob_value

def get_alpha_sign_prediction(video_path):
    import mediapipe as mp
    from AlphabetModel import decode  # assumes decode returns the predicted sign
    model = tf.keras.models.load_model('asl_model.h5')

    mp_hands = mp.solutions.hands
    cap = cv2.VideoCapture(video_path)

    if not cap.isOpened():
        return jsonify({"error": f"Could not open video file {video_path}"}), 400

    with mp_hands.Hands(model_complexity=0, min_detection_confidence=0.5, min_tracking_confidence=0.5) as hands:
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
                    pred = decode(np.argmax(prediction))
                    prob = float(np.max(prediction))

                    return jsonify({'sign': pred, 'probability': f"{prob:.2f}"})
                except Exception as e:
                    print(f"ROI issue: {e}")
                    continue

    return jsonify({"error": "No hand landmarks detected"}), 400



@app.route('/predict', methods=['POST'])
def predict():

    if 'video' not in request.files:
        return jsonify({"error": "No video file provided"}), 400

    video_file = request.files['video']

    print(model.input_shape)

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
        result = get_alpha_sign_prediction(temp_video_path)  # Returns jsonify
        print(f"Sending response: {result}")
        return result
    else:
        resultSign, resultProb = get_sign_recognition(temp_video_path)
        print(f"Sending response: {resultSign} {resultProb}")
        return jsonify({'sign': resultSign, 'probability': resultProb})

if __name__ == "__main__":
    app.run(debug=True, host='0.0.0.0', port=5000)
