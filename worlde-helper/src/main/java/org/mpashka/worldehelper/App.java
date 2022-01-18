package org.mpashka.worldehelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * todo It's possible to get single char as pos and neg in one case - that means number of chars is 1
 *
 */
public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

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

    public Language language = Language.rus;
    public int firstLetter;
    public int lastLetter;
    public int letters;
    private static final int NCHARS = 5;

    private Map<Character, VisitedCharInfo> visitedChars;
    private char[] knownChars = new char[NCHARS];
    private int foundCharsCount;

    public App(String[] args) {
        if (args.length > 0) {
            language = languages.get(args[0]);
        }
        firstLetter = language.firstLetter;
        lastLetter = language.lastLetter;
        letters = lastLetter - firstLetter + 1;
        charMap = createCharMap(language.charMapArr);

        if (args.length == 2) {
            CharVisitParser arg = new CharVisitParser(knownChars).parseArg(args[1]);
            this.visitedChars = arg.getVisitedChars();
            this.foundCharsCount = arg.getFoundCharsCount();
            log.debug("Visited chars: {}, found chars: {}", visitedChars.size(), foundCharsCount);
        } else {
            this.visitedChars = Collections.emptyMap();
            this.foundCharsCount = 0;
        }
    }

    private void analyze() throws IOException {
        int[] freq = new int[letters];
        Set<String> words = wordsSet(language.fileName);
        Set<String> foundWords = words.stream().filter(this::conformVisitedChars).collect(Collectors.toSet());
        foundWords.forEach(w -> {
            for (int i = 0; i < w.length(); i++) {
                int c = w.charAt(i) - firstLetter;
                freq[c]++;
            }
        });
        log.debug("Found words: {}", foundWords.size());
        if (foundWords.size() < 30) {
            foundWords.stream().limit(10).forEach(c -> log.debug("    {}", c));
        }

        Map<Character, CharInfo> chars = new HashMap<>();
        SortedSet<CharInfo> charsSet = new TreeSet<>();
        for (int i = 0; i < freq.length; i++) {
            char chr = (char) (i + firstLetter);
            VisitedCharInfo visitedCharInfo = visitedChars.get(chr);
            if (visitedCharInfo == null || visitedCharInfo.present) {
                CharInfo charInfo = new CharInfo(chr, freq[i]);
                chars.put(chr, charInfo);
                charsSet.add(charInfo);
            }
        }
        log.debug("10 most popular letters:");
        charsSet.stream().limit(10).forEach(c -> log.debug("    {} = {}", c.c, c.freq));
        log.debug("10 less popular letters:");
        ArrayList<CharInfo> reverseChars = new ArrayList<>(charsSet);
        int singleCharScore = Math.max(reverseChars.get((int) Math.round(reverseChars.size() * CHAR_FREQ_K)).freq, SINGLE_CHAR_SCORE);

        Collections.reverse(reverseChars);
        reverseChars.stream().limit(10).forEach(c -> log.debug("    {} = {}", c.c, c.freq));

        log.debug("Single char score: {}", singleCharScore);

        SortedSet<WordInfo> wordsSorted = foundWords/*words*/
                .stream()
                .map(w -> new WordInfo(w, singleCharScore, chars))
                .collect(Collectors.toCollection(TreeSet::new));
        log.debug("10 most popular words:");
        wordsSorted.stream().limit(10).forEach(w -> log.debug("    {} = {}", w.word, w.score));
    }

    private boolean conformVisitedChars(String word) {
        if (visitedChars.isEmpty()) return true;
        int foundChars = foundCharsCount;
        int notfoundChars = NCHARS - foundCharsCount;
        BitSet visited = new BitSet(letters);
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (knownChars[i] != 0 && knownChars[i] != c) return false;
            VisitedCharInfo visitedCharInfo = visitedChars.get(c);
            if (visitedCharInfo == null) {
                notfoundChars--;
                if (notfoundChars < 0) {
                    return false;
                }
                continue;

            } else {
                // Check how many chars were found in this word
                int chrI = c - firstLetter;
                if (!visited.get(chrI)) {
                    foundChars--;
                }
                visited.set(chrI);
            }

            if (!visitedCharInfo.present) return false;
            if (visitedCharInfo.wrongPositions.get(i)) return false;
        }
        return foundChars == 0;
    }

    public void saveWords(String inFile, String outName, Predicate<String> filter) throws IOException {
        try (Stream<String> words = wordsStream(inFile);
                PrintWriter out = new PrintWriter(new FileWriter(outName))
        ) {
            words.filter(filter).forEach(out::println);
        }
    }

    private Set<String> wordsSet(String inFile) throws IOException {
        try (Stream<String> wordsStream = wordsStream(inFile)) {
            return wordsStream.collect(Collectors.toSet());
        }
    }

    private Stream<String> wordsStream(String inFile) throws IOException {
        return Files.lines(Paths.get(inFile))
                .map(String::toLowerCase)
                .map(this::replaceSameGraphemes)
//                .map()
                ;
    }


    public static void main(String[] args) throws IOException {
        App app = new App(args);

/*
        app.saveWords(".in/f_out2_.txt", ".in/f_out3.txt", App::isCorrectWord);
//        app.saveWords(".in/f_out2_.txt", ".in/f_out3_wrong_char.txt", App::isWrongChar);
        app.saveWords(".in/f_out2_.txt", ".in/f_out3_incorrect.txt", w -> !isCorrectWord(w));
*/
        app.analyze();
    }


    public Map<Character, Character> charMap;

    private static Map<Character, Character> createCharMap(char[] charMapArr) {
        Map<Character, Character> charMap = new HashMap<>(charMapArr.length/2);
        for (int i = 0; i < charMapArr.length / 2; i++) {
            charMap.put(charMapArr[i*2], charMapArr[i*2 + 1]);
        }
        return charMap;
    }

    public String replaceSameGraphemes(String word) {
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (charMap.containsKey(c)) {
                char[] result = new char[word.length()];
                for (int j = 0; j < result.length; j++) {
                    char orig = word.charAt(j);
                    result[j] = charMap.getOrDefault(orig, orig);
                }
                return new String(result);
            }
        }
        return word;
    }

