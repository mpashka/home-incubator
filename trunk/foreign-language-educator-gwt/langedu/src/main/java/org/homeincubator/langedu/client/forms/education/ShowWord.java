package org.homeincubator.langedu.client.forms.education;

import java.util.List;
import org.homeincubator.langedu.client.EducationPage;
import org.homeincubator.langedu.client.Educator;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;

/**
 */
public class ShowWord implements EducationPage {

    interface ShowWordUiBinder extends UiBinder<DivElement, ShowWord> {}
    private static ShowWordUiBinder ourUiBinder = GWT.create(ShowWordUiBinder.class);

    private Educator educator;
    private Educator.WordEducation word;

    private DivElement rootElement;
    @UiField DivElement sound;
    @UiField DivElement image;
    @UiField DivElement wordHtml;
    @UiField DivElement translation;

    public ShowWord(Educator educator) {
        this.educator = educator;
        rootElement = ourUiBinder.createAndBindUi(this);
    }

    public DivElement getRootElement() {
        return rootElement;
    }

    @Override
    public int getWordsCount() {
        return 1;
    }

    @Override
    public int getMinWordsCount() {
        return 1;
    }

    @Override
    public int getMaxWordsCount() {
        return 1;
    }

    @Override
    public void educate(List<Educator.WordEducation> words) {
        if (words.size() != 1) {
            throw new RuntimeException("Words count must be 1 for show word!");
        }
        word = words.get(0);
        sound.setInnerHTML(word.getSoundUrl());
        image.setInnerHTML(word.getImageUrl());
        wordHtml.setInnerHTML(word.getWord());
        translation.setInnerHTML(word.getTranslation());
    }

    @Override
    public Level getLevel() {
        return Level.notseen;
    }

    @UiHandler("finish")
    public void onFinish(ClickEvent event) {
        word.setLevel(Level.trans_fwd0);
        educator.nextEducation();
    }

}
