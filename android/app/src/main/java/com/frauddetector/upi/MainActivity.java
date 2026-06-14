// app/src/main/java/com/frauddetector/upi/MainActivity.java
package com.frauddetector.upi;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.util.Log;
import com.frauddetector.upi.model.*;
import com.frauddetector.upi.network.RetrofitClient;
import com.frauddetector.upi.db.AppDatabase;
import com.frauddetector.upi.db.TransactionDao;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    EditText etAmount, etSenderId, etReceiverId;
    CheckBox cbNewBeneficiary, cbNight, cbDeviceChange, cbLocation;
    Button btnAnalyze;
    CardView cardResult;
    TextView tvRiskLevel, tvProbability, tvRecommendation, tvRiskScore;
    ProgressBar progressBar;

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // ... (existing bindings)
        etAmount        = findViewById(R.id.etAmount);
        etSenderId      = findViewById(R.id.etSenderId);
        etReceiverId    = findViewById(R.id.etReceiverId);
        cbNewBeneficiary= findViewById(R.id.cbNewBeneficiary);
        cbNight         = findViewById(R.id.cbNightTransaction);
        cbDeviceChange  = findViewById(R.id.cbDeviceChange);
        cbLocation      = findViewById(R.id.cbLocationAnomaly);
        btnAnalyze      = findViewById(R.id.btnAnalyze);
        cardResult      = findViewById(R.id.cardResult);
        tvRiskLevel     = findViewById(R.id.tvRiskLevel);
        tvProbability   = findViewById(R.id.tvProbability);
        tvRecommendation= findViewById(R.id.tvRecommendation);
        tvRiskScore     = findViewById(R.id.tvRiskScore);
        progressBar     = findViewById(R.id.progressBar);

        btnAnalyze.setOnClickListener(v -> analyzeTransaction());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }

    private void analyzeTransaction() {
        String amountStr = etAmount.getText().toString().trim();
        String senderId  = etSenderId.getText().toString().trim();

        if (amountStr.isEmpty() || senderId.isEmpty()) {
            Toast.makeText(this, "Enter amount and sender ID", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        progressBar.setVisibility(View.VISIBLE);
        cardResult.setVisibility(View.GONE);
        btnAnalyze.setEnabled(false);

        long oneHourAgo = System.currentTimeMillis() - (3600 * 1000);
        TransactionDao dao = AppDatabase.getInstance(this).transactionDao();

        // Use RxJava to query DB off main thread
        Disposable d = Observable.fromCallable(() -> {
            int count = dao.getCountRecent(senderId, oneHourAgo);
            double sum = dao.getSumRecent(senderId, oneHourAgo);
            return new double[]{count, sum};
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(stats -> {
            TransactionRequest req = new TransactionRequest(amount, senderId);
            req.receiverId      = etReceiverId.getText().toString().trim();
            req.isNewBeneficiary= cbNewBeneficiary.isChecked() ? 1 : 0;
            req.isNight         = cbNight.isChecked() ? 1 : 0;
            req.deviceChanged   = cbDeviceChange.isChecked() ? 1 : 0;
            req.locationAnomaly = cbLocation.isChecked() ? 1 : 0;
            
            // Populate velocity features from local DB
            req.txnCount1h      = (int) stats[0] + 1; // +1 for current txn
            req.txnSum1h        = stats[1] + amount;
            req.avgTxnAmount    = req.txnSum1h / req.txnCount1h;

            sendPredictionRequest(req);
        }, throwable -> {
            progressBar.setVisibility(View.GONE);
            btnAnalyze.setEnabled(true);
            Toast.makeText(this, "Local DB error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
        });
        
        disposables.add(d);
    }

    private void sendPredictionRequest(TransactionRequest req) {
        RetrofitClient.getInstance().getApiService()
            .predictFraud(req)
            .enqueue(new Callback<FraudResponse>() {
                @Override
                public void onResponse(Call<FraudResponse> call, Response<FraudResponse> response) {
                    progressBar.setVisibility(View.GONE);
                    btnAnalyze.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null) {
                        displayResult(response.body());
                        saveTransactionToHistory(req);
                    } else {
                        Toast.makeText(MainActivity.this, "API Error", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<FraudResponse> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    btnAnalyze.setEnabled(true);
                    Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }

    private void saveTransactionToHistory(TransactionRequest req) {
        Disposable d = Completable.fromAction(() -> {
            AppDatabase.getInstance(this).transactionDao().insert(
                new TransactionEntity(req.senderId, req.amount, System.currentTimeMillis())
            );
        })
        .subscribeOn(Schedulers.io())
        .subscribe(
            () -> {}, // Success
            throwable -> Log.e("DB_ERROR", "Failed to save transaction: " + throwable.getMessage())
        );
        disposables.add(d);
    }


    private void displayResult(FraudResponse result) {
        cardResult.setVisibility(View.VISIBLE);
        tvRiskLevel.setText(result.riskLevel);
        tvProbability.setText(String.format("Fraud Probability: %.1f%%", result.fraudProbability * 100));
        tvRiskScore.setText(String.format("Risk Score: %.1f / 100", result.riskScore));
        tvRecommendation.setText("Action: " + result.recommendation);

        int color;
        switch (result.riskLevel) {
            case "HIGH":   color = Color.parseColor("#FF4444"); break;
            case "MEDIUM": color = Color.parseColor("#FF8800"); break;
            case "LOW":    color = Color.parseColor("#FFBB33"); break;
            default:       color = Color.parseColor("#00C851"); break;
        }
        cardResult.setCardBackgroundColor(color);
        tvRiskLevel.setTextColor(Color.WHITE);
    }
}
