import requests
import json
import os
from dotenv import load_dotenv

load_dotenv()

BASE_URL = os.environ.get("API_URL", "http://localhost:5000")
API_KEY = os.environ.get("API_KEY", "your_api_key_here")

def test_predict():
    url = f"{BASE_URL}/api/v1/predict"
    headers = {
        "Content-Type": "application/json",
        "X-API-Key": API_KEY
    }
    payload = {
        "amount": 95000,
        "sender_id": "test_user_123",
        "is_new_beneficiary": 1,
        "is_night": 1,
        "device_changed": 1,
        "location_anomaly": 1
    }
    
    try:
        response = requests.post(url, json=payload, headers=headers)
        print(f"Status: {response.status_code}")
        print(json.dumps(response.json(), indent=2))
    except Exception as e:
        print(f"Request failed: {e}")

if __name__ == "__main__":
    test_predict()
