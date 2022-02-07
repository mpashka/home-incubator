package org.mpashka.worldehelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.mpashka.worldehelper.Utils.*;

public class Competition implements CompetitionInterface {
    private static final Logger log = LoggerFactory.getLogger(Competition.class);

    private Language language;
    private int firstLetter;
    private int lastLetter;
    private int letters;

    private Map<String, AlgorithmSession> sessions = new HashMap<>();
    private String[] wordList;

    public Competition(Language language) {
        this.language = language;
        firstLetter = language.firstLetter();
        lastLetter = language.lastLetter();
        letters = lastLetter - firstLetter + 1;
    }

    Competition initWords() throws IOException {
        String wordsFile = language.fileName();
        try (Stream<String> wordsStream = Files.lines(Paths.get(wordsFile))
                .map(String::toLowerCase)
                .filter(this::checkWordValid)) {
            wordList = wordsStream.toArray(String[]::new);
        }
        log.info("Lang {}, {} words", language.name(), wordList.length);
        return this;
    }

    void setWordList(String[] wordList) {
        this.wordList = wordList;
    }

    private boolean checkWordValid(String word) {
        if (word.length() != WORD_LENGTH) return false;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (c < firstLetter || c > lastLetter) return false;
        }
        return true;
    }

    @Override
    public String[] getWordList() {
        return wordList;
    }

    @Override
    public SessionId session() {
        SessionId sessionId = new SessionId(Utils.randomString(10));
        AlgorithmSession algorithmSession = new AlgorithmSession(wordList.length);
        sessions.put(sessionId.sessionId(), algorithmSession);
        return sessionId;
    }

    @Override
    public boolean nextWord(SessionId sessionId) {
        AlgorithmSession session = session(sessionId);
        if (!session.hasNext()) {
            return false;
        }

        String expectedWord = wordList[session.nextIndex()];
        log.trace("Expected word: {}", expectedWord);
        Map<Character, BitSet> doubleChars = wordChars(expectedWord);
        session.setMyWord(new MyWordSession(doubleChars));
        return true;
    }

    @Override
    public WordResult checkWord(SessionId sessionId, String word) {
        AlgorithmSession session = session(sessionId);

//        String expectedWord = wordList[Integer.parseInt(sessionId.sessionId())];
        word = word.toLowerCase();
        if (!checkWordValid(word)) {
            throw new IllegalArgumentException("Unexpected word '" + word + "'");
        }
        MyWordSession expectedWord = session.getMyWord();

        Map<Character, BitSet> wordChars = wordChars(word);
        Map<Character, BitSet> expectedChars = expectedWord.getChars();
        int found = 0;
        CharResult[] result = new CharResult[WORD_LENGTH];
        for (int i = 0; i < WORD_LENGTH; i++) {
            char wordChar = word.charAt(i);
            BitSet expectedCharPos = expectedChars.get(wordChar);
            BitSet charPos = wordChars.get(wordChar);
            CharResult charResult;
            if (expectedCharPos == null) {
                charResult = CharResult.black;
            } else if (expectedCharPos.get(i)) {
                charResult = CharResult.green;
            } else {
                int expectedCharCount = expectedCharPos.cardinality();
                int charCount = charPos.cardinality();
                if (expectedCharCount >= charCount) {
                    charResult = CharResult.yellow;
                } else {
                    BitSet samePos = (BitSet) charPos.clone();
                    samePos.and(expectedCharPos);
                    int samePosCount = samePos.cardinality();
                    int yellowChars = expectedCharCount - samePosCount;
                    BitSet notSamePos = (BitSet) charPos.clone();
                    notSamePos.andNot(expectedCharPos);
                    if (i < WORD_LENGTH -1) {
                        notSamePos.clear(i, WORD_LENGTH -1);
                    }
                    int charNum = notSamePos.cardinality();
                    charResult = charNum <= yellowChars ? CharResult.yellow : CharResult.black;
                }
            }
            if (charResult == CharResult.green) found++;
            result[i] = charResult;
        }
        boolean fin, allGreen = fin = found == WORD_LENGTH;
        expectedWord.addAnswer();
        if (allGreen) {
            session.addResult(expectedWord.getAnswers());
        } else if (expectedWord.getAnswers() >= MAX_ANSWERS) {
            fin = true;
            session.addNotFound();
        }
        if (fin) {
            session.setMyWord(null);
        }
        log.trace("    Attempt: {} -> {}", word, new String(Arrays.stream(result).mapToInt(r -> r.name().charAt(0)).toArray(), 0, WORD_LENGTH));
        return new WordResult(fin, result);
    }

    @Override
    public CompetitionResult result(SessionId sessionId) {
        AlgorithmSession session = session(sessionId);
        sessions.remove(sessionId.sessionId());
        return session.result();
    }

    private AlgorithmSession session(SessionId sessionId) {
        return sessions.computeIfAbsent(sessionId.sessionId(), id -> {throw new RuntimeException("Session id not found " + id);});
    }

    private class AlgorithmSession {
        private int total;
        private int index;
        private int[] attempts = new int[MAX_ANSWERS];
        private int notFound;
        private long sum;
        private int count;

        private MyWordSession myWord;

        public AlgorithmSession(int total) {
            this.total = total;
        }

        public boolean hasNext() {
            return index < total;
        }

        public int nextIndex() {
            return index++;
        }

        public MyWordSession getMyWord() {
            return myWord;
        }

        public void setMyWord(MyWordSession myWord) {
            this.myWord = myWord;
        }

        public void addResult(int attempts) {
            this.attempts[attempts - 1]++;
            count++;
            sum += attempts;
        }

        public void addNotFound() {
            notFound++;
//            count++;
//            sum += 7;
        }

        public CompetitionResult result() {
            return new CompetitionResult((double) sum / count, count, attempts, notFound);
        }
    }

    private class MyWordSession {
        private Map<Character, BitSet> chars;
        private int answers;

        public MyWordSession(Map<Character, BitSet> chars) {
            this.chars = chars;
        }

        public Map<Character, BitSet> getChars() {
            return chars;
        }

        public void addAnswer() {
            answers++;
        }

        public int getAnswers() {
            return answers;
        }
    }
}
