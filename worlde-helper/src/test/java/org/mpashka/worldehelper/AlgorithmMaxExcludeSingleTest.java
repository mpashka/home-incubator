package org.mpashka.worldehelper;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.BitSet;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mpashka.worldehelper.AlgorithmMaxExcludeSingle.*;

public class AlgorithmMaxExcludeSingleTest {
    private static final Logger log = LoggerFactory.getLogger(AlgorithmMaxExcludeSingleTest.class);

    @Test
    public void test() {
        AlgorithmMaxExcludeSingle a = new AlgorithmMaxExcludeSingle();
        WordChecker wordChecker = new WordChecker(Language.eng);
        String[] words = {
                "abcde",
                "abcdf",
                "abcfe",
                "abfde",
                "afcde",
                "fbcde",
                "abcdz",
                "abcze",
                "abzde",
                "azcde",
                "zbcde",
        };
        a.init(Language.eng, words, wordChecker);

        Function<Collection<SelectResult>, Double> prob = col -> col.stream().mapToDouble(SelectResult::probability).sum();
        Function<Collection<SelectResult>, Double> size = col -> col.stream().mapToDouble(r -> r.probability() * r.size()).sum() / col.size();

        BitSet conformedWords = new BitSet(words.length);
        conformedWords.set(0, words.length);
        Collection<SelectResult> res1 = a.processLetter("abcde", 0, 1, conformedWords, "d");
        log.debug("abcde: {}, {}, {}", prob.apply(res1), size.apply(res1), res1);
        Collection<SelectResult> res2 = a.processLetter("abcdg", 0, 1, conformedWords, "d");
        log.debug("abcdf: {}, {}, {}", prob.apply(res2), size.apply(res2), res2);
        Collection<SelectResult> res3 = a.processLetter("abcgh", 0, 1, conformedWords, "d");
        log.debug("abcgh: {}, {}, {}", prob.apply(res3), size.apply(res3), res3);
        Collection<SelectResult> res4 = a.processLetter("abghj", 0, 1, conformedWords, "d");
        log.debug("abghj: {}, {}, {}", prob.apply(res4), size.apply(res4), res4);
        Collection<SelectResult> res5 = a.processLetter("aghjk", 0, 1, conformedWords, "d");
        log.debug("aghjk: {}, {}, {}", prob.apply(res5), size.apply(res5), res5);
        Collection<SelectResult> res6 = a.processLetter("ghjkl", 0, 1, conformedWords, "d");
        log.debug("ghjkl: {}, {}, {}", prob.apply(res6), size.apply(res6), res6);
        Collection<SelectResult> res7 = a.processLetter("cajkl", 0, 1, conformedWords, "d");
        log.debug("cajkl: {}, {}, {}", prob.apply(res7), size.apply(res7), res7);
        Collection<SelectResult> resN = a.processLetter("yuiop", 0, 1, conformedWords, "d");
        log.debug("yuiop: {}, {}, {}", prob.apply(resN), size.apply(resN), resN);
    }


    @Test
    public void test2() throws IOException {
        AlgorithmMaxExcludeSingle a = new AlgorithmMaxExcludeSingle();
        Language language = Language.rus;
        WordChecker wordChecker = new WordChecker(language);
        String wordsFile = language.fileName();
        BitSet conformedWords;
        try (Stream<String> wordsStream = Files.lines(Paths.get(wordsFile))
                .map(String::toLowerCase)
                .filter(w -> language.isCorrect(w))) {
            String[] words = wordsStream.toArray(String[]::new);
            a.init(language, words, wordChecker);
            conformedWords = new BitSet(words.length);
            conformedWords.set(0, words.length);
        }

//        Function<Collection<SelectResult>, Double> prob = col -> col.stream().mapToDouble(SelectResult::probability).sum();
        Function<Collection<SelectResult>, Double> size = col -> col.stream().mapToDouble(r -> r.probability() * r.size()).sum() / col.size();

        Collection<SelectResult> res1 = a.processLetter("аллах", 0, 1, conformedWords, "d");
        Collection<SelectResult> res2 = res1.stream().filter(r -> r.size() > 0).toList();
        log.debug("abcde: {}, {}", size.apply(res1), res1);
        log.debug("abcde: {}, {}", size.apply(res2), res2);
    }
}
