package com.receipt.scanner.model;

/**
 * Java 17 record for sending receipt text data to the backend API.
 * Uses Java 17 features: record syntax.
 *
 * @param userId the user ID performing the scan
 * @param pfrTime PFR timestamp (e.g., "07.09.2024. 19:25:47")
 * @param pfrReceiptNumber PFR receipt number (e.g., "P8BPS55R-P8BPS55R-71822")
 * @param pfrCounter receipt counter value (e.g., "64763/71822PP")
 */
public record ReceiptTextRequest(
    int userId,
    String pfrTime,
    String pfrReceiptNumber,
    String pfrCounter
) {
    /**
     * Creates a ReceiptTextRequest from ReceiptTextData.
     *
     * @param userId the user ID
     * @param data the parsed receipt text data
     * @return a new ReceiptTextRequest
     */
    public static ReceiptTextRequest fromData(int userId, ReceiptTextData data) {
        return new ReceiptTextRequest(
            userId,
            data.pfrTime(),
            data.pfrReceiptNumber(),
            data.pfrCounter()
        );
    }
}
