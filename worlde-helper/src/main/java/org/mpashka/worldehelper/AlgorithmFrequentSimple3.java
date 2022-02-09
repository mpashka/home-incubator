package org.mpashka.worldehelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.mpashka.worldehelper.Utils.WORD_LENGTH;
import static org.mpashka.worldehelper.Utils.contains;

/**
 * Almost same as {@link AlgorithmFrequent} but use any possible abracadabra words
 * Take pos into account
 */
public class AlgorithmFrequentSimple3 implements AlgorithmInterface {
    private static final Logger log = LoggerFactory.getLogger(AlgorithmFrequentSimple3.class);

    private Language language;
    private String[] words;
    private List<String> conformedWords;
    private WordChecker wordChecker;

    @Override
    public void init(Language language, String[] words, WordChecker wordChecker) {
        this.language = language;
        this.words = words;
        this.wordChecker = wordChecker;
    }

    @Override
    public void nextRound() {
        conformedWords = Arrays.asList(words);
    }

    @Override
    public String nextWord(int attempt) {
        conformedWords = conformedWords.stream()
                .filter(w -> wordChecker.conform(w))
                .collect(Collectors.toList());
        if (conformedWords.isEmpty()) {
            log.error("Conformed words are empty");
        }
        if (conformedWords.size() < 3) {
            return conformedWords.iterator().next();
        }

        boolean notFoundChars = wordChecker.getWordMaxChars() > wordChecker.getPresentChars().size();

        byte[] wordChars = new byte[WORD_LENGTH];
        if (notFoundChars) {
            log.trace("    Guess word. From: {}. Known chars: {}, total chars: {}", conformedWords.size(), wordChecker.getPresentChars().size(), wordChecker.getWordMaxChars());
            createCharsWordSets(wordChars);

        } else {
            // All chars are found but order is not. Sure this can't be. Put doubles to find order
            log.info("        All chars are found but order is not. Words {}/{}.", conformedWords.size(), conformedWords);

            WordChecker.CharInfo[] chars = wordChecker.getPresentChars().values().toArray(WordChecker.CharInfo[]::new);
            int charIdx = 0;
            for (int i = 0; i < WORD_LENGTH; i++) {
                byte c;
                while (true) {
                    WordChecker.CharInfo charInfo = chars[charIdx++ % chars.length];
                    if (charInfo.getCorrectPositions().get(i) || charInfo.isPosAllKnown()) continue;
                    c = charInfo.getChar();
                    break;
                }
                wordChars[i] = c;
            }
        }
        return language.word(wordChars);
    }

    private void createCharsWordSets(byte[] wordChars) {
        // Some chars are not known
        int[] freq = new int[language.letters()];
        int[][] pos = new int[language.letters()][WORD_LENGTH];
        for (String w : conformedWords) {
            for (int i = 0; i < WORD_LENGTH; i++) {
                byte c = language.idx(w.charAt(i));
                freq[c]++;
                pos[c][i]++;
            }
        }

        for (int i = 0; i < WORD_LENGTH; i++) {
            byte c = mostFreqCharIdx(freq, idx -> wordChecker.getPresentChars().get(idx) == null && !contains(idx, wordChars, 0, WORD_LENGTH));
            int idx = findIndex(pos[c], wordChars);
            wordChars[idx] = c;
        }
    }

    private int findIndex(int[] pos, byte[] wordChars) {
        int maxCount = -1, maxPos = -1;
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (wordChars[i] == 0 && pos[i] > maxCount) {
                maxCount = pos[i];
                maxPos = i;
            }
        }
        return maxPos;
    }

    private byte mostFreqCharIdx(int[] charsFreq, Predicate<Byte> filter) {
        byte mostFreqChar = -1;
        int maxFreq = -1;
        for (byte i = 0; i < charsFreq.length; i++) {
            int freq = charsFreq[i];
            if (!filter.test(i)) {
                continue;
            }
            if (freq > maxFreq) {
                mostFreqChar = i;
                maxFreq = freq;
            }
        }
        return mostFreqChar;
    }

}
