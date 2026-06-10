# fraud_model.py
import xgboost as xgb
import lightgbm as lgb
from sklearn.ensemble import VotingClassifier
from sklearn.model_selection import StratifiedKFold, cross_val_score
import joblib
import os
import numpy as np

MODEL_DIR = 'models/saved_models/'

def build_xgboost(scale_pos_weight=10):
    return xgb.XGBClassifier(
        n_estimators=300,
        max_depth=6,
        learning_rate=0.05,
        subsample=0.8,
        colsample_bytree=0.8,
        scale_pos_weight=scale_pos_weight,
        use_label_encoder=False,
        eval_metric='aucpr',
        random_state=42,
        n_jobs=-1
    )

def build_lightgbm(class_weight='balanced'):
    return lgb.LGBMClassifier(
        n_estimators=300,
        max_depth=6,
        learning_rate=0.05,
        num_leaves=63,
        subsample=0.8,
        colsample_bytree=0.8,
        class_weight=class_weight,
        random_state=42,
        n_jobs=-1,
        verbose=-1
    )

def build_ensemble():
    xgb_model = build_xgboost()
    lgb_model = build_lightgbm()
    ensemble = VotingClassifier(
        estimators=[('xgb', xgb_model), ('lgb', lgb_model)],
        voting='soft',
        weights=[1, 1]
    )
    return ensemble

def save_model(model, feature_cols, name='fraud_ensemble'):
    os.makedirs(MODEL_DIR, exist_ok=True)
    path = os.path.join(MODEL_DIR, f'{name}.pkl')
    # Save both model and feature column list to prevent schema mismatch
    joblib.dump({
        'model': model,
        'feature_cols': feature_cols
    }, path)
    print(f"Model and features saved to {path}")
    return path

def load_model(name='fraud_ensemble'):
    path = os.path.join(MODEL_DIR, f'{name}.pkl')
    if not os.path.exists(path):
        raise FileNotFoundError(f"No saved model at {path}. Train first.")
    
    data = joblib.load(path)
    if not isinstance(data, dict) or 'model' not in data:
        raise ValueError(
            "Model file is in old format. Delete it and retrain with: python models/train_model.py"
        )
    return data['model']

def get_risk_score(model, features) -> dict:
    proba = model.predict_proba(features)[0][1]
    if proba >= 0.75:
        risk_level = "HIGH"
        recommendation = "BLOCK"
    elif proba >= 0.5:
        risk_level = "MEDIUM"
        recommendation = "REVIEW"
    elif proba >= 0.25:
        risk_level = "LOW"
        recommendation = "ALLOW_WITH_OTP"
    else:
        risk_level = "SAFE"
        recommendation = "ALLOW"

    return {
        'fraud_probability': round(float(proba), 4),
        'risk_score': round(float(proba) * 100, 2),
        'risk_level': risk_level,
        'recommendation': recommendation,
        'is_fraud': bool(proba >= 0.5)
    }
