package org.mpashka.worldehelper;

import static org.hamcrest.Matchers.*;
import static org.mpashka.worldehelper.CompetitionInterface.CharResult.*;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;

public class CompetitionTest {
    @Test
    public void testSingleChar() {
        Competition competition = new Competition(Language.eng);
        competition.setWordList(new String[]{
                "abcde",
                "qwert",
        });
        CompetitionInterface.SessionId sessionId = competition.session();
        assertThat(competition.nextWord(sessionId), is(true));

        // Word - abcde
        CompetitionInterface.WordResult res1 = competition.checkWord(sessionId, "fghed");
        assertThat(res1.fin(), is(false));
        assertThat(res1.result(), arrayContaining(black, black, black, yellow, yellow));

        CompetitionInterface.WordResult res2 = competition.checkWord(sessionId, "fgedq");
        assertThat(res2.fin(), is(false));
        assertThat(res2.result(), arrayContaining(black, black, yellow, green, black));

        CompetitionInterface.WordResult res3 = competition.checkWord(sessionId, "abcde");
        assertThat(res3.fin(), is(true));
        assertThat(res3.result(), arrayContaining(green, green, green, green, green));

        assertThat(competition.nextWord(sessionId), is (true));

        // Word - qwert
        CompetitionInterface.WordResult res2_1 = competition.checkWord(sessionId, "fghed");
        assertThat(res2_1.fin(), is(false));
        assertThat(res2_1.result(), arrayContaining(black, black, black, yellow, black));

        CompetitionInterface.WordResult res2_2 = competition.checkWord(sessionId, "fgedq");
        assertThat(res2_2.fin(), is(false));
        assertThat(res2_2.result(), arrayContaining(black, black, green, black, yellow));

        CompetitionInterface.WordResult res2_3 = competition.checkWord(sessionId, "abcde");
        assertThat(res2_3.fin(), is(false));
        assertThat(res2_3.result(), arrayContaining(black, black, black, black, yellow));

        CompetitionInterface.WordResult res2_4 = competition.checkWord(sessionId, "aecde");
        assertThat(res2_4.fin(), is(false));
        assertThat(res2_4.result(), arrayContaining(black, yellow, black, black, black));

        CompetitionInterface.WordResult res2_5 = competition.checkWord(sessionId, "ebede");
        assertThat(res2_5.fin(), is(false));
        assertThat(res2_5.result(), arrayContaining(black, black, green, black, black));

        CompetitionInterface.WordResult res2_6 = competition.checkWord(sessionId, "abcde");
        assertThat(res2_6.fin(), is(true));
        assertThat(res2_6.result(), arrayContaining(black, black, black, black, yellow));

        assertThat(competition.nextWord(sessionId), is (false));
        CompetitionInterface.CompetitionResult result = competition.result(sessionId);
        assertThat(Arrays.stream(result.attempts()).boxed().toArray(Integer[]::new), arrayContaining(0,0,1,0,0,0));
        assertThat(result.notFound(), is(1));
        assertThat(result.score(), closeTo(3, 0.001));
    }

    @Test
    public void testDoubleChar() {
        Competition competition = new Competition(Language.eng);
        competition.setWordList(new String[]{
                "adadd",
        });
        CompetitionInterface.SessionId sessionId = competition.session();
        CompetitionInterface.WordResult res;
        assertThat(competition.nextWord(sessionId), is(true));

        // Word - adadd
        res = competition.checkWord(sessionId, "caccc");
        assertThat(res.fin(), is(false));
        assertThat(res.result(), arrayContaining(black, yellow, black, black, black));

        res = competition.checkWord(sessionId, "cacaa");
        assertThat(res.fin(), is(false));
        assertThat(res.result(), arrayContaining(black, yellow, black, yellow, black));

        res = competition.checkWord(sessionId, "caade");
        assertThat(res.fin(), is(false));
        assertThat(res.result(), arrayContaining(black, yellow, green, green, black));

        res = competition.checkWord(sessionId, "aaaaa");
        assertThat(res.fin(), is(false));
        assertThat(res.result(), arrayContaining(green, black, green, black, black));

/*
        assertThat(competition.nextWord(sessionId), is (false));

        assertThat(competition.nextWord(sessionId), is (false));
        CompetitionInterface.CompetitionResult result = competition.result(sessionId);
        assertThat(Arrays.stream(result.attempts()).boxed().toArray(Integer[]::new), arrayContaining(0,0,1,0,0,0));
        assertThat(result.notFound(), is(1));
        assertThat(result.score(), closeTo(3, 0.001));
*/
    }

    @Test
    public void testDoubleChar2() {
        Competition competition = new Competition(Language.eng);
        competition.setWordList(new String[]{
                "abcde",
                "aabbb",
        });
        CompetitionInterface.SessionId sessionId = competition.session();
        CompetitionInterface.WordResult res;
        assertThat(competition.nextWord(sessionId), is(true));

        // Word - abcde
        res = competition.checkWord(sessionId, "zaaqw");
        assertThat(res.fin(), is(false));
        assertThat(res.result(), arrayContaining(black, yellow, black, black, black));

        res = competition.checkWord(sessionId, "zccqw");
        assertThat(res.fin(), is(false));
        assertThat(res.result(), arrayContaining(black, black, green, black, black));

        res = competition.checkWord(sessionId, "zcxcw");
        assertThat(res.fin(), is(false));
        assertThat(res.result(), arrayContaining(black, yellow, black, black, black));

        competition.nextWord(sessionId);

        // Word - aabbb
        res = competition.checkWord(sessionId, "zaaqw");
        assertThat(res.fin(), is(false));
        assertThat(res.result(), arrayContaining(black, green, yellow, black, black));

        res = competition.checkWord(sessionId, "zaaaw");
        assertThat(res.fin(), is(false));
        assertThat(res.result(), arrayContaining(black, green, yellow, black, black));

        res = competition.checkWord(sessionId, "aaaaw");
        assertThat(res.fin(), is(false));
        assertThat(res.result(), arrayContaining(green, green, black, black, black));
    }

    @Test
    public void testDoubleCharProd1() {
        Competition competition = new Competition(Language.rus);
        competition.setWordList(new String[]{
                "вширь",
                "атаба",
        });
        CompetitionInterface.SessionId sessionId = competition.session();
        assertThat(competition.nextWord(sessionId), is(true));

        // Word - вширь
        assertThat(competition.checkWord(sessionId, "вирши").result(), arrayContaining(green,yellow,yellow,yellow,black));

        competition.nextWord(sessionId);

        // Word - атаба
        assertThat(competition.checkWord(sessionId, "аббат").result(), arrayContaining(green,yellow,black,yellow,yellow));
    }
}
