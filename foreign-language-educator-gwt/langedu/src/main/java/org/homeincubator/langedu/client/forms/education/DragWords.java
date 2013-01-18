package org.homeincubator.langedu.client.forms.education;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;

/**
 */
public class DragWords {
    interface OverlapWordsUiBinder extends UiBinder<DivElement, DragWords> {}
    private static OverlapWordsUiBinder ourUiBinder = GWT.create(OverlapWordsUiBinder.class);

    public DragWords() {
        DivElement rootElement = ourUiBinder.createAndBindUi(this);

    }
}