package com.frauddetector.upi.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.frauddetector.upi.model.TransactionEntity;
import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    void insert(TransactionEntity transaction);

    @Query("SELECT * FROM transactions WHERE senderId = :senderId AND timestamp >= :since")
    List<TransactionEntity> getRecentTransactions(String senderId, long since);

    @Query("SELECT COUNT(*) FROM transactions WHERE senderId = :senderId AND timestamp >= :since")
    int getCountRecent(String senderId, long since);

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE senderId = :senderId AND timestamp >= :since")
    double getSumRecent(String senderId, long since);
}
