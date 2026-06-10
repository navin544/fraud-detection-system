# routes.py
from flask import Blueprint, request, jsonify
try:
    from models.fraud_model import load_model, get_risk_score
    from models.feature_engineering import single_transaction_features
except ImportError:
    import sys
    import os
    sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
    from models.fraud_model import load_model, get_risk_score
    from models.feature_engineering import single_transaction_features

from datetime import datetime
import json, os

api_bp = Blueprint('api', __name__)

# Load model at startup
model = None
threshold = 0.5

def init_model():
    global model, threshold
    try:
        model = load_model('fraud_ensemble')
        thresh_file = 'models/saved_models/threshold.txt'
        if os.path.exists(thresh_file):
            with open(thresh_file) as f:
                threshold = float(f.read().strip())
        print(f"Model loaded. Threshold: {threshold}")
    except Exception as e:
        print(f"Error loading model: {e}")


@api_bp.route('/health', methods=['GET'])
def health():
    return jsonify({'status': 'ok', 'timestamp': datetime.now().isoformat()})


@api_bp.route('/predict', methods=['POST'])
def predict():
    try:
        data = request.get_json(force=True)
        if not data:
            return jsonify({'error': 'No JSON body'}), 400

        required = ['amount', 'sender_id']
        for field in required:
            if field not in data:
                return jsonify({'error': f'Missing field: {field}'}), 400

        features = single_transaction_features(data)
        result = get_risk_score(model, features)

        response = {
            'transaction_id': data.get('transaction_id', 'TXN_UNKNOWN'),
            'timestamp': datetime.now().isoformat(),
            **result
        }
        return jsonify(response), 200

    except Exception as e:
        return jsonify({'error': str(e)}), 500


@api_bp.route('/batch_predict', methods=['POST'])
def batch_predict():
    try:
        data = request.get_json(force=True)
        transactions = data.get('transactions', [])
        results = []
        for txn in transactions:
            features = single_transaction_features(txn)
            result = get_risk_score(model, features)
            results.append({
                'transaction_id': txn.get('transaction_id', ''),
                **result
            })
        return jsonify({'results': results, 'count': len(results)}), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500


@api_bp.route('/metrics', methods=['GET'])
def metrics():
    try:
        with open('models/saved_models/metrics.json') as f:
            m = json.load(f)
        return jsonify(m), 200
    except:
        return jsonify({'error': 'No metrics found. Train model first.'}), 404
