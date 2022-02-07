package org.mpashka.worldehelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.mpashka.worldehelper.CompetitionInterface.WordResult;
import static org.mpashka.worldehelper.Utils.WORD_LENGTH;

/**
 * Use abracadabra words
 */
public class AlgorithmFrequent {
    private static final Logger log = LoggerFactory.getLogger(AlgorithmFrequent.class);

    /**
     * 0 means we don't take into account char presence - only char frequency
     * 0.5 means we take median frequency and use it for each char
     */
    private static final double CHAR_FREQ_K = 0.3;
    private static final int SINGLE_CHAR_SCORE = 0;

    /**
     * 0 we don't care if we don't know position
     * 1 we add same as above score
     */
    private static final double CHAR_POSITION_K = 0.2;

    private CompetitionInterface.SessionId session;
    private Language language;
    private CompetitionInterface competition;
    private String[] words;
    private Collection<String> conformedWords;
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
        competition.nextWord(session);

        WordResult wordResult;
        do {
            String word = findMostFreqWord();
            wordResult = competition.checkWord(session, word);
            wordChecker.guessWordAttempt(word, wordResult);
        } while (!wordResult.fin());
    }

    private String findMostFreqWord() {
        int[] freq = new int[WORD_LENGTH];
        conformedWords = conformedWords.stream()
                .filter(w -> wordChecker.conform(w))
                .collect(Collectors.toList());
        conformedWords.forEach(w -> {
            for (int i = 0; i < w.length(); i++) {
                int c = w.charAt(i) - language.firstLetter();
                freq[c]++;
            }
        });
//        log.debug("Found words: {}", conformedWords.size());

        Map<Character, CharInfo> chars = new HashMap<>();
        SortedSet<CharInfo> charsSet = new TreeSet<>();
        for (int i = 0; i < freq.length; i++) {
            char chr = (char) (i + language.firstLetter());
            if (!wordChecker.getNonPresentChars().get(i)) {
                CharInfo charInfo = new CharInfo(chr, freq[i]);
                chars.put(chr, charInfo);
                charsSet.add(charInfo);
            }
        }
//        log.debug("10 most popular letters:");
//        charsSet.stream().limit(10).forEach(c -> log.debug("    {} = {}", c.c, c.freq));
//        log.debug("10 less popular letters:");
        ArrayList<CharInfo> reverseChars = new ArrayList<>(charsSet);
        int singleCharScore = Math.max(reverseChars.get((int) Math.round(reverseChars.size() * CHAR_FREQ_K)).freq, SINGLE_CHAR_SCORE);

/*
        Collections.reverse(reverseChars);
        reverseChars.stream().limit(10).forEach(c -> log.debug("    {} = {}", c.c, c.freq));

        log.debug("Single char score: {}", singleCharScore);
*/

        SortedSet<WordInfo> wordsSorted = conformedWords/*words*/
                .stream()
                .map(w -> new WordInfo(w, singleCharScore, chars))
                .collect(Collectors.toCollection(TreeSet::new));
//        log.debug("10 most popular words:");
//        wordsSorted.stream().limit(10).forEach(w -> log.debug("    {} = {}", w.word, w.score));
        return wordsSorted.iterator().next().word;
    }

    private static class CharInfo implements Comparable<CharInfo> {
        private char c;
        private int freq;

        public CharInfo(char c, int freq) {
            this.c = c;
            this.freq = freq;
        }

        @Override
        public int compareTo(CharInfo o) {
            int freqCompare = Integer.compare(o.freq, freq);
            return freqCompare != 0 ? freqCompare : Character.compare(c, o.c);
        }
    }

    private class WordInfo implements Comparable<WordInfo> {
        private String word;
        private int score;

        public WordInfo(String word, int singleCharScore, Map<Character, CharInfo> chars) {
            this.word = word;
            int posScore = Math.max((int) Math.round(singleCharScore * CHAR_POSITION_K), 1);
            BitSet visited = new BitSet(language.letters());
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                int chrI = c - language.firstLetter();
                if (visited.get(chrI)) {
                    continue;
                }
                visited.set(chrI);
                WordChecker.CharInfo visitedCharInfo = wordChecker.getPresentChars().get(c);

                // Char was already checked
                if (visitedCharInfo != null) {
                    // Char either not present or pos found or this pos was already checked for this char - score == 0
                    if (!visitedCharInfo.getCorrectPositions().get(i) || visitedCharInfo.getWrongPositions().get(i)) {
                        continue;
                    }
                    if (wordChecker.getGreenChars()[i] == c) {
                        // If position is known then add 2 to give correct word some advantage
                        score += 2*posScore;
                    } else {
                        // Char was found but position is still unknown so just in case add this to check position
                        score += posScore;
                    }
                    continue;
                }

                score += singleCharScore;

                CharInfo charInfo = chars.get(c);
                if (charInfo == null) {
                    // This can happen after filtering
                    continue;
                }
                score += charInfo.freq;
            }
        }

        @Override
        public int compareTo(WordInfo o) {
            int scoreCompare = Integer.compare(o.score, score);
            return scoreCompare != 0 ? scoreCompare : word.compareTo(o.word);
        }
    }


}
