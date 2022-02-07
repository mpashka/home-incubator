package org.mpashka.worldehelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CompetitionApp {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws IOException {
        Language language = Language.rus;

        Competition competition = new Competition(language).initWords();

        CompetitionInterface.SessionId session = competition.session();
        AlgorithmFrequentSimple algorithm = new AlgorithmFrequentSimple();
        algorithm.run(language, competition, session);
        CompetitionInterface.CompetitionResult result = competition.result(session);
        printResult(competition, algorithm, result);
    }

    private static void printResult(Competition competition, Object algorithm, CompetitionInterface.CompetitionResult result) {
        log.info("Algo: {}, Words: {}, Score: {}, Count: {}, Not found: {}", algorithm.toString(), competition.getWordList().length, result.score(), result.count(), result.notFound());
        for (int i = 0; i < result.attempts().length; i++) {
            log.info("    Answers[{}]: {}", i+1, result.attempts()[i]);
        }
    }
}
