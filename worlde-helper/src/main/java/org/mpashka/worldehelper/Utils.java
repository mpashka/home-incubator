package org.mpashka.worldehelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import static org.mpashka.worldehelper.CompetitionInterface.*;

// 🟩🟨🟦🟪️🟫⬛⬜
public class Utils {
    public static final int WORD_LENGTH = 5;
    public static final int MAX_ANSWERS = 6;

    public static String randomString(int length) {
        return new Random().ints('a', 'z' + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static Map<Byte, BitSet> wordChars(Language language, String word) {
        Map<Byte, BitSet> wordChars = new HashMap<>(WORD_LENGTH);
        for (int i = 0; i < WORD_LENGTH; i++) {
            wordChars.computeIfAbsent(language.idx(word.charAt(i)), c -> new BitSet(WORD_LENGTH)).set(i);
        }
        return wordChars;
    }

/*
    CharResult min(CharResult ... in) {
        return Arrays.stream(in).min((v1, v2) -> Integer.compare(v2.ordinal(), v1.ordinal())).get();
    }

    CharResult max(CharResult ... in) {
        return Arrays.stream(in).min((v1, v2) -> Integer.compare(v2.ordinal(), v1.ordinal())).get();
    }
*/

    public static CharResult min(CharResult in1, CharResult in2) {
        return in1.ordinal() > in2.ordinal() ? in1 : in2;
    }

    public static CharResult max(CharResult in1, CharResult in2) {
        return in1.ordinal() < in2.ordinal() ? in1 : in2;
    }

    public static boolean contains(byte idx, byte[] wordChars, int start, int end) {
        for (int i = start; i < end; i++) {
            if (wordChars[i] == idx) return true;
        }
        return false;
    }

    public static String[] loadWords(Language language, String wordsFile) throws IOException {
//        String wordsFile = language.fileName();
        try (Stream<String> wordsStream = Files.lines(Paths.get(wordsFile))
                .map(String::toLowerCase)
                .filter(language::isCorrect)) {
            return wordsStream.toArray(String[]::new);
        }
//        log.info("Lang {}, {} words", language.name(), wordList.length);
    }
}
