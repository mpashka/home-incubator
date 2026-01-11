package com.receipt.scanner.model;

/**
 * Java 17 record for receiving receipt text response from the backend API.
 *
 * @param receiptTextId the ID of the created receipt text record
 * @param status the processing status
 */
public record ReceiptTextResponse(
    int receiptTextId,
    String status
) {
    public static final String STATUS_RECEIVED = "received";
    public static final String STATUS_ERROR = "error";
}
