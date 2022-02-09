package org.mpashka.worldehelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static org.mpashka.worldehelper.Utils.WORD_LENGTH;

/**
 * Find word that excludes maximum possible variants in either case
 * Single means double chars are not taken into account
 */
public class AlgorithmMaxExcludeSingle implements AlgorithmInterface {
    private static final Logger log = LoggerFactory.getLogger(AlgorithmMaxExcludeSingle.class);

    private Language language;
    private String[] words;
    private BitSet conformedWords;
    private WordChecker wordChecker;
    private CharWordSet[] charsStats;

    public void init(Language language, String[] words, WordChecker wordChecker) {
        this.language = language;
        this.words = words;
        this.wordChecker = wordChecker;

        charsStats = IntStream.range(0, language.letters()).mapToObj(i -> new CharWordSet(words.length)).toArray(CharWordSet[]::new);
        for (int wordIdx = 0; wordIdx < words.length; wordIdx++) {
            String word = words[wordIdx];
            BitSet found = new BitSet(language.letters());

            for (int pos = 0; pos < WORD_LENGTH; pos++) {
                byte c = language.idx(word.charAt(pos));
                found.set(c);
                charsStats[c].greenSet[pos].set(wordIdx);

                for (int yPos = 0; yPos < WORD_LENGTH; yPos++) {
                    byte yC = language.idx(word.charAt(yPos));
                    if (yC != c) {
                        charsStats[c].yellowSet[yPos].set(wordIdx);
                    }
                }
            }

            for (byte c = 0; c < language.letters(); c++) {
                if (!found.get(c)) {
                    charsStats[c].blackSet.set(wordIdx);
                }
            }
        }
    }

    @Override
    public void nextRound() {
        conformedWords = new BitSet(words.length);
        conformedWords.set(0, words.length);
    }

    public String nextWord(int attempt) {
        for (int i = 0; i < words.length; i++) {
            if (!conformedWords.get(i)) continue;
            String w = words[i];
            if (!wordChecker.conform(w)) {
                conformedWords.clear(i);
            }
        }
        int conformedWordsCount = conformedWords.cardinality();
        if (conformedWordsCount == 0) {
            log.error("Conformed words are empty");
        }
        if (conformedWordsCount < 3) {
            if (conformedWordsCount > 1) log.debug("50%");
            return words[conformedWords.nextSetBit(0)];
        }

        List<WordResult> wordResults = new ArrayList<>();
        for (int wordIdx = 0; wordIdx < words.length; wordIdx++) {
            String w = words[wordIdx];
            BitSet inWordSet = (BitSet) conformedWords.clone();
            Collection<SelectResult> result = processLetter(w, 0, 1, inWordSet, "");
            double size = result.stream()
                    .filter(r -> r.probability() > 0)
                    .mapToDouble(r -> r.probability() * r.size())
                    .sum();
            wordResults.add(new WordResult(wordIdx, size));
        }

        // Take word with minimum fetchSize
        wordResults.sort(Comparator.comparingDouble(WordResult::fetchSize));
        WordResult wordResult = wordResults.get(0);
        return words[wordResult.idx()];
    }

    Collection<SelectResult> processLetter(String word, int nextIndex, double inProbability, BitSet inWordSet, String display) {
        int inCardinality = inWordSet.cardinality();
        if (nextIndex >= WORD_LENGTH) return Collections.singleton(new SelectResult(inProbability, inCardinality, display));
        if (inCardinality == 0) return Collections.emptySet();

        char cD = word.charAt(nextIndex);
        byte c = language.idx(cD);

        BitSet greenWordSet = (BitSet) inWordSet.clone();
        BitSet yellowWordSet = (BitSet) inWordSet.clone();
        BitSet blackWordSet = (BitSet) inWordSet.clone();

        greenWordSet.and(charsStats[c].greenSet[nextIndex]);
        yellowWordSet.and(charsStats[c].yellowSet[nextIndex]);
        blackWordSet.and(charsStats[c].blackSet);

        double greenProbability = (double) greenWordSet.cardinality() / inCardinality;
        double yellowProbability = (double) yellowWordSet.cardinality() / inCardinality;
        double blackProbability = (double) blackWordSet.cardinality() / inCardinality;

        List<SelectResult> outResults = new ArrayList<>();
        outResults.addAll(processLetter(word, nextIndex+1, inProbability*greenProbability, greenWordSet, "g:" + cD));
        outResults.addAll(processLetter(word, nextIndex+1, inProbability*yellowProbability, yellowWordSet, "y:" + cD));
        outResults.addAll(processLetter(word, nextIndex+1, inProbability*blackProbability, blackWordSet, "b:" + cD));

        return outResults;
    }

    record WordResult(int idx, double fetchSize) {
    }

    private static final NumberFormat percentFormat = NumberFormat.getPercentInstance();
    record SelectResult(double probability, int size, String display) {
        @Override
        public String toString() {
            return display + "/" + size + "/" + percentFormat.format(probability);
        }
    }

    /** Each instance per letter */
    static class CharWordSet {
        /** All words with this letter green in this pos */
        private BitSet[] greenSet;
        /** All words that don't contain this letter */
        private BitSet blackSet;
        /** All words with this letter yellow in this pos */
        private BitSet[] yellowSet;

        public CharWordSet(int wordsCount) {
            greenSet = IntStream.range(0, WORD_LENGTH).mapToObj(i -> new BitSet(wordsCount)).toArray(BitSet[]::new);
            blackSet = new BitSet(wordsCount);
            yellowSet = IntStream.range(0, WORD_LENGTH).mapToObj(i -> new BitSet(wordsCount)).toArray(BitSet[]::new);
        }
    }
}
