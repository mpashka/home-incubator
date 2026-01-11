package com.receipt.scanner.util;

import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.function.Consumer;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.receipt.scanner.model.ReceiptTextData;

/**
 * CameraX ImageAnalyzer for recognizing Serbian fiscal receipt text using OCR.
 * Uses Java 17 features: var, records, functional interfaces.
 *
 * Scans for PFR (Poreska fiskalna registracija) data:
 * - PFR Time
 * - PFR Receipt Number
 * - Counter
 *
 * This recognizer works offline and is optimized for Latin alphabet.
 * It can recognize receipt numbers like
 */
public final class TextReceiptAnalyzer implements ImageAnalysis.Analyzer {

    private static final String TAG = "TextReceiptAnalyzer";

    private final Consumer<ReceiptTextData> onReceiptDetected;
    private final Consumer<String> onError;
    private final Consumer<String> onTextDetected;

    private volatile boolean isProcessing = false;
    private volatile boolean isEnabled = true;

    private final TextRecognizer textRecognizer;

    /**
     * Creates a new ML Kit Latin text recognizer.
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
        this.onReceiptDetected = onReceiptDetected;
        this.onError = onError;
        this.onTextDetected = onTextDetected;
        var options = new TextRecognizerOptions.Builder().build();
        this.textRecognizer = TextRecognition.getClient(options);
        Log.d(TAG, "TextReceiptAnalyzer initialized");
    }

    /**
     * Enables or disables the analyzer.
     *
     * @param enabled true to enable, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
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

        recognizeText(
            mediaImage,
            rotationDegrees,
            recognizedText -> {
                if (recognizedText.isBlank()) {
                    return;
                }

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
                } else {
                    Log.d(TAG, "Receipt markers not found:\n" + recognizedText);
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

    private void recognizeText(
            @NonNull Image image,
            int rotationDegrees,
            @NonNull Consumer<String> onSuccess,
            @NonNull Consumer<String> onFailure
    ) {
        var inputImage = InputImage.fromMediaImage(image, rotationDegrees);

        textRecognizer.process(inputImage)
                .addOnSuccessListener(result -> {
                    var recognizedText = result.getText();
                    Log.d(TAG, "Recognition successful, text length: " + recognizedText.length());
                    onSuccess.accept(recognizedText);
                })
                .addOnFailureListener(e -> {
                    var errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown OCR error";
                    Log.e(TAG, "Recognition failed: " + errorMessage, e);
                    onFailure.accept(errorMessage);
                });
    }

    public void close() {
        textRecognizer.close();
        Log.d(TAG, "ML Kit Latin text recognizer closed");
    }
}
