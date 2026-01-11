package com.receipt.scanner.ocr;

import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.function.Consumer;

/**
 * ML Kit offline text recognizer for Latin characters.
 * Uses Java 17 features: var, text blocks.
 *
 * This recognizer works offline and is optimized for Latin alphabet.
 * It can recognize receipt numbers like "P8BPS55R-P8BPS55R-71822"
 * but will not recognize Cyrillic text properly.
 *
 * For Cyrillic text, use {@link FirebaseTextRecognizer} instead.
 */
public final class MlKitLatinTextRecognizer implements TextRecognizerProvider {

    private static final String TAG = "MlKitLatinRecognizer";

    private final TextRecognizer textRecognizer;

    /**
     * Creates a new ML Kit Latin text recognizer.
     * Uses the default Latin text recognizer options for offline recognition.
     */
    public MlKitLatinTextRecognizer() {
        var options = new TextRecognizerOptions.Builder().build();
        this.textRecognizer = TextRecognition.getClient(options);
        Log.d(TAG, "ML Kit Latin text recognizer initialized (offline mode)");
    }

    @Override
    public void recognizeText(
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

    @Override
    public void close() {
        textRecognizer.close();
        Log.d(TAG, "ML Kit Latin text recognizer closed");
    }

    @Override
    public Mode getMode() {
        return Mode.MLKIT_OFFLINE_LATIN;
    }

    @Override
    public String getDescription() {
        return """
            ML Kit Offline Latin Text Recognizer
            - Works without internet connection
            - Optimized for Latin alphabet (A-Z, 0-9)
            - Fast recognition speed
            - Best for receipt numbers and codes
            """;
    }
}
