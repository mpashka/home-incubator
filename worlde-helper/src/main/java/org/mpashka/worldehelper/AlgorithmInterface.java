package org.mpashka.worldehelper;

public interface AlgorithmInterface {
    void init(Language language, String[] words, WordChecker wordChecker);

    void nextRound();

    String nextWord(int attempt);
}
