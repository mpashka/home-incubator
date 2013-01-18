package org.homeincubator.langedu.client;

import java.util.List;
import java.util.logging.Level;

/**
 */
public interface EducationPage {
    /**
     * Уровень знания, который человек уже показал
     */
    enum Level {
        notseen,
        /**
         * Перевести с иностранного на родной.
         * Выбрать один вариант из 3
         */
        trans_fwd0,
        /**
         * Выбрать один вариант из 7
         */
        trans_fwd1,
        /**
         * Написать по иностранному произношению
         */
        write_aud,
        /**
         * Перевести с родного на иностранный
         */
        trans_back0, trans_back1,
        /**
         * Написать перевод
         */
        write;
    }

    int getWordsCount();
    int getMinWordsCount();
    int getMaxWordsCount();
    void educate(List<Educator.WordEducation> words);
    Level getLevel();
}
