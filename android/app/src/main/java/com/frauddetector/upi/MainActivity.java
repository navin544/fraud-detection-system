// app/src/main/java/com/frauddetector/upi/MainActivity.java
package com.frauddetector.upi;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.frauddetector.upi.model.*;
import com.frauddetector.upi.network.RetrofitClient;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    private void analyzeTransaction() {
        String amountStr = etAmount.getText().toString().trim();
        String senderId  = etSenderId.getText().toString().trim();

        if (amountStr.isEmpty() || senderId.isEmpty()) {
            Toast.makeText(this, "Enter amount and sender ID", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        cardResult.setVisibility(View.GONE);
        btnAnalyze.setEnabled(false);

        TransactionRequest req = new TransactionRequest(
            Double.parseDouble(amountStr), senderId
        );
        req.receiverId      = etReceiverId.getText().toString().trim();
        req.isNewBeneficiary= cbNewBeneficiary.isChecked() ? 1 : 0;
        req.deviceChanged   = cbDeviceChange.isChecked() ? 1 : 0;
        req.locationAnomaly = cbLocation.isChecked() ? 1 : 0;

        RetrofitClient.getInstance().getApiService()
            .predictFraud(req)
            .enqueue(new Callback<FraudResponse>() {
                @Override
                public void onResponse(Call<FraudResponse> call, Response<FraudResponse> response) {
                    progressBar.setVisibility(View.GONE);
                    btnAnalyze.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null) {
                        displayResult(response.body());
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
