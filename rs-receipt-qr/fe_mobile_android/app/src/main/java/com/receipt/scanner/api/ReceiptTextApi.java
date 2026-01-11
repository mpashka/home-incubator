package com.receipt.scanner.api;

import com.receipt.scanner.model.ReceiptTextRequest;
import com.receipt.scanner.model.ReceiptTextResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Retrofit API interface for sending receipt text data to the backend.
 * Uses Java 17 records for request/response models.
 */
public interface ReceiptTextApi {

    /**
     * Sends scanned receipt text data to the backend.
     *
     * @param request the receipt text data containing PFR time, number, and counter
     * @return Call with response containing the created record ID
     */
    @POST("api/receipt-text")
    Call<ReceiptTextResponse> sendReceiptText(@Body ReceiptTextRequest request);
}
