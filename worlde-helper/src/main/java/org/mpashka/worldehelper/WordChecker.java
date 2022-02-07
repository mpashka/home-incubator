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
    private char[] greenChars = new char[WORD_LENGTH];
    private Map<Character, CharInfo> presentChars; // green + yellow
    /** Max number of chars in the word. Decrease if we have guessed 2 double chars in single word */
    private int wordMaxChars;

    public WordChecker(Language language) {
        this.language = language;
    }

    public BitSet getNonPresentChars() {
        return nonPresentChars;
    }

    public char[] getGreenChars() {
        return greenChars;
    }

    public Map<Character, CharInfo> getPresentChars() {
        return presentChars;
    }

    public int getWordMaxChars() {
        return wordMaxChars;
    }

    //
    //
    //

    public void clear() {
        Arrays.fill(greenChars, (char) 0);
        presentChars = new HashMap<>(WORD_LENGTH);
        nonPresentChars = new BitSet(language.letters());
        wordMaxChars = WORD_LENGTH;
    }

    public boolean conform(String word) {
        int knownChars = 0, unknownChars = 0, maxUnknownChars = wordMaxChars - presentChars.size();
        BitSet visited = new BitSet(language.letters());
        Map<Character, AtomicInteger> charCount = new HashMap<>(WORD_LENGTH);
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (greenChars[i] != 0 && greenChars[i] != c) return false;
            if (nonPresentChars.get(c - language.firstLetter())) return false;
            CharInfo charInfo = presentChars.get(c);
            if (charInfo == null) {
                if (++unknownChars > maxUnknownChars) {
                    return false;
                }

            } else {
                if (charInfo.wrongPositions.get(i)) return false;

                // Check how many chars were found in this word
                int chrI = c - language.firstLetter();
                if (!visited.get(chrI)) {
                    visited.set(chrI);
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
        for (Iterator<Map.Entry<Character, AtomicInteger>> iter = charCount.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<Character, AtomicInteger> charCountEntry = iter.next();
            CharInfo charInfo = presentChars.get(charCountEntry.getKey());
            if (charInfo.count == 2 && charCountEntry.getValue().get() != 2) {
                return false;
            }
        }
        return knownChars == presentChars.size();
    }

    public void guessWordAttempt(String word, WordResult wordResult) {
        BitSet skipChars = new BitSet(WORD_LENGTH);
        for (int i = 0; i < WORD_LENGTH; i++) {
            char c = word.charAt(i);
            CompetitionInterface.CharResult charResult = wordResult.result()[i];
            if (skipChars.get(i)) {
                continue;
            }
            int pos2 = findNextChar(word, i, c);
            if (pos2 == -1) {
                switch (charResult) {
                    case black -> nonPresentChars.set(c - language.firstLetter());
                    case green,yellow -> addCharInfo(c, i, charResult);
                }
            } else {
                skipChars.set(pos2);

                int pos3 = findNextChar(word, pos2, c);
                if (pos3 != -1) {
                    log.warn("Unexpected same char count {}/{} in word {}", pos3, c, word);
                }


                CompetitionInterface.CharResult charResult2 = wordResult.result()[pos2];
                CompetitionInterface.CharResult max = Utils.max(charResult, charResult2);
                if (max == CompetitionInterface.CharResult.black) {
                    nonPresentChars.set(c - language.firstLetter());
                    continue;
                }
                CompetitionInterface.CharResult min = Utils.min(charResult, charResult2);
                if (min == CompetitionInterface.CharResult.black) {
                    // One letter
                    int presPos = charResult != CompetitionInterface.CharResult.black ? i : pos2;
                    CharInfo charInfo = addCharInfo(c, presPos, max);
                    charInfo.setSingle();
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

    private int findNextChar(String word, int pos, char c) {
        if (pos == WORD_LENGTH -1) return -1;
        for (int i = pos+1; i < WORD_LENGTH; i++) {
            if (word.charAt(i) == c) return i;
        }
        return -1;
    }

    private CharInfo addCharInfo(char chr, int pos, CompetitionInterface.CharResult result) {
        return presentChars.computeIfAbsent(chr, c -> new CharInfo(chr))
                .set(result, pos);
    }


    public class CharInfo {
        private final BitSet correctPositions = new BitSet(WORD_LENGTH);
        private final BitSet wrongPositions = new BitSet(WORD_LENGTH);
        private int count = -1;
        private char c;

        public CharInfo(char c) {
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

        public void setSingle() {
            if (count == -1) {
                count = 1;
            } else if (count == 2) {
                log.error("Was 2, now set to 1: {}", c);
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

        public char getChar() {
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
