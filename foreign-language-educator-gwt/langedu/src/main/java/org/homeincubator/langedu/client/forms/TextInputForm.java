package org.homeincubator.langedu.client.forms;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.TextArea;
import org.homeincubator.langedu.client.Educator;

/**
 */
public class TextInputForm {
    interface TextInputFormUiBinder extends UiBinder<DivElement, TextInputForm> {}
    private static TextInputFormUiBinder ourUiBinder = GWT.create(TextInputFormUiBinder.class);


    private Educator educator;

    @UiField TextArea inputTextArea;

    private DivElement rootElement;

    public TextInputForm(Educator educator) {
        this.educator = educator;
        rootElement = ourUiBinder.createAndBindUi(this);
    }

    public void setText(String text) {
        inputTextArea.setText(text);
    }

    public DivElement getRootElement() {
        return rootElement;
    }

    @UiHandler("startLink")
    public void finishTextInput(ClickEvent event) {
        educator.finishTextInput(inputTextArea.getText());
    }
}
