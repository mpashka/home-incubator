package org.homeincubator.langedu.client.forms;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.homeincubator.langedu.client.Educator;
import org.homeincubator.langedu.client.GwtUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

/**
 */
public class TextInputForm {

    private static final Logger log = Logger.getLogger(TextInputForm.class.getName());


    interface TextInputFormUiBinder extends UiBinder<DivElement, TextInputForm> {}


    private Educator educator;

    @UiField TextAreaElement inputTextArea;
    @UiField Element startLink;
    @UiField Element startTestsLink;

    private DivElement rootElement;

    public TextInputForm(Educator educator) {
        this.educator = educator;
        rootElement = GWT.<TextInputFormUiBinder>create(TextInputFormUiBinder.class).createAndBindUi(this);
        GwtUtils.addEventListener(startLink, Event.ONCLICK, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                log.finest("Event " + event.getType());
                if (DOM.eventGetType(event) == Event.ONCLICK) {
                    log.finest("Event processing");

                    finishTextInput(event);
                    GwtUtils.stopEvent(event);
                }
            }
        });
        // todo [!] Че за хуйня
        GwtUtils.addEventListener(startTestsLink, Event.ONCLICK, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                onStartTests(null);
            }
        });
    }

    public void setText(String text) {
        inputTextArea.setValue(text);
    }

    public DivElement getRootElement() {
        return rootElement;
    }

    public void finishTextInput(Event event) {
        log.finest("Finish text input");
        educator.finishTextInput(inputTextArea.getValue());
    }

//    @UiHandler("startTestsLink")
    void onStartTests(ClickEvent event) {
        log.finest("Start tests");
        List<String> words = Arrays.asList(
            "Example",
            "text ",
            "used",
            "to",
            "test",
            "application ",
            "tr ",
            "Description",
            "Bind",
            "an",
            "event",
            "handler",
            "the",
            "click",
            "JavaScript",
            "or",
            "trigger"
        );
        educator.finishSelectWords(words);
        educator.nextEducation();
//        GwtUtils.stopEvent(event);
    }
}
