package com.receipt.scanner.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReceiptApi {

    @POST("api/receipt-raw")
    suspend fun sendQrCode(@Body request: QrRequest): Response<QrResponse>

    @GET("api/receipt-raw/{id}")
    suspend fun getStatus(@Path("id") id: Int): Response<StatusResponse>
}

data class QrRequest(
    val userId: Int,
    val url: String
)

data class QrResponse(
    val receiptRawId: Int,
    val status: String
)

data class StatusResponse(
    val receiptRawId: Int,
    val status: String,
    val receiptId: Int?
)
