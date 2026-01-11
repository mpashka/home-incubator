package com.receipt.scanner.model;

/**
 * Java 17 record representing parsed receipt text data from Serbian fiscal receipts.
 * Contains PFR (Poreska fiskalna registracija) information:
 * - pfrTime: timestamp in format dd.MM.yyyy. HH:mm:ss
 * - pfrReceiptNumber: receipt number in format XXXXXXXX-XXXXXXXX-NNNNN
 * - pfrCounter: counter value in format NNNNN/NNNNNpp
 */
public record ReceiptTextData(
    String pfrTime,
    String pfrReceiptNumber,
    String pfrCounter
) {
    /**
     * Example format from Serbian fiscal receipts:
     * {@snippet :
     *   ========================================
     *   ПФР време:          07.09.2024. 19:25:47
     *   ПФР број рачуна: P8BPS55R-P8BPS55R-71822
     *   Бројач рачуна:             64763/71822ПП
     *   ========================================
     * }
     */
    public static final String RECEIPT_FORMAT_EXAMPLE = """
        ========================================
        ПФР време:          07.09.2024. 19:25:47
        ПФР број рачу|на: P8BPS55R-P8BPS55R-71822
        Бројач рачуна:             64763/71822ПП
        ========================================
        """;

    /**
     * Validates that all required fields are present and non-empty.
     * @return true if all fields are valid
     */
    public boolean isValid() {
        return pfrTime != null && !pfrTime.isBlank()
            && pfrReceiptNumber != null && !pfrReceiptNumber.isBlank()
            && pfrCounter != null && !pfrCounter.isBlank();
    }

    @Override
    public String toString() {
        return """
            ReceiptTextData {
                pfrTime: %s,
                pfrReceiptNumber: %s,
                pfrCounter: %s
            }
            """.formatted(pfrTime, pfrReceiptNumber, pfrCounter);
    }
}
