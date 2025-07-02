package org.mpashka.worldehelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.mpashka.worldehelper.CompetitionInterface.*;

public class AlgorithmExecutor {
    private static final Logger log = LoggerFactory.getLogger(AlgorithmExecutor.class);

    private Language language;
    private AlgorithmInterface algorithm;
    private SessionId session;
    private CompetitionInterface competition;
    private WordChecker wordChecker;
    private Map<Answers, String> answersCache;

    public AlgorithmExecutor(Language language, AlgorithmInterface algorithm, CompetitionInterface competition, SessionId session) {
        this.language = language;
        this.algorithm = algorithm;
        this.session = session;
        this.competition = competition;
        this.wordChecker = new WordChecker(language);
    }

    public void run() {
        String[] words = competition.getWordList();
        algorithm.init(language, words, wordChecker);
        this.answersCache = new HashMap<>(words.length * 3);

        takeFirstWord();

        while (competition.nextWord(session)) {
            guessWord();
        }
    }

    private void takeFirstWord() {
// катер,

        algorithm.nextRound();
        wordChecker.clear();
        String firstWord = algorithm.nextWord(1000);
        answersCache.put(new Answers(), firstWord);
        log.info("First word: {}", firstWord);

    }

    private void guessWord() {
        wordChecker.clear();
        algorithm.nextRound();

        Answers answers = new Answers();
        CompetitionInterface.WordResult wordResult;
        for (int i = 0; i < Utils.MAX_ANSWERS; i++) {
            String word = answersCache.get(answers);
            boolean cached = word != null;
            if (word == null) {
                word = algorithm.nextWord(i);
            }
            wordResult = competition.checkWord(session, word);
            if (!cached) {
                answersCache.put(answers, word);
            }
            answers = new Answers(answers, new Answer(word, wordResult.result()));
            wordChecker.guessWordAttempt(word, wordResult);
            if (wordResult.fin()) {
                return;
            }
        }
    }

    private static class Answers {
        private Answer[] answers;

        public Answers() {
            this.answers = new Answer[0];
        }

        public Answers(Answers prevAnswers, Answer answer) {
            if (prevAnswers != null) {
                answers = new Answer[prevAnswers.answers.length + 1];
                System.arraycopy(prevAnswers.answers, 0, answers, 0, prevAnswers.answers.length);
                answers[prevAnswers.answers.length] = answer;
            } else {
                answers = new Answer[]{answer};
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Answers answers = (Answers) o;
            return Arrays.equals(this.answers, answers.answers);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(answers);
        }
    }

    private static class Answer {
        private String word;
        private CharResult[] result;

        public Answer(String word, CharResult[] result) {
            this.word = word;
            this.result = result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Answer answer = (Answer) o;
            return word.equals(answer.word) && Arrays.equals(result, answer.result);
        }

        @Override
        public int hashCode() {
            int result1 = Objects.hash(word);
            result1 = 31 * result1 + Arrays.hashCode(result);
            return result1;
        }
    }
}