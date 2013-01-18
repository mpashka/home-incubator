package org.homeincubator.langedu.client.forms.education;

import java.util.List;
import org.homeincubator.langedu.client.EducationPage;
import org.homeincubator.langedu.client.Educator;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;

/**
 */
public abstract class DragWords implements EducationPage {
    interface OverlapWordsUiBinder extends UiBinder<DivElement, DragWords> {}
    private static OverlapWordsUiBinder ourUiBinder = GWT.create(OverlapWordsUiBinder.class);

    @UiField TableElement table;
//    private

    public DragWords() {
        DivElement rootElement = ourUiBinder.createAndBindUi(this);

    }

    @Override
    public void educate(List<Educator.WordEducation> words) {
        Element child;
        while ((child = table.getFirstChildElement()) != null) {
            table.removeChild(child);
        }

        StringBuilder content = new StringBuilder();
        for (Educator.WordEducation word : words) {

        }
    }

    private static final class DraggableWordInfo {

    }
}
