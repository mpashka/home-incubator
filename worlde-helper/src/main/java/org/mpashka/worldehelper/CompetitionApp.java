package org.mpashka.worldehelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CompetitionApp {
    public static void main(String[] args) throws IOException {
        Language language = Language.rus;

        Competition competition = new Competition(language).initWords();

        AlgorithmSimple algorithmSimple = new AlgorithmSimple();
        algorithmSimple.run(competition);

    }
}
