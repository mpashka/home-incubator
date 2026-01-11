package com.receipt.scanner.util;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.receipt.scanner.model.ReceiptTextData;
import com.receipt.scanner.ocr.TextRecognizerFactory;
import com.receipt.scanner.ocr.TextRecognizerProvider;

import java.util.function.Consumer;

/**
 * CameraX ImageAnalyzer for recognizing Serbian fiscal receipt text using OCR.
 * Uses Java 17 features: var, records, functional interfaces.
 *
 * Supports two recognition modes:
 * - ML Kit offline (Latin only) - fast, works offline
 * - Firebase ML cloud (Cyrillic + Latin) - requires internet
 *
 * Scans for PFR (Poreska fiskalna registracija) data:
 * - PFR Time
 * - PFR Receipt Number
 * - Counter
 */
public final class TextReceiptAnalyzer implements ImageAnalysis.Analyzer {

    private static final String TAG = "TextReceiptAnalyzer";

    private final TextRecognizerProvider textRecognizer;
    private final Consumer<ReceiptTextData> onReceiptDetected;
    private final Consumer<String> onError;
    private final Consumer<String> onTextDetected;

    private volatile boolean isProcessing = false;
    private volatile boolean isEnabled = true;

    /**
     * Creates a new TextReceiptAnalyzer with the specified recognition mode.
     *
     * @param mode the recognition mode (MLKIT_OFFLINE_LATIN or FIREBASE_CLOUD)
     * @param onReceiptDetected callback when valid receipt data is detected
     * @param onError callback when an error occurs
     * @param onTextDetected optional callback for raw OCR text (for debugging)
     */
    public TextReceiptAnalyzer(
            TextRecognizerProvider.Mode mode,
            Consumer<ReceiptTextData> onReceiptDetected,
            Consumer<String> onError,
            Consumer<String> onTextDetected
    ) {
        this.textRecognizer = TextRecognizerFactory.create(mode);
        this.onReceiptDetected = onReceiptDetected;
        this.onError = onError;
        this.onTextDetected = onTextDetected;
        Log.d(TAG, "TextReceiptAnalyzer initialized with mode: " + mode);
    }

    /**
     * Creates a new TextReceiptAnalyzer with ML Kit offline Latin recognizer.
     *
     * @param onReceiptDetected callback when valid receipt data is detected
     * @param onError callback when an error occurs
     * @param onTextDetected optional callback for raw OCR text (for debugging)
     */
    public TextReceiptAnalyzer(
            Consumer<ReceiptTextData> onReceiptDetected,
            Consumer<String> onError,
            Consumer<String> onTextDetected
    ) {
        this(TextRecognizerProvider.Mode.MLKIT_OFFLINE_LATIN, onReceiptDetected, onError, onTextDetected);
    }

    /**
     * Creates a new TextReceiptAnalyzer without debug callback.
     * Uses ML Kit offline Latin recognizer by default.
     *
     * @param onReceiptDetected callback when valid receipt data is detected
     * @param onError callback when an error occurs
     */
    public TextReceiptAnalyzer(
            Consumer<ReceiptTextData> onReceiptDetected,
            Consumer<String> onError
    ) {
        this(TextRecognizerProvider.Mode.MLKIT_OFFLINE_LATIN, onReceiptDetected, onError, null);
    }

    /**
     * Creates a new TextReceiptAnalyzer with specified mode, without debug callback.
     *
     * @param mode the recognition mode
     * @param onReceiptDetected callback when valid receipt data is detected
     * @param onError callback when an error occurs
     */
    public TextReceiptAnalyzer(
            TextRecognizerProvider.Mode mode,
            Consumer<ReceiptTextData> onReceiptDetected,
            Consumer<String> onError
    ) {
        this(mode, onReceiptDetected, onError, null);
    }

    /**
     * Enables or disables the analyzer.
     *
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    /**
     * @return true if the analyzer is currently enabled
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    /**
     * @return the current recognition mode
     */
    public TextRecognizerProvider.Mode getMode() {
        return textRecognizer.getMode();
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        if (!isEnabled || isProcessing) {
            imageProxy.close();
            return;
        }

        @SuppressWarnings("UnsafeOptInUsageError")
        var mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            imageProxy.close();
            return;
        }

        isProcessing = true;
        var rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();

        textRecognizer.recognizeText(
            mediaImage,
            rotationDegrees,
            recognizedText -> {
                // Debug callback if provided
                if (onTextDetected != null && !recognizedText.isBlank()) {
                    onTextDetected.accept(recognizedText);
                }

                // Check if text contains receipt markers
                if (ReceiptTextParser.containsReceiptMarkers(recognizedText)) {
                    var parsedData = ReceiptTextParser.parse(recognizedText);

                    if (parsedData.isPresent()) {
                        Log.d(TAG, "Receipt data detected: " + parsedData.get());
                        onReceiptDetected.accept(parsedData.get());
                    } else {
                        // Found markers but couldn't parse all fields
                        Log.d(TAG, "Found receipt markers but parse incomplete:\n" +
                            ReceiptTextParser.debugParse(recognizedText));
                    }
                }

                isProcessing = false;
                imageProxy.close();
            },
            errorMessage -> {
                Log.e(TAG, "Text recognition failed: " + errorMessage);
                onError.accept(errorMessage);
                isProcessing = false;
                imageProxy.close();
            }
        );
    }

    /**
     * Releases resources used by the text recognizer.
     * Call this when the analyzer is no longer needed.
     */
    public void close() {
        textRecognizer.close();
    }
}
