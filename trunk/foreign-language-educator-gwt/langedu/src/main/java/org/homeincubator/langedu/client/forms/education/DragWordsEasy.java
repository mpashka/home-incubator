package org.homeincubator.langedu.client.forms.education;

import java.util.List;
import org.homeincubator.langedu.client.EducationPage;
import org.homeincubator.langedu.client.Educator;

/**
 */
public class DragWordsEasy extends DragWords {

    public DragWordsEasy(Educator educator) {
        super(educator);
    }

    @Override
    public int getWordsCount() {
        return 5;
    }

    @Override
    public int getMinWordsCount() {
        return 4;
    }

    @Override
    public int getMaxWordsCount() {
        return 7;
    }


    @Override
    public EducationPage.Level getLevel() {
        return EducationPage.Level.notseen;
    }
}
