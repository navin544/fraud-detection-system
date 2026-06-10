// app/src/main/java/com/frauddetector/upi/model/FraudResponse.java
package com.frauddetector.upi.model;

import com.google.gson.annotations.SerializedName;

public class FraudResponse {
    @SerializedName("transaction_id")    public String transactionId;
    @SerializedName("fraud_probability") public double fraudProbability;
    @SerializedName("risk_score")        public double riskScore;
    @SerializedName("risk_level")        public String riskLevel;
    @SerializedName("recommendation")    public String recommendation;
    @SerializedName("is_fraud")          public boolean isFraud;
    @SerializedName("timestamp")         public String timestamp;
}
