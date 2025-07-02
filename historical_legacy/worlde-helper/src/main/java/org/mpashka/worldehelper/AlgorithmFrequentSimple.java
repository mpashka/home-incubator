package org.mpashka.worldehelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mpashka.worldehelper.Utils.WORD_LENGTH;
import static org.mpashka.worldehelper.Utils.contains;

/**
 * Almost same as {@link AlgorithmFrequent} but use any possible abracadabra words
 */
public class AlgorithmFrequentSimple implements AlgorithmInterface {
    private static final Logger log = LoggerFactory.getLogger(AlgorithmFrequentSimple.class);

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
        BitSet[] freq = IntStream.range(0, language.letters()).mapToObj(i -> new BitSet(conformedWords.size())).toArray(BitSet[]::new);
        for (ListIterator<String> iter = conformedWords.listIterator(); iter.hasNext(); ) {
            int wordIdx = iter.nextIndex();
            String w = iter.next();
            for (int i = 0; i < WORD_LENGTH; i++) {
                byte c = language.idx(w.charAt(i));
                freq[c].set(wordIdx);
            }
        }
//        int[] freqCount = Arrays.stream(freq).mapToInt(BitSet::cardinality).toArray();

        for (int i = 0; i < wordChars.length; i++) {
            int i1 = i;
            byte c = mostFreqCharIdx(freq, idx -> wordChecker.getPresentChars().get(idx) == null && !contains(idx, wordChars, 0, i1));
            wordChars[i] = c;
            freq = clearChar(freq, c);
        }
    }

    private byte mostFreqCharIdx(BitSet[] charsFreq, Predicate<Byte> filter) {
        byte mostFreqChar = -1;
        int maxCardinality = -1;
        for (byte i = 0; i < charsFreq.length; i++) {
            BitSet freq = charsFreq[i];
            if (freq == null || !filter.test(i)) {
                continue;
            }
            int cardinality = freq.cardinality();
            if (cardinality > maxCardinality) {
                mostFreqChar = i;
                maxCardinality = cardinality;
            }
        }
        return mostFreqChar;
    }

    private BitSet[] clearChar(BitSet[] charsFreq, byte mostFreqChar) {
        BitSet maxFreq = charsFreq[mostFreqChar];
        charsFreq[mostFreqChar] = null;
        for (BitSet bitSet : charsFreq) {
            if (bitSet != null) {
                bitSet.andNot(maxFreq);
            }
        }
        return charsFreq;
    }
}
