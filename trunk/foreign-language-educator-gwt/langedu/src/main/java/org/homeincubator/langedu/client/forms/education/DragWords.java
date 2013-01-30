package org.homeincubator.langedu.client.forms.education;

import java.util.List;
import org.homeincubator.langedu.client.EducationPage;
import org.homeincubator.langedu.client.Educator;
import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandler;
import com.allen_sauer.gwt.dnd.client.DragStartEvent;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.VetoDragException;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;

/**
 */
public abstract class DragWords implements EducationPage, DragHandler {
    private Educator educator;

    interface OverlapWordsUiBinder extends UiBinder<HTMLPanel, DragWords> {}
    private static OverlapWordsUiBinder ourUiBinder = GWT.create(OverlapWordsUiBinder.class);

    private static final int WIDTH = 200;
    private static final int WIDTH_GAP = 100;
    private static final int HEIGHT = 50;
    private static final int HEIGHT_GAP = 50;

    @UiField AbsolutePanel panel;

    private HTMLPanel rootElement;
    private PickupDragController dragController;


    public DragWords(Educator educator) {
        this.educator = educator;
        rootElement = ourUiBinder.createAndBindUi(this);
        dragController = new PickupDragController(panel, true);
        dragController.addDragHandler(this);
/*
        AbsolutePositionDropController dropController = new AbsolutePositionDropController(panel);
        dragController.registerDropController(dropController);
*/
    }

    public Element getRootElement() {
        return rootElement.getElement();
    }

    @Override
    public void educate(List<Educator.WordEducation> words) {
        int height = words.size() * (HEIGHT + HEIGHT_GAP) + HEIGHT_GAP;
        int width = WIDTH*2 + WIDTH_GAP*3;
        panel.setPixelSize(width, height);

        panel.clear();

        for (int i = 0; i < words.size(); i++) {
            Educator.WordEducation word =  words.get(i);
            DraggableWordInfo wordInfo = new DraggableWordInfo(word);
            Label sourceLabel = new Label(word.getWord());
            panel.add(sourceLabel, WIDTH_GAP, calculateTop(i));
            dragController.makeDraggable(sourceLabel);
            sourceLabel.addStyleName("dragAndDropWord draggableWord");

            Label targetLabel = new Label(word.getTranslation());
            panel.add(targetLabel, WIDTH + WIDTH_GAP*2, calculateTop(i));
            targetLabel.addStyleName("dragAndDropWord droppableWord");
        }
    }

    private int calculateTop(int i) {
        return HEIGHT_GAP + (HEIGHT + HEIGHT_GAP) * i;
    }

    //
    // DnD implementation
    //

    @Override
    public void onDragEnd(DragEndEvent event) {

    }

    @Override
    public void onDragStart(DragStartEvent event) {
    }

    @Override
    public void onPreviewDragEnd(DragEndEvent event) throws VetoDragException {
    }

    @Override
    public void onPreviewDragStart(DragStartEvent event) throws VetoDragException {
    }


    @UiHandler("finish")
    public void onFinish(ClickEvent event) {
//        word.setLevel(Level.trans_fwd0);
        educator.nextEducation();
    }

    private static final class DraggableWordInfo {
        private Educator.WordEducation word;

        private DraggableWordInfo(Educator.WordEducation word) {
            this.word = word;
        }
    }
}