/*
    public static boolean isWrongChar(String word) {
        for (int i = 0; i < word.length(); i++) {
            int chr = word.charAt(i) - firstLetter;
            if (chr < 0 || chr >= letters) return true;
        }
        return false;
    }
*/

    public boolean isCorrectWord(String word) {
//        BitSet chars = new BitSet(letters);
        for (int i = 0; i < word.length(); i++) {
            int chr = word.charAt(i) - firstLetter;
            if (chr < 0 || chr >= letters) return false;
//            if (chr < 0 || chr >= letters || chars.get(chr)) return false;
//            chars.set(chr);
        }
        return true;
    }

    private class WordInfo implements Comparable<WordInfo> {
        private String word;
        private int score;

        public WordInfo(String word, int singleCharScore, Map<Character, CharInfo> chars) {
            this.word = word;
            int posScore = Math.max((int) Math.round(singleCharScore * CHAR_POSITION_K), 1);
            BitSet visited = new BitSet(letters);
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                int chrI = c - firstLetter;
                if (visited.get(chrI)) {
                    continue;
                }
                visited.set(chrI);
                VisitedCharInfo visitedCharInfo = visitedChars.get(c);
                // Char was already checked
                if (visitedCharInfo != null) {
                    // Char either not present or pos found or this pos was already checked for this char - score == 0
                    if (!visitedCharInfo.present || visitedCharInfo.wrongPositions.get(i)) {
                        continue;
                    }
                    if (knownChars[i] == c) {
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

    private static class CharVisitParser {
        private Map<Character, VisitedCharInfo> result = new HashMap<>();
        private char[] knownChars;
        private int pos = 0;
        private boolean error;
        private int foundCharsCount;

        private CharProcessor procNeg = c -> {
            VisitedCharInfo visitedCharInfo = result.get(c);
            if (visitedCharInfo != null && visitedCharInfo.present) {
                error = true;
                log.error("Char '{}' is present twice in pos and neg", c);
            }
            result.put(c, new VisitedCharInfo(c, false, -1));
        };

        private CharProcessor procUnpos;
        private CharProcessor procPos0 = c -> {
            if (result.get(c) == null) {
                result.put(c, new VisitedCharInfo(c, true, -1));
                foundCharsCount++;
            }
            knownChars[pos] = c;
        };

        private CharProcessor procPosSingle = c -> {
            procPos0.nextChar(c);
            procCurrent = procUnpos;
        };

        private CharProcessor procPos = c -> {
            if (c == '[') {
                procCurrent = procUnpos;
            } else if (c == '!') {
                procCurrent = procNeg;
            } else if (c == ' ' || c == '_') {
                pos++;
            } else {
                procPos0.nextChar(c);
                pos++;
            }
        };

        {
            procUnpos = c -> {
                if (c == '!') {
                    procCurrent = procPosSingle;
                } else if (c == ']') {
                    procCurrent = procPos;
                    pos++;
                } else {
                    VisitedCharInfo visitedCharInfo = result.get(c);
                    if (visitedCharInfo != null) {
                        visitedCharInfo.wrongPositions.set(pos);
                    } else {
                        result.put(c, new VisitedCharInfo(c, true, pos));
                        foundCharsCount++;
                    }
                }
            };
        }

        private CharProcessor procCurrent = procPos;

        public CharVisitParser(char[] knownChars) {
            this.knownChars = knownChars;
        }

        public CharVisitParser parseArg(String arg) {
            for (int i = 0; i < arg.length(); i++) {
                char chr = arg.charAt(i);
                procCurrent.nextChar(chr);
            }
            if (error) throw new IllegalArgumentException("Wrong ags");
            return this;
        }

        public Map<Character, VisitedCharInfo> getVisitedChars() {
            return result;
        }

        public int getFoundCharsCount() {
            return foundCharsCount;
        }

        interface CharProcessor {
            void nextChar(char c);
        }
    }

    private static class VisitedCharInfo {
        private char c;
        private boolean present;
        private final BitSet wrongPositions = new BitSet(NCHARS);

        public VisitedCharInfo(char c, boolean present, int wrongPosition) {
            this.c = c;
            this.present = present;
            if (present && wrongPosition >= 0) {
                this.wrongPositions.set(wrongPosition);
            }
        }
    }

    /**
     */
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


    private static final Map<String, Language> languages = new HashMap<>();

    private record Language(String name, char firstLetter, char lastLetter, char[] charMapArr, String fileName) {
        private Language {
            languages.put(name, this);
        }

        private static final Language rus = new Language("rus", 'а', 'я', new char[] {'a', 'а',
                'b', 'б',
                'c', 'с',
                'e', 'е',
                'h', 'н',
                'm', 'м',
                'k', 'к',
                'o', 'о',
                'p', 'р',
                't', 'т',
                'x', 'х',
        }, ".in/rus.txt");
        private static final Language eng = new Language("eng", 'a', 'z', new char[0], ".in/eng.txt");
    }
}
