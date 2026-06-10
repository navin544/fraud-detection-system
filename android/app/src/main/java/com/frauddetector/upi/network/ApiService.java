// app/src/main/java/com/frauddetector/upi/network/ApiService.java
package com.frauddetector.upi.network;

import com.frauddetector.upi.model.TransactionRequest;
import com.frauddetector.upi.model.FraudResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/v1/predict")
    Call<FraudResponse> predictFraud(@Body TransactionRequest request);

    @GET("api/v1/health")
    Call<Object> healthCheck();

    @GET("api/v1/metrics")
    Call<Object> getModelMetrics();
}
