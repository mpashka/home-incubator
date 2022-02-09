package org.mpashka.worldehelper;

import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.BitSet;
import java.util.Map;
import java.util.Set;

import static org.mpashka.worldehelper.Utils.WORD_LENGTH;

public class WordsConverter {
    @Test
    public void excludeDoubleLetters() throws IOException {
        Language language = Language.rus;
        String[] words = Utils.loadWords(language, language.fileName());
        try (PrintWriter out = new PrintWriter(new FileWriter(".in/rus_nodup.txt"))) {
            BitSet chars = new BitSet(language.letters());
            mainCycle:
            for (String word : words) {
                chars.clear();
                for (int i = 0; i < WORD_LENGTH; i++) {
                    byte c = language.idx(word.charAt(i));
                    if (chars.get(c)) continue mainCycle;
                    chars.set(c);
                }
                out.println(word);
            }
        }
    }
}
