package org.mpashka.worldehelper;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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

    public static Map<Character, BitSet> wordChars(String word) {
        Map<Character, BitSet> wordChars = new HashMap<>(WORD_LENGTH);
        for (int i = 0; i < WORD_LENGTH; i++) {
            wordChars.computeIfAbsent(word.charAt(i), c -> new BitSet(WORD_LENGTH)).set(i);
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
}
