package org.homeincubator.langedu.client.forms.education;

import java.util.List;
import org.homeincubator.langedu.client.EducationPage;
import org.homeincubator.langedu.client.Educator;

/**
 */
public class DragWordsEasy implements EducationPage {
    @Override
    public int getWordsCount() {
        return 5;
    }

    @Override
    public int getMinWordsCount() {
        return 4;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getMaxWordsCount() {
        return 7;
    }

    @Override
    public void educate(List<Educator.WordEducation> words) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Level getLevel() {
        return Level.select;
    }
}
