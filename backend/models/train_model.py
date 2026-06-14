# train_model.py
import pandas as pd
import numpy as np
from datetime import datetime
from sklearn.model_selection import train_test_split
try:
    from models.fraud_model import build_ensemble, save_model
    from models.feature_engineering import engineer_features
except ImportError:
    from fraud_model import build_ensemble, save_model
    from feature_engineering import engineer_features

import sys
import os

# Add the parent directory to sys.path to allow imports when running from models/
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from utils.smote_handler import apply_smote
from utils.metrics import evaluate_model, find_optimal_threshold
import json

def generate_synthetic_data(n_samples=50000, fraud_rate=0.02):
    """Generate synthetic UPI transaction data for training."""
    np.random.seed(42)
    n_fraud = int(n_samples * fraud_rate)
    n_legit = n_samples - n_fraud

    def get_base_data(n, is_fraud):
        if not is_fraud:
            # Legit: Mostly small/medium, rarely at night, rarely new beneficiaries
            data = {
                'amount': np.random.lognormal(8, 1.5, n), # Increased variance
                'timestamp': [datetime.now().isoformat() for _ in range(n)],
                'sender_id': [f'user_{i}' for i in np.random.randint(0, 5000, n)], # More users
                'is_new_beneficiary': np.random.binomial(1, 0.2, n), # More "new" beneficiaries
                'is_international': np.random.binomial(1, 0.02, n),
                'device_changed': np.random.binomial(1, 0.05, n),
                'location_anomaly': np.random.binomial(1, 0.05, n),
                'is_night': np.random.binomial(1, 0.2, n)
            }
        else:
            # Fraud: Significant overlap with legit features.
            # Making 40% of fraud transactions virtually identical to legit ones.
            data = {
                'amount': np.random.lognormal(8.5, 2.0, n), # Very similar to legit
                'timestamp': [datetime.now().isoformat() for _ in range(n)],
                'sender_id': [f'fraud_user_{i}' for i in np.random.randint(0, 100, n)],
                'is_new_beneficiary': np.random.binomial(1, 0.4, n), # Much lower than before
                'is_international': np.random.binomial(1, 0.1, n),
                'device_changed': np.random.binomial(1, 0.3, n),
                'location_anomaly': np.random.binomial(1, 0.3, n),
                'is_night': np.random.binomial(1, 0.3, n)
            }
        # Add random noise to continuous columns
        df = pd.DataFrame(data)
        df['amount'] += np.random.normal(0, 100, n)
        df['amount'] = df['amount'].clip(lower=1)
        return df

    df_legit = get_base_data(n_legit, False)
    df_fraud = get_base_data(n_fraud, True)
    
    df_legit['label'] = 0
    df_fraud['label'] = 1
    
    df = pd.concat([df_legit, df_fraud])
    
    # Process features using the central engineer_features
    features_df = engineer_features(df)
    features_df['label'] = df['label'].values
    
    return features_df.sample(frac=1, random_state=42).reset_index(drop=True)

def train():
    print("=== UPI Fraud Detection Model Training ===\n")

    # Generate/Load data
    print("[1/6] Generating training data...")
    df = generate_synthetic_data(n_samples=100000, fraud_rate=0.025)
    print(f"Dataset: {len(df)} rows | Fraud rate: {df['label'].mean():.2%}\n")

    feature_cols = [c for c in df.columns if c != 'label']
    X = df[feature_cols].values
    y = df['label'].values

    # Split
    print("[2/6] Splitting dataset...")
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, stratify=y, random_state=42
    )
    X_train, X_val, y_train, y_val = train_test_split(
        X_train, y_train, test_size=0.1, stratify=y_train, random_state=42
    )
    print(f"Train: {len(X_train)} | Val: {len(X_val)} | Test: {len(X_test)}\n")

    # SMOTE
    print("[3/6] Applying SMOTE for class balancing...")
    X_train_res, y_train_res = apply_smote(X_train, y_train, strategy='smotetomek')
    print()

    # Train
    print("[4/6] Training Ensemble (XGBoost + LightGBM)...")
    model = build_ensemble()
    model.fit(X_train_res, y_train_res)
    print("Training complete.\n")

    # Find optimal threshold
    print("[5/6] Finding optimal decision threshold...")
    best_threshold, best_f1 = find_optimal_threshold(model, X_val, y_val)
    print(f"Optimal threshold: {best_threshold:.3f} | Val F1: {best_f1:.4f}\n")

    # Evaluate
    print("[6/6] Evaluating on test set...")
    metrics = evaluate_model(model, X_test, y_test, threshold=best_threshold)
    print(json.dumps({k: v for k, v in metrics.items() if k != 'classification_report'}, indent=2))

    # Save
    save_model(model, feature_cols, 'fraud_ensemble')
    
    # Ensure directory exists
    os.makedirs('models/saved_models', exist_ok=True)
    
    with open('models/saved_models/metrics.json', 'w') as f:
        json.dump({k: v for k, v in metrics.items() if k != 'classification_report'}, f, indent=2)
    with open('models/saved_models/threshold.txt', 'w') as f:
        f.write(str(best_threshold))

    print("\n=== Training Complete! Model saved. ===")

if __name__ == '__main__':
    train()
