// app/src/main/java/com/frauddetector/upi/model/TransactionRequest.java
package com.frauddetector.upi.model;

import com.google.gson.annotations.SerializedName;

public class TransactionRequest {
    @SerializedName("transaction_id")   public String transactionId;
    @SerializedName("amount")           public double amount;
    @SerializedName("sender_id")        public String senderId;
    @SerializedName("receiver_id")      public String receiverId;
    @SerializedName("timestamp")        public String timestamp;
    @SerializedName("is_new_beneficiary") public int isNewBeneficiary;
    @SerializedName("is_international") public int isInternational;
    @SerializedName("device_changed")   public int deviceChanged;
    @SerializedName("location_anomaly") public int locationAnomaly;

    public TransactionRequest(double amount, String senderId) {
        this.amount = amount;
        this.senderId = senderId;
        this.transactionId = "TXN_" + System.currentTimeMillis();
        this.timestamp = new java.util.Date().toString();
    }
}
