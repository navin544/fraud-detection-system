# feature_engineering.py
import pandas as pd
import numpy as np
from datetime import datetime

def engineer_features(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()

    # Time-based features
    df['hour'] = pd.to_datetime(df['timestamp']).dt.hour
    df['day_of_week'] = pd.to_datetime(df['timestamp']).dt.dayofweek
    df['is_weekend'] = df['day_of_week'].isin([5, 6]).astype(int)
    if 'is_night' not in df.columns:
        df['is_night'] = ((df['hour'] >= 22) | (df['hour'] <= 5)).astype(int)

    # Amount-based features
    df['amount_log'] = np.log1p(df['amount'])
    df['is_round_amount'] = (df['amount'] % 100 == 0).astype(int)
    df['is_high_value'] = (df['amount'] > 50000).astype(int)

    # Velocity features (transactions per user in last N hours)
    df['txn_count_1h'] = df.groupby('sender_id')['amount'].transform('count')
    df['txn_sum_1h'] = df.groupby('sender_id')['amount'].transform('sum')
    df['avg_txn_amount'] = df.groupby('sender_id')['amount'].transform('mean')

    # Beneficiary features
    df['new_beneficiary'] = df.get('is_new_beneficiary', 0)
    df['international_txn'] = df.get('is_international', 0)

    # Device / location anomaly
    df['device_change'] = df.get('device_changed', 0)
    df['location_anomaly'] = df.get('location_anomaly', 0)

    # Drop non-numeric columns for model
    feature_cols = [
        'amount', 'hour', 'day_of_week',
        'is_weekend', 'is_night', 'is_round_amount', 'is_high_value',
        'txn_count_1h', 'txn_sum_1h', 'avg_txn_amount',
        'new_beneficiary', 'international_txn',
        'device_change', 'location_anomaly', 'amount_log'
    ]
    return df[feature_cols]


def single_transaction_features(txn: dict) -> pd.DataFrame:
    """Convert a single transaction dict to feature DataFrame."""
    row = {
        'timestamp': txn.get('timestamp', datetime.now().isoformat()),
        'amount': float(txn.get('amount', 0)),
        'sender_id': txn.get('sender_id', 'unknown'),
        'is_new_beneficiary': int(txn.get('is_new_beneficiary', 0)),
        'is_international': int(txn.get('is_international', 0)),
        'device_changed': int(txn.get('device_changed', 0)),
        'location_anomaly': int(txn.get('location_anomaly', 0)),
    }
    if 'is_night' in txn:
        row['is_night'] = int(txn['is_night'])

    df = pd.DataFrame([row])
    return engineer_features(df)
