package com.receipt.scanner.util;

import com.receipt.scanner.model.ReceiptTextData;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Parser for Serbian fiscal receipt text (PFR - Poreska fiskalna registracija).
 * Uses Java 17 features: var, text blocks, records.
 *
 * Parses text containing three key lines:
 * - ПФР време (PFR time)
 * - ПФР број рачуна (PFR receipt number)
 * - Бројач рачуна (Receipt counter)
 */
public final class ReceiptTextParser {

    // Pattern for PFR time: "ПФР време:" followed by date time
    // Example: "ПФР време:          07.09.2024. 19:25:47"
    private static final Pattern PFR_TIME_PATTERN = Pattern.compile(
        "ПФР\\s*време\\s*:\\s*(\\d{2}\\.\\d{2}\\.\\d{4}\\.?\\s+\\d{2}:\\d{2}:\\d{2})",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    // Pattern for PFR receipt number: "ПФР број рачуна:" followed by alphanumeric code
    // Example: "ПФР број рачуна: P8BPS55R-P8BPS55R-71822"
    private static final Pattern PFR_RECEIPT_NUMBER_PATTERN = Pattern.compile(
        "ПФР\\s*број\\s*рачуна\\s*:\\s*([A-Za-z0-9\\-]+)",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    // Pattern for counter: "Бројач рачуна:" followed by counter value
    // Example: "Бројач рачуна:             64763/71822ПП"
    private static final Pattern COUNTER_PATTERN = Pattern.compile(
        "Бројач\\s*рачуна\\s*:\\s*([0-9/]+[А-Яа-яA-Za-z]*)",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private ReceiptTextParser() {
        // Utility class
    }

    /**
     * Parses the OCR text and extracts PFR data.
     *
     * @param ocrText the raw text from OCR recognition
     * @return Optional containing ReceiptTextData if all fields were found
     */
    public static Optional<ReceiptTextData> parse(String ocrText) {
        if (ocrText == null || ocrText.isBlank()) {
            return Optional.empty();
        }

        var pfrTime = extractMatch(ocrText, PFR_TIME_PATTERN);
        var pfrReceiptNumber = extractMatch(ocrText, PFR_RECEIPT_NUMBER_PATTERN);
        var pfrCounter = extractMatch(ocrText, COUNTER_PATTERN);

        if (pfrTime.isEmpty() || pfrReceiptNumber.isEmpty() || pfrCounter.isEmpty()) {
            return Optional.empty();
        }

        var data = new ReceiptTextData(
            pfrTime.get(),
            pfrReceiptNumber.get(),
            pfrCounter.get()
        );

        return data.isValid() ? Optional.of(data) : Optional.empty();
    }

    /**
     * Checks if the text contains receipt markers (the separator lines).
     *
     * @param text the text to check
     * @return true if text appears to contain receipt data
     */
    public static boolean containsReceiptMarkers(String text) {
        if (text == null) {
            return false;
        }
        // Check for separator line or PFR keywords
        var hasMarkers = text.contains("====")
            || (text.contains("ПФР") && text.contains("рачуна"));
        return hasMarkers;
    }

    /**
     * Extracts a single match from text using the given pattern.
     *
     * @param text the text to search
     * @param pattern the pattern with one capture group
     * @return Optional containing the captured group if found
     */
    private static Optional<String> extractMatch(String text, Pattern pattern) {
        var matcher = pattern.matcher(text);
        if (matcher.find() && matcher.groupCount() >= 1) {
            var value = matcher.group(1);
            return value != null && !value.isBlank()
                ? Optional.of(value.trim())
                : Optional.empty();
        }
        return Optional.empty();
    }

    /**
     * Debug helper to show what was found in the text.
     *
     * @param ocrText the OCR text
     * @return debug information string
     */
    public static String debugParse(String ocrText) {
        if (ocrText == null) {
            return "Input text is null";
        }

        var pfrTime = extractMatch(ocrText, PFR_TIME_PATTERN);
        var pfrNumber = extractMatch(ocrText, PFR_RECEIPT_NUMBER_PATTERN);
        var counter = extractMatch(ocrText, COUNTER_PATTERN);

        return """
            Debug parse results:
            - PFR Time: %s
            - PFR Receipt Number: %s
            - Counter: %s
            - Contains markers: %s
            """.formatted(
                pfrTime.orElse("NOT FOUND"),
                pfrNumber.orElse("NOT FOUND"),
                counter.orElse("NOT FOUND"),
                containsReceiptMarkers(ocrText)
            );
    }
}
