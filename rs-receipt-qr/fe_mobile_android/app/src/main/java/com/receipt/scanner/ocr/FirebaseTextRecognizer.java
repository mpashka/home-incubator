package com.receipt.scanner.ocr;

import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Firebase ML cloud text recognizer for Cyrillic and Latin characters.
 * Uses Java 17 features: var, text blocks.
 *
 * This recognizer uses Firebase ML Vision cloud API and supports:
 * - Cyrillic alphabet (Serbian, Russian, etc.)
 * - Latin alphabet
 * - Multiple languages simultaneously
 *
 * Requires:
 * - Internet connection
 * - Firebase project configuration (google-services.json)
 * - Firebase ML Vision enabled in Firebase Console
 */
public final class FirebaseTextRecognizer implements TextRecognizerProvider {

    private static final String TAG = "FirebaseTextRecognizer";

    private final FirebaseVisionTextRecognizer textRecognizer;

    /**
     * Creates a new Firebase cloud text recognizer.
     * Configures the recognizer to support Serbian (Cyrillic) and Latin text.
     */
    public FirebaseTextRecognizer() {
        // Configure cloud recognizer with language hints for better accuracy
        var options = new FirebaseVisionCloudTextRecognizerOptions.Builder()
            .setLanguageHints(Arrays.asList("sr", "sr-Latn", "en"))  // Serbian Cyrillic, Latin, English
            .build();

        this.textRecognizer = FirebaseVision.getInstance()
            .getCloudTextRecognizer(options);

        Log.d(TAG, "Firebase cloud text recognizer initialized (Cyrillic + Latin)");
    }

    @Override
    public void recognizeText(
            @NonNull Image image,
            int rotationDegrees,
            @NonNull Consumer<String> onSuccess,
            @NonNull Consumer<String> onFailure
    ) {
        var rotation = degreesToFirebaseRotation(rotationDegrees);

        var metadata = new FirebaseVisionImageMetadata.Builder()
            .setWidth(image.getWidth())
            .setHeight(image.getHeight())
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setRotation(rotation)
            .build();

        // Convert Image to FirebaseVisionImage
        var firebaseImage = FirebaseVisionImage.fromMediaImage(image, rotation);

        textRecognizer.processImage(firebaseImage)
            .addOnSuccessListener(result -> {
                var recognizedText = result.getText();
                Log.d(TAG, "Cloud recognition successful, text length: " + recognizedText.length());
                onSuccess.accept(recognizedText);
            })
            .addOnFailureListener(e -> {
                var errorMessage = e.getMessage() != null ? e.getMessage() : "Cloud OCR error";
                Log.e(TAG, "Cloud recognition failed: " + errorMessage, e);
                onFailure.accept(errorMessage);
            });
    }

    /**
     * Converts rotation degrees to Firebase rotation constant.
     */
    private int degreesToFirebaseRotation(int rotationDegrees) {
        return switch (rotationDegrees) {
            case 0 -> FirebaseVisionImageMetadata.ROTATION_0;
            case 90 -> FirebaseVisionImageMetadata.ROTATION_90;
            case 180 -> FirebaseVisionImageMetadata.ROTATION_180;
            case 270 -> FirebaseVisionImageMetadata.ROTATION_270;
            default -> {
                Log.w(TAG, "Unknown rotation: " + rotationDegrees + ", defaulting to 0");
                yield FirebaseVisionImageMetadata.ROTATION_0;
            }
        };
    }

    @Override
    public void close() {
        textRecognizer.close();
        Log.d(TAG, "Firebase cloud text recognizer closed");
    }

    @Override
    public Mode getMode() {
        return Mode.FIREBASE_CLOUD;
    }

    @Override
    public String getDescription() {
        return """
            Firebase ML Cloud Text Recognizer
            - Requires internet connection
            - Supports Cyrillic alphabet (Serbian)
            - Supports Latin alphabet
            - Higher accuracy for mixed text
            - Best for full receipt text recognition
            """;
    }
}
