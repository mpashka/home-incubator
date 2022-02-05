package org.mpashka.worldehelper;

public interface CompetitionInterface {
    String[] getWordList();
    SessionId session();
    boolean nextWord(SessionId sessionId);
    WordResult checkWord(SessionId sessionId, String word);
    CompetitionResult result(SessionId sessionId);

    record CompetitionResult(double score, int[] attempts, int notFound) {
    }

    record SessionId(String sessionId) {
    }

    record WordResult(boolean fin, CharResult[] result) {
    }

    enum CharResult {
        green, yellow, black
    }
}
