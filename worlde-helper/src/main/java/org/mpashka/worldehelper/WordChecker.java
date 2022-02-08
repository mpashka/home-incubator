package org.mpashka.worldehelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mpashka.worldehelper.Utils.WORD_LENGTH;
import static org.mpashka.worldehelper.CompetitionInterface.*;

public class WordChecker {
    private static final Logger log = LoggerFactory.getLogger(WordChecker.class);

    private Language language;
    private BitSet nonPresentChars;
    private byte[] greenChars = new byte[WORD_LENGTH];
    private Map<Byte, CharInfo> presentChars; // green + yellow
    /** Max number of chars in the word. Decrease if we have guessed 2 double chars in single word */
    private int wordMaxChars;

    public WordChecker(Language language) {
        this.language = language;
    }

    public BitSet getNonPresentChars() {
        return nonPresentChars;
    }

    public byte[] getGreenChars() {
        return greenChars;
    }

    public Map<Byte, CharInfo> getPresentChars() {
        return presentChars;
    }

    public int getWordMaxChars() {
        return wordMaxChars;
    }

    //
    //
    //

    public void clear() {
        Arrays.fill(greenChars, (byte) 0);
        presentChars = new HashMap<>(WORD_LENGTH);
        nonPresentChars = new BitSet(language.letters());
        wordMaxChars = WORD_LENGTH;
    }

    public boolean conform(String word) {
        int knownChars = 0, unknownChars = 0, maxUnknownChars = wordMaxChars - presentChars.size();
        BitSet visited = new BitSet(language.letters());
        Map<Byte, AtomicInteger> charCount = new HashMap<>(WORD_LENGTH);
        for (int i = 0; i < word.length(); i++) {
            byte c = language.idx(word.charAt(i));
            if (greenChars[i] != 0 && greenChars[i] != c) {
                return false;
            }
            if (nonPresentChars.get(c)) {
                return false;
            }
            CharInfo charInfo = presentChars.get(c);
            if (charInfo == null) {
                if (++unknownChars > maxUnknownChars) {
                    return false;
                }

            } else {
                if (charInfo.wrongPositions.get(i)) {
                    return false;
                }

                // Check how many chars were found in this word
                if (!visited.get(c)) {
                    visited.set(c);
                    knownChars++;
                }

                // Check single and double chars
                if (charInfo.count != -1) {
                    int count = charCount.computeIfAbsent(c, c_ -> new AtomicInteger()).incrementAndGet();
                    if (charInfo.count == 1 && count > 1) {
                        return false;
                    }
                }
            }
        }
        for (Map.Entry<Byte, AtomicInteger> charCountEntry : charCount.entrySet()) {
            CharInfo charInfo = presentChars.get(charCountEntry.getKey());
            if (charInfo.count == 2 && charCountEntry.getValue().get() < 2) {
                return false;
            }
        }
        return knownChars == presentChars.size();
    }

    public void guessWordAttempt(String word, WordResult wordResult) {
        Map<Byte, AtomicInteger> charCount = new HashMap<>(WORD_LENGTH);
        for (int i = 0; i < WORD_LENGTH; i++) {
            byte c = language.idx(word.charAt(i));
            charCount.computeIfAbsent(c, c_ -> new AtomicInteger()).incrementAndGet();
        }

        for (int i = 0; i < WORD_LENGTH; i++) {
            byte c = language.idx(word.charAt(i));
            CompetitionInterface.CharResult charResult = wordResult.result()[i];
            AtomicInteger count = charCount.remove(c);
            if (count == null) {
                // Multiple chars were already processed
                continue;
            } else if (count.get() == 1) {
                switch (charResult) {
                    case black -> nonPresentChars.set(c);
                    case green,yellow -> getCharInfo(c).set(charResult, i);
                }
            } else {
                processSameChars(word, wordResult.result(), i);
            }
        }
    }

    private void processSameChars(String word, CharResult[] result, int fromPos) {
        byte c = language.idx(word.charAt(fromPos));
        CharResult min = result[fromPos];
        CharResult max = result[fromPos];
        int count = min == CharResult.black ? 0 : 1;
        for (int i = fromPos+1; i < WORD_LENGTH; i++) {
            if (language.idx(word.charAt(i)) != c) continue;
            min = Utils.min(min, result[i]);
            max = Utils.max(max, result[i]);
            if (result[i] != CharResult.black) {
                count++;
            }
        }

        if (max == CompetitionInterface.CharResult.black) {
            nonPresentChars.set(c);
            return;
        }

        CharInfo charInfo = getCharInfo(c);
        charInfo.setCount(count);

        for (int i = fromPos; i < WORD_LENGTH; i++) {
            if (language.idx(word.charAt(i)) == c) {
                charInfo.set(result[i], i);
            }
        }
    }

    private CharInfo getCharInfo(byte c) {
        return presentChars.computeIfAbsent(c, c2 -> new CharInfo(c));
    }


    public class CharInfo {
        private final BitSet correctPositions = new BitSet(WORD_LENGTH);
        private final BitSet wrongPositions = new BitSet(WORD_LENGTH);
        private int count = -1;
        private byte c;

        public CharInfo(byte c) {
            this.c = c;
        }

        public CharInfo set(CompetitionInterface.CharResult result, int pos) {
            boolean green = result == CompetitionInterface.CharResult.green;
            if (green) {
                greenChars[pos] = c;
            }

            (green ? correctPositions : wrongPositions).set(pos);
            return this;
        }

        public BitSet getCorrectPositions() {
            return correctPositions;
        }

        public BitSet getWrongPositions() {
            return wrongPositions;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            if (this.count == -1) {
                this.count = count;
                if (count >= 2) {
                    wordMaxChars -= count-1;
                }
            } else if (count > this.count) {
                wordMaxChars -= count-this.count;
                this.count = count;
            } else if (count == this.count) {
                // Ignore
            } else {
                log.error("Char count was {}, now set to {}: {}", this.count, count, language.ofIdx(c));
            }
        }

        public byte getChar() {
            return c;
        }

        public boolean isPosAllKnown() {
            return count != -1 && (correctPositions.cardinality() == count);
        }

        public boolean isPosKnown() {
            return correctPositions.cardinality() > 0;
        }
    }
}
