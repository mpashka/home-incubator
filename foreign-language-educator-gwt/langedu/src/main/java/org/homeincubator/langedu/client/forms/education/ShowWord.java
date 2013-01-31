package org.homeincubator.langedu.client.forms.education;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import org.homeincubator.langedu.client.EducationPage;
import org.homeincubator.langedu.client.Educator;
import org.homeincubator.langedu.client.GwtUtils;

import java.util.List;

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
    @UiField AnchorElement finishLink;

    public ShowWord(Educator educator) {
        this.educator = educator;
        rootElement = ourUiBinder.createAndBindUi(this);

        GwtUtils.addEventListener(finishLink, Event.ONCLICK, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                onFinish(event);
                GwtUtils.stopEvent(event);
            }
        });
    }

    public Element getRootElement() {
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

    public void onFinish(Event event) {
        word.setLevel(Level.trans_fwd0);
        educator.nextEducation();
    }

}
