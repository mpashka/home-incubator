package org.mpashka.worldehelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mpashka.worldehelper.Utils.WORD_LENGTH;
import static org.mpashka.worldehelper.CompetitionInterface.*;

public class WordChecker {
    private static final Logger log = LoggerFactory.getLogger(AlgorithmFrequentSimple.class);

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
        BitSet skipChars = new BitSet(WORD_LENGTH);
        for (int i = 0; i < WORD_LENGTH; i++) {
            byte c = language.idx(word.charAt(i));
            CompetitionInterface.CharResult charResult = wordResult.result()[i];
            if (skipChars.get(i)) {
                continue;
            }
            int pos2 = findNextChar(word, i, c);
            if (pos2 == -1) {
                switch (charResult) {
                    case black -> nonPresentChars.set(c);
                    case green,yellow -> addCharInfo(c, i, charResult);
                }
            } else {
                skipChars.set(pos2);

                int pos3 = findNextChar(word, pos2, c);
                if (pos3 != -1) {
                    log.warn("Unexpected same char '{}' count in word {}:{}", language.ofIdx(c), word, pos3);
                }


                CompetitionInterface.CharResult charResult2 = wordResult.result()[pos2];
                CompetitionInterface.CharResult max = Utils.max(charResult, charResult2);
                if (max == CompetitionInterface.CharResult.black) {
                    nonPresentChars.set(c);
                    continue;
                }
                CompetitionInterface.CharResult min = Utils.min(charResult, charResult2);
                if (min == CompetitionInterface.CharResult.black) {
                    // We have limit - usually one letter
                    int presPos = charResult != CompetitionInterface.CharResult.black ? i : pos2;
                    CharInfo charInfo = addCharInfo(c, presPos, max);
                    charInfo.setCount(1+findCharsCount(word, pos2, c));
                    int blackPos = charResult == CompetitionInterface.CharResult.black ? i : pos2;
                    charInfo.set(CompetitionInterface.CharResult.black, blackPos);
                    continue;
                }
                // Two letters
                CharInfo charInfo = addCharInfo(c, i, charResult)
                        .set(charResult2, pos2);
                charInfo.setDouble();
            }
        }
    }

    private int findNextChar(String word, int pos, byte c) {
        for (int i = pos+1; i < WORD_LENGTH; i++) {
            if (language.idx(word.charAt(i)) == c) return i;
        }
        return -1;
    }

    private int findCharsCount(String word, int fromPos, byte c) {
        int count = 0;
        for (int i = fromPos+1; i < WORD_LENGTH; i++) {
            if (language.idx(word.charAt(i)) == c) count++;
        }
        return count;
    }

    private CharInfo addCharInfo(byte c, int pos, CompetitionInterface.CharResult result) {
        return presentChars.computeIfAbsent(c, c2 -> new CharInfo(c))
                .set(result, pos);
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
            } else if (this.count != count) {
                log.error("Was {}, now set to {}: {}", this.count, count, language.ofIdx(c));
            }

        }

        public void setDouble() {
            if (count == -1) {
                count = 2;
                wordMaxChars--;
            } else if (count == 1) {
                log.error("Was 1, now set to 2: {}", c);
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
