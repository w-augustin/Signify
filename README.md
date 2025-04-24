# Signify
Signify is an android app designed to solve the problem of finding a free and engaging way to learn American Sign Language (ASL). Whether you're a beginner or an advanced learner, Signify offers learning modules, real-time signing practice with feedback, and progress tracking to help you master the language. no matter which stage you are in learning the language. The app fosters inclusivity and connection with the Deaf and hard-of-hearing community by empowering users to communicate confidently in ASL.
# Deployment Instructions

## What You Need

- [Android Studio](https://developer.android.com/studio)
- A created **device emulator** in Android Studio
  - **Preferred device:** Medium Phone, VanillaIceCream API 35, x86_64 system image, Android 15.0 (Google Play)
  - **Advanced settings:** Front/Back camera set to `webcam0`
- A computer with **Python 3.9+**
- *(Optional but recommended)* Virtual environment tools like `venv` or `conda`

---

## Clone the Project

```bash
git clone https://github.com/w-augustin/Signify.git
```

Or download the ZIP file from the GitHub repository and extract it.

---

## Run Server Code (Python Backend)

1. Open terminal / command prompt:
```bash
cd AndroidStudioProjects/Signify/app/src/main/python/SignRecognitionModel
```

2. *(Optional)* Create and activate a virtual environment:

**macOS / Linux**
```bash
python3 -m venv venv
source venv/bin/activate
```

**Windows**
```bash
python -m venv venv
venv\Scripts\activate
```

3. Install Python dependencies:

```bash
pip install opencv-python numpy matplotlib pandas
```

4. Run the server:

```bash
python server.py
```

---

## Run the App (Android Client)

1. Open the Android Studio project
2. Navigate to:  
   `app/src/main/java/com/example/signifybasic/features/tabs/playground/videorecognition/ModelRetroFitClient.kt`
3. Update the `BASE_URL` to your computer's local IP address using port `5000`.

### Find Local IP Address

- **Windows:**  
  ```bash
  ipconfig
  ```
- **macOS:**  
  ```bash
  ipconfig getifaddr en0
  ```
- **Linux:**  
  ```bash
  ip addr show
  ```

Use the format:
```
http://<IPAddress>:5000
```

4. In Android Studio, click the **Run** icon to launch the app.

---

## You're Done!

Your ASL learning app is now deployed and ready to use.
