# config.py
import os

class Config:
    SECRET_KEY = os.environ.get('SECRET_KEY')
    DEBUG = False
    TESTING = False
    MODEL_PATH = 'models/saved_models/'
    DB_URI = 'sqlite:///transactions.db'
    FRAUD_THRESHOLD = 0.5
    HIGH_RISK_THRESHOLD = 0.75

class DevelopmentConfig(Config):
    DEBUG = True

class ProductionConfig(Config):
    DEBUG = False
