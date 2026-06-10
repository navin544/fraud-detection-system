# app.py
from flask import Flask
from flask_cors import CORS
from api.routes import api_bp, init_model

import os

def create_app():
    app = Flask(__name__)
    # In production, set ALLOWED_ORIGINS=https://yourdomain.com in .env
    allowed = os.environ.get("ALLOWED_ORIGINS", "*").split(",")
    CORS(app, origins=allowed)

    app.register_blueprint(api_bp, url_prefix='/api/v1')

    with app.app_context():
        init_model()

    return app

if __name__ == '__main__':
    app = create_app()
    app.run(host='0.0.0.0', port=5000, debug=False)
