package com.frauddetector.upi.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class TransactionEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String senderId;
    public double amount;
    public long timestamp;

    public TransactionEntity(String senderId, double amount, long timestamp) {
        this.senderId = senderId;
        this.amount = amount;
        this.timestamp = timestamp;
    }
}
