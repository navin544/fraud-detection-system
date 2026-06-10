# UPI Fraud Detection System

This project consists of a Python ML backend and an Android application.

## Backend (Python/Flask)
- **Framework**: Flask
- **ML Models**: XGBoost + LightGBM Ensemble
- **Features**: Time-based, Amount-based, Velocity, Device/Location anomalies
- **Data Balancing**: SMOTE (Synthetic Minority Over-sampling Technique)

### Setup
1. Navigate to `backend/`
2. Install dependencies: `pip install -r requirements.txt`
3. Train the model: `python models/train_model.py`
4. Run the API: `python app.py`

## Android App (Java)
- **Framework**: Android SDK (Java)
- **Networking**: Retrofit + OkHttp
- **Architecture**: Model-View-Controller (MVC)

### Setup
1. Open the `android/` folder in Android Studio.
2. Ensure you have the Android SDK and a suitable emulator or physical device.
3. Update `RetrofitClient.java` with your backend URL (default is `http://10.0.2.2:5000/` for emulator).
4. Build and run the app.

## File Structure
- `backend/`: Python source code, models, and utility scripts.
- `android/`: Android Studio project files, Java source code, and layout XMLs.
