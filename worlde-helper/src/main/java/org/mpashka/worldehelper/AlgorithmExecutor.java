package org.mpashka.worldehelper;

public class AlgorithmExecutor {
    private Language language;
    private AlgorithmInterface algorithm;
    private CompetitionInterface.SessionId session;
    private CompetitionInterface competition;
    private WordChecker wordChecker;

    public AlgorithmExecutor(Language language, AlgorithmInterface algorithm, CompetitionInterface competition, CompetitionInterface.SessionId session) {
        this.language = language;
        this.algorithm = algorithm;
        this.session = session;
        this.competition = competition;
        this.wordChecker = new WordChecker(language);
    }

    public void run() {
        algorithm.init(language, competition.getWordList(), wordChecker);
        while (competition.nextWord(session)) {
            guessWord();
        }
    }

    private void guessWord() {
        wordChecker.clear();
        algorithm.nextRound();

        CompetitionInterface.WordResult wordResult;
        do {
            String word = algorithm.nextWord();
            wordResult = competition.checkWord(session, word);
            wordChecker.guessWordAttempt(word, wordResult);
        } while (!wordResult.fin());
    }


}
