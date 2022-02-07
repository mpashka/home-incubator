package org.mpashka.worldehelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mpashka.worldehelper.Utils.WORD_LENGTH;

/**
 * Find word that excludes maximum possible variants in either case
 * Single means double chars are not taken into account
 */
public class AlgorithmMaxExcludeSingle {
    private static final Logger log = LoggerFactory.getLogger(AlgorithmMaxExcludeSingle.class);

    private CompetitionInterface.SessionId session;
    private Language language;
    private CompetitionInterface competition;
    private String[] words;
    private BitSet conformedWords;
    private WordChecker wordChecker;
    private CharWordSet[] charsStats;

    public void run(Language language, CompetitionInterface competition, CompetitionInterface.SessionId session) {
        this.language = language;
        this.competition = competition;
        this.session = session;
        init();

        while (competition.nextWord(session)) {
            guessWord();
        }
    }

    private void init() {
        words = competition.getWordList();
        wordChecker = new WordChecker(language);

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

    private void guessWord() {
        conformedWords = new BitSet(words.length);
        conformedWords.set(0, words.length);
        wordChecker.clear();

        CompetitionInterface.WordResult wordResult;
        do {
            String word = findMostFreqWord();
            wordResult = competition.checkWord(session, word);
            wordChecker.guessWordAttempt(word, wordResult);
        } while (!wordResult.fin());
    }

    private String findMostFreqWord() {
        for (int i = 0; i < words.length; i++) {
            if (!conformedWords.get(i)) continue;
            String w = words[i];
            if (!wordChecker.conform(w)) {
                conformedWords.clear(i);
            }
        }
        if (conformedWords.isEmpty()) {
            log.error("Conformed words are empty");
        }
        if (conformedWords.size() < 3) {
            if (conformedWords.size() > 1) log.debug("50%");
            return words[conformedWords.nextSetBit(0)];
        }

        for (int wordIdx = 0; wordIdx < words.length; wordIdx++) {
            String w = words[wordIdx];

            BitSet inWordSet = (BitSet) conformedWords.clone();
            BitSet greenWordSet = (BitSet) inWordSet.clone();
            BitSet yellowWordSet = (BitSet) inWordSet.clone();
            BitSet blackWordSet = (BitSet) inWordSet.clone();
            for (int i = 0; i < WORD_LENGTH; i++) {
                byte c = language.idx(w.charAt(i));
                greenWordSet.and(charsStats[c].greenSet[i]);
                yellowWordSet.and(charsStats[c].yellowSet[i]);
                blackWordSet.and(charsStats[c].blackSet);

                double greenProbability = (double) greenWordSet.cardinality() / inWordSet.cardinality();
                double yellowProbability = (double) yellowWordSet.cardinality() / inWordSet.cardinality();
                double blackProbability = (double) blackWordSet.cardinality() / inWordSet.cardinality();


            }
        }
    }

//    static class

    static class CharWordSet {
        private BitSet[] greenSet;
        private BitSet blackSet;
        private BitSet[] yellowSet;

        public CharWordSet(int wordsCount) {
            greenSet = IntStream.range(0, WORD_LENGTH).mapToObj(i -> new BitSet(wordsCount)).toArray(BitSet[]::new);
            blackSet = new BitSet(wordsCount);
            yellowSet = IntStream.range(0, WORD_LENGTH).mapToObj(i -> new BitSet(wordsCount)).toArray(BitSet[]::new);
        }


    }
}
