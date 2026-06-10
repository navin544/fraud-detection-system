import requests
import json

url = "http://192.168.192.193:5000/api/v1/batch_predict"
transactions = []

for i in range(1, 15):
    transactions.append({
        "transaction_id": f"TXN_LIVE_{i}",
        "amount": 10000 + (i * 5000),
        "sender_id": "live_test_user_99",
        "is_new_beneficiary": 1,
        "is_night": 1,
        "device_changed": 1,
        "location_anomaly": 1
    })

payload = {"transactions": transactions}
response = requests.post(url, json=payload)
print(json.dumps(response.json(), indent=2))
