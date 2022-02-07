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

import static org.mpashka.worldehelper.CompetitionInterface.WordResult;
import static org.mpashka.worldehelper.Utils.WORD_LENGTH;
import static org.mpashka.worldehelper.Utils.contains;

/**
 * Check char frequency, create word from most frequent unknown chars
 */
public class AlgorithmFrequentSimple {
    private static final Logger log = LoggerFactory.getLogger(AlgorithmFrequentSimple.class);

    private CompetitionInterface.SessionId session;
    private Language language;
    private CompetitionInterface competition;
    private String[] words;
    private List<String> conformedWords;
    private WordChecker wordChecker;

    public void run(Language language, CompetitionInterface competition, CompetitionInterface.SessionId session) {
        this.language = language;
        this.competition = competition;
        this.session = session;
        init(language);

        while (competition.nextWord(session)) {
            guessWord();
        }
    }

    private void init(Language language) {
        words = competition.getWordList();
        wordChecker = new WordChecker(language);
    }

    private void guessWord() {
        conformedWords = Arrays.asList(words);
        wordChecker.clear();

        WordResult wordResult;
        do {
            String word = findMostFreqWord();
            wordResult = competition.checkWord(session, word);
            wordChecker.guessWordAttempt(word, wordResult);
        } while (!wordResult.fin());
    }

    private String findMostFreqWord() {
        conformedWords = conformedWords.stream()
                .filter(w -> wordChecker.conform(w))
                .collect(Collectors.toList());
        if (conformedWords.isEmpty()) {
            log.error("Conformed words are empty");
        }
        if (conformedWords.size() < 4) return conformedWords.iterator().next();

        boolean notFoundChars = wordChecker.getWordMaxChars() > wordChecker.getPresentChars().size();

        byte[] wordChars = new byte[WORD_LENGTH];
        if (notFoundChars) {
            log.trace("    Guess word. From: {}. Known chars: {}, total chars: {}", conformedWords.size(), wordChecker.getPresentChars().size(), wordChecker.getWordMaxChars());
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

            for (int i = 0; i < WORD_LENGTH; i++) {
                int i1 = i;
                byte c = mostFreqCharIdx(freq, idx -> wordChecker.getPresentChars().get(idx) == null && !contains(idx, wordChars, 0, i1));
                wordChars[i] = c;
                freq = clearChar(freq, c);
            }
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
