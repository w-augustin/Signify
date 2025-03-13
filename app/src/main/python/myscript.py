#TODO: call this from where i need to use the sign rec model
import cv2
import numpy as np
import mediapipe as mp
import pandas as pd
pd.options.mode.chained_assignment = None
import os
import tensorflow as tf

# Initialize mediapipe holistic model
mp_holistic = mp.solutions.holistic

# Load the TFLite model
def load_tflite_model(sign_model_path):
    sign_interpreter = tf.lite.Interpreter(model_path=sign_model_path)
    # Get input and output tensors.
    sign_interpreter.allocate_tensors()
    # Print the input and output details of the model
    return sign_interpreter

# Perform inference using the TFLite model
def predict_with_tflite(sign_interpreter, input_data):
    # input and output format
    input_details = sign_interpreter.get_input_details()
    output_details = sign_interpreter.get_output_details()

    # Set the tensor for input data
    sign_interpreter.set_tensor(input_details[0]['index'], input_data)
    sign_interpreter.invoke()

    # Get the output tensor
    output_data = sign_interpreter.get_tensor(output_details[0]['index'])
    return output_data[0]

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

def prob_viz(res, actions):
    # Find the index of the highest probability
    max_prob_index = np.argmax(res)
    max_prob_value = res[max_prob_index]
    sign = actions[max_prob_index]

    # Convert probability to 2 decimal places
    prob_value = f"{max_prob_value:.2f}"

    # Return the sign and its probability value
    return sign, prob_value

def get_sign_recognition (video_filename):
    # 1. New detection variables
    sequence = []
    # predictions = []
    threshold = 0.5
    actions = np.array(['hello', 'thank you', 'book'])

    # Check if the video file exists
    if not os.path.exists(video_filename):
        print("Error: Video file '{video_filename} not found.")
        exit()

    cap = cv2.VideoCapture(video_filename)
    if not cap.isOpened():
        print("Error: Failed to open video file at {video_filename}")
        exit()

    # Set mediapipe model
    with mp_holistic.Holistic(min_detection_confidence=threshold, min_tracking_confidence=threshold) as holistic:
        while cap.isOpened():

            # Check if video has frames to read
            frame_count = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
            print("Video contains", frame_count, "frames.")

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
                res = predict_with_tflite(interpreter, np.expand_dims(sequence, axis=0))

                # Display the predicted action
                predicted_action, prob_value = prob_viz(res, actions)

                # Output the result to the console
                print("Predicted sign: ", predicted_action, " Probability: ", prob_value)

                # Return predicted sign and probability (for function)
                # return predicted_sign, prob_value

            # Break with q
            if cv2.waitKey(10) & 0xFF == ord('q'):
                break
    cap.release()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    # Load the TFLite model
    model_path = 'sign_language_model.tflite'  # Replace with the path to your TFLite model
    interpreter = load_tflite_model(model_path)
    #TODO: replace with name of user recorded video
    test = "test_input_data/hello.mp4"

    get_sign_recognition(test)  # Call the function when the script is executed directly