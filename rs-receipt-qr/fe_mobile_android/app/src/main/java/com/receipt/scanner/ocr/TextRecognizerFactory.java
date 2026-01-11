package com.receipt.scanner.ocr;

import android.util.Log;

/**
 * Factory for creating text recognizer instances.
 * Uses Java 17 features: var, switch expressions.
 */
public final class TextRecognizerFactory {

    private static final String TAG = "TextRecognizerFactory";

    private TextRecognizerFactory() {
        // Utility class
    }

    /**
     * Creates a text recognizer for the specified mode.
     *
     * @param mode the recognition mode
     * @return a new TextRecognizerProvider instance
     */
    public static TextRecognizerProvider create(TextRecognizerProvider.Mode mode) {
        Log.d(TAG, "Creating text recognizer for mode: " + mode);

        return switch (mode) {
            case MLKIT_OFFLINE_LATIN -> new MlKitLatinTextRecognizer();
            case FIREBASE_CLOUD -> new FirebaseTextRecognizer();
        };
    }

    /**
     * Creates the default text recognizer.
     * Uses ML Kit offline Latin recognizer as it works without internet.
     *
     * @return a new TextRecognizerProvider instance
     */
    public static TextRecognizerProvider createDefault() {
        return create(TextRecognizerProvider.Mode.MLKIT_OFFLINE_LATIN);
    }

    /**
     * Creates a text recognizer optimized for Serbian receipts.
     * Uses Firebase cloud recognizer for Cyrillic support.
     *
     * @return a new TextRecognizerProvider instance
     */
    public static TextRecognizerProvider createForSerbianReceipts() {
        return create(TextRecognizerProvider.Mode.FIREBASE_CLOUD);
    }
}
