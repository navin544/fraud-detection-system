# smote_handler.py
from imblearn.over_sampling import SMOTE, BorderlineSMOTE
from imblearn.combine import SMOTETomek
from collections import Counter
import numpy as np

def apply_smote(X_train, y_train, strategy='smote'):
    print(f"Before SMOTE: {Counter(y_train)}")

    if strategy == 'smote':
        sampler = SMOTE(random_state=42, k_neighbors=5)
    elif strategy == 'borderline':
        sampler = BorderlineSMOTE(random_state=42, kind='borderline-1')
    elif strategy == 'smotetomek':
        sampler = SMOTETomek(random_state=42)
    else:
        return X_train, y_train

    X_resampled, y_resampled = sampler.fit_resample(X_train, y_train)
    print(f"After  SMOTE: {Counter(y_resampled)}")
    return X_resampled, y_resampled
