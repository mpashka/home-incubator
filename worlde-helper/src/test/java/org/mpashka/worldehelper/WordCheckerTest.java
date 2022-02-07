package org.mpashka.worldehelper;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mpashka.worldehelper.CompetitionInterface.*;
import static org.mpashka.worldehelper.CompetitionInterface.CharResult.*;

public class WordCheckerTest {

    @Test
    public void testWordChecker() {
        WordChecker wordChecker = new WordChecker(Language.eng);
        wordChecker.clear();
        // word - hello

        assertThat(wordChecker.conform("abcde"), is(true));

        wordChecker.guessWordAttempt("qwert", new WordResult(false, new CharResult[]{
                black,black,yellow,black,black,
        }));

        // black now: qwrt
        assertThat(wordChecker.conform("abcde"), is(true));
        assertThat(wordChecker.conform("abcqe"), is(false));
        assertThat(wordChecker.conform("hello"), is(true));

        wordChecker.guessWordAttempt("eldrr", new WordResult(false, new CharResult[]{
                yellow,yellow,black,black,black,
        }));
        // black now: qwrt+d
        assertThat(wordChecker.conform("hello"), is(true));
        assertThat(wordChecker.conform("abcde"), is(false));

        wordChecker.guessWordAttempt("eezll", new WordResult(false, new CharResult[]{
                black,green,black,green,yellow,
        }));
        // e:1, l: 2
        assertThat(wordChecker.conform("hello"), is(true));
        assertThat(wordChecker.conform("eello"), is(false));
        assertThat(wordChecker.conform("heloo"), is(false));
        assertThat(wordChecker.conform("heooo"), is(false));
        assertThat(wordChecker.conform("hhllo"), is(false));
    }

    @Test
    public void test2() {
        WordChecker wordChecker = new WordChecker(Language.rus);
        wordChecker.clear();
        // word - атаба
        wordChecker.guessWordAttempt("аоеиу", new WordResult(false, new CharResult[]{
                green,black,black,black,black,
        }));
        wordChecker.guessWordAttempt("крлтн", new WordResult(false, new CharResult[]{
                black,black,black,yellow,black,
        }));
        wordChecker.guessWordAttempt("бгмвп", new WordResult(false, new CharResult[]{
                yellow,black,black,black,black,
        }));
        wordChecker.guessWordAttempt("аббат", new WordResult(false, new CharResult[]{
                green,yellow,yellow,yellow,yellow,
        }));

        assertThat(wordChecker.conform("атаба"), is(true));
    }

}
