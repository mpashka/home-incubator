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
    public void test3() {
        WordChecker wordChecker = new WordChecker(Language.rus);
        wordChecker.clear();
        // word - слово
        byte c = Language.rus.idx('о');
        wordChecker.guessWordAttempt("аоеиу", new WordResult(false, new CharResult[]{
                black,yellow,black,black,black,
        }));
        assertThat(wordChecker.getPresentChars().get(c).getCount(), is(-1));
        wordChecker.guessWordAttempt("ртвлб", new WordResult(false, new CharResult[]{
                black,black,yellow,yellow,black,
        }));
        assertThat(wordChecker.getPresentChars().get(c).getCount(), is(-1));
        wordChecker.guessWordAttempt("олово", new WordResult(false, new CharResult[]{
                black,green,green,green,green,
        }));
        assertThat(wordChecker.getPresentChars().get(c).getCount(), is(2));

        assertThat(wordChecker.conform("слово"), is(true));
    }

}
