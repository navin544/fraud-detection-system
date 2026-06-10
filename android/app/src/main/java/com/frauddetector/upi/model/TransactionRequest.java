// app/src/main/java/com/frauddetector/upi/model/TransactionRequest.java
package com.frauddetector.upi.model;

import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TransactionRequest {
    @SerializedName("transaction_id")   public String transactionId;
    @SerializedName("amount")           public double amount;
    @SerializedName("sender_id")        public String senderId;
    @SerializedName("receiver_id")      public String receiverId;
    @SerializedName("timestamp")        public String timestamp;
    @SerializedName("is_new_beneficiary") public int isNewBeneficiary;
    @SerializedName("is_night")         public int isNight;
    @SerializedName("is_international") public int isInternational;
    @SerializedName("device_changed")   public int deviceChanged;
    @SerializedName("location_anomaly") public int locationAnomaly;
    
    // Velocity features (sent from client-side Room DB tracking)
    @SerializedName("txn_count_1h")     public int txnCount1h;
    @SerializedName("txn_sum_1h")       public double txnSum1h;
    @SerializedName("avg_txn_amount")    public double avgTxnAmount;

    public TransactionRequest(double amount, String senderId) {
        this.amount = amount;
        this.senderId = senderId;
        this.transactionId = "TXN_" + System.currentTimeMillis();
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(new Date());
    }
}
