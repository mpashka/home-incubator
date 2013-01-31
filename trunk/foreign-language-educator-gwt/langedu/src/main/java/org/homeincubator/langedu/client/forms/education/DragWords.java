package org.homeincubator.langedu.client.forms.education;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import org.homeincubator.langedu.client.EducationPage;
import org.homeincubator.langedu.client.Educator;
import org.homeincubator.langedu.client.GwtUtils;

import java.util.List;

/**
 */
public abstract class DragWords implements EducationPage {

    interface OverlapWordsUiBinder extends UiBinder<DivElement, DragWords> {}
    private static OverlapWordsUiBinder ourUiBinder = GWT.create(OverlapWordsUiBinder.class);

    private static final String WORD_ATTR = "DropWord";
    private static final String DRAGGABLE_WORD_ATTR = "DragWord";

    private static final int WIDTH = 200;
    private static final int WIDTH_GAP = 100;
    private static final int HEIGHT = 50;
    private static final int HEIGHT_GAP = 50;

    @UiField DivElement panel;
    @UiField AnchorElement finishLink;

    private Educator educator;
    private DivElement rootElement;
    private DraggableWordInfo draggableWordInfo;



    public DragWords(Educator educator) {
        this.educator = educator;
        rootElement = ourUiBinder.createAndBindUi(this);

        GwtUtils.addEventListener(finishLink, Event.ONCLICK, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                onFinish(event);
                GwtUtils.stopEvent(event);
            }
        });

        GwtUtils.addEventListener(panel, Event.ONMOUSEDOWN | Event.ONMOUSEMOVE | Event.ONMOUSEUP
                | Event.ONMOUSEOUT | Event.ONMOUSEOVER, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                Element eventTarget = event.getEventTarget().cast();

                int eventType = DOM.eventGetType(event);
                if (eventType == Event.ONMOUSEDOWN) {
                    DraggableWordInfo draggableWordInfo = (DraggableWordInfo) eventTarget.getPropertyObject(DRAGGABLE_WORD_ATTR);
                    if (draggableWordInfo == null) return;
                    dragStart(event, eventTarget, draggableWordInfo);
                } else {
                    if (draggableWordInfo == null) return;
                    if (eventType == Event.ONMOUSEMOVE) {
                        drag(event, eventTarget);
                    } else if (eventType == Event.ONMOUSEUP) {
                        dragStop(event, eventTarget);
                    }
                }
            }
        });
    }

    public Element getRootElement() {
        return rootElement;
    }

    @Override
    public void educate(List<Educator.WordEducation> words) {
/*
        panel.setPixelSize(width, height);
*/

        panel.setInnerHTML("");
        int height = words.size() * (HEIGHT + HEIGHT_GAP) + HEIGHT_GAP;
        int width = WIDTH*2 + WIDTH_GAP*3;
        panel.getStyle().setHeight(height, Style.Unit.PX);
        panel.getStyle().setWidth(width, Style.Unit.PX);

        for (int i = 0; i < words.size(); i++) {
            Educator.WordEducation word =  words.get(i);

            {   DraggableWordInfo wordInfo = new DraggableWordInfo(word, width-WIDTH, height-HEIGHT);
                DivElement sourceLabel = Document.get().createDivElement();
                sourceLabel.appendChild(Document.get().createTextNode(word.getWord()));
                sourceLabel.setClassName("Draggable");
                wordInfo.locate(sourceLabel, WIDTH_GAP, calculateTop(i));
                sourceLabel.setPropertyObject(DRAGGABLE_WORD_ATTR, wordInfo);
                panel.appendChild(sourceLabel);
            }
            {
                DivElement targetLabel = Document.get().createDivElement();
                targetLabel.appendChild(Document.get().createTextNode(word.getWord()));
                targetLabel.setClassName("Droppable");
                targetLabel.getStyle().setLeft(WIDTH + WIDTH_GAP*2, Style.Unit.PX);
                targetLabel.getStyle().setTop(calculateTop(i), Style.Unit.PX);
                targetLabel.setPropertyObject(WORD_ATTR, word);
                panel.appendChild(targetLabel);
            }
        }
    }

    private int calculateTop(int i) {
        return HEIGHT_GAP + (HEIGHT + HEIGHT_GAP) * i;
    }

    //
    // DnD implementation
    //

    private void drag(Event event, Element eventTarget) {
        draggableWordInfo.setDragPos(event.getClientX(), event.getClientY());
    }

    private void dragStop(Event event, Element eventTarget) {
        eventTarget.removeClassName("Dragging");
        draggableWordInfo.recalculatePos(event.getClientX(), event.getClientY());
        draggableWordInfo = null;
    }

    private void dragStart(Event event, Element eventTarget, DraggableWordInfo draggableWordInfo) {
        this.draggableWordInfo = draggableWordInfo;
        eventTarget.addClassName("Dragging");
        draggableWordInfo.setInitialDragPos(event.getClientX(), event.getClientY());
    }


    //
    //
    //

    public void onFinish(Event event) {
//        word.setLevel(Level.trans_fwd0);
        educator.nextEducation();
    }

    private static final class DraggableWordInfo {
        private Educator.WordEducation word;
        private final int maxX;
        private final int maxY;
        private int x,y, dragX, dragY, displayX, displayY;
        private Element element;

        private DraggableWordInfo(Educator.WordEducation word, int maxX, int maxY) {
            this.word = word;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        public void locate(Element element, int x, int y) {
            this.element = element;
            this.x = x;
            this.y = y;
            locate(x, y);
        }

         private void locate(int x, int y) {
             if (x < 0) x = 0;
             if (x > maxX) x = maxX;
             if (y < 0) y = 0;
             if (y > maxY) y = maxY;
             element.getStyle().setLeft(x, Style.Unit.PX);
             element.getStyle().setTop(y, Style.Unit.PX);
             displayX = x;
             displayY = y;
         }

        public void setInitialDragPos(int clientX, int clientY) {
            this.dragX = clientX;
            this.dragY = clientY;
        }

        public void setDragPos(int clientX, int clientY) {
            locate(x - this.dragX + clientX, y - this.dragY + clientY);
        }

        public void recalculatePos(int clientX, int clientY) {
            setDragPos(clientX, clientY);
            x = displayX;
            y = displayY;
        }
    }
}
