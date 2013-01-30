package org.homeincubator.langedu.client.forms;

import org.homeincubator.langedu.client.Educator;

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
    interface TextInputFormUiBinder extends UiBinder<DivElement, TextInputForm> {}
    private static TextInputFormUiBinder ourUiBinder = GWT.create(TextInputFormUiBinder.class);


    private Educator educator;

    @UiField TextAreaElement inputTextArea;
    @UiField Element startLink;

    private DivElement rootElement;

    public TextInputForm(Educator educator) {
        this.educator = educator;
        rootElement = ourUiBinder.createAndBindUi(this);
        com.google.gwt.user.client.Element element = (com.google.gwt.user.client.Element) startLink;
        DOM.setEventListener(element, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                GWT.log("Event " + event.getType());
                if (DOM.eventGetType(event) == Event.ONCLICK) {
                    GWT.log("Event processing");

                    finishTextInput(event);
                    event.preventDefault();
                    event.stopPropagation();
                }
            }
        });
        DOM.sinkEvents(element, Event.ONCLICK);
    }

    public void setText(String text) {
        inputTextArea.setValue(text);
    }

    public DivElement getRootElement() {
        return rootElement;
    }

    public void finishTextInput(Event event) {
        GWT.log("Finish text input");
        educator.finishTextInput(inputTextArea.getValue());
    }
}
