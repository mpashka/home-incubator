package com.receipt.scanner.ocr;

import android.media.Image;

import androidx.annotation.NonNull;

import java.util.function.Consumer;

/**
 * Interface for text recognition providers.
 * Allows switching between different OCR implementations:
 * - ML Kit offline (Latin only)
 * - Firebase ML (Cyrillic + Latin, requires cloud)
 */
public interface TextRecognizerProvider {

    /**
     * Recognition mode enumeration.
     */
    enum Mode {
        /**
         * ML Kit offline text recognition (Latin characters only).
         * Works without internet, fast, but limited to Latin alphabet.
         * Good for extracting receipt numbers like "P8BPS55R-P8BPS55R-71822".
         */
        MLKIT_OFFLINE_LATIN,

        /**
         * Firebase ML cloud text recognition (Cyrillic + Latin).
         * Requires internet connection, supports multiple languages.
         * Good for full receipt text including Serbian Cyrillic.
         */
        FIREBASE_CLOUD
    }

    /**
     * Recognizes text from a camera image.
     *
     * @param image the camera image to process
     * @param rotationDegrees the rotation of the image
     * @param onSuccess callback with recognized text
     * @param onFailure callback with error message
     */
    void recognizeText(
        @NonNull Image image,
        int rotationDegrees,
        @NonNull Consumer<String> onSuccess,
        @NonNull Consumer<String> onFailure
    );

    /**
     * Releases resources used by the recognizer.
     */
    void close();

    /**
     * Returns the recognition mode of this provider.
     */
    Mode getMode();

    /**
     * Returns a human-readable description of this provider.
     */
    String getDescription();
}
