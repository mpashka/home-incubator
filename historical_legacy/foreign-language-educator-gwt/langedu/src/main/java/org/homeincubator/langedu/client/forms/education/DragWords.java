package org.homeincubator.langedu.client.forms.education;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.homeincubator.langedu.client.EducationPage;
import org.homeincubator.langedu.client.Educator;
import org.homeincubator.langedu.client.GwtUtils;
import org.homeincubator.langedu.client.RandomSelector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

/**
 */
public abstract class DragWords implements EducationPage {

    private static final Logger log = Logger.getLogger(DragWords.class.getName());


    interface OverlapWordsUiBinder extends UiBinder<DivElement, DragWords> {}
    private static OverlapWordsUiBinder ourUiBinder = GWT.create(OverlapWordsUiBinder.class);

    private static final String DROPPABLE_TARGET_ATTR = "DropWord";
    private static final String DRAGGABLE_WORD_ATTR = "DragWord";

    private static final int WIDTH = 200;
    private static final int WIDTH_GAP = 100;
    private static final int HEIGHT = 30;
    private static final int HEIGHT_GAP = 30;

    @UiField DivElement panel;
    @UiField AnchorElement finishLink;

    private Educator educator;
    private DivElement rootElement;
    private DraggableWordInfo draggableWordInfo;
    private List<DraggableWordInfo> words = new ArrayList<DraggableWordInfo>();
    private List<DivElement> droppableLabels = new ArrayList<DivElement>();



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
                    if (draggableWordInfo == null) {
                        return;
                    }
                    dragStart(event, eventTarget, draggableWordInfo);
                } else {
                    if (draggableWordInfo == null) {
                        return;
                    }
                    if (eventType == Event.ONMOUSEMOVE) {
                        drag(event, eventTarget);
                        GwtUtils.stopEvent(event);
                    } else if (eventType == Event.ONMOUSEUP) {
                        log.finest("mouse " + event.getClientX() + " x " + event.getClientY());
                        int x = event.getClientX(), y = event.getClientY();
                        Educator.WordEducation word = null;
                        for (DivElement droppableLabel : droppableLabels) {
                            {   // debug
                                Educator.WordEducation word0 = (Educator.WordEducation) droppableLabel.getPropertyObject(DROPPABLE_TARGET_ATTR);
                                log.finest("[" + word0.getWord() + "] "
                                    + droppableLabel.getAbsoluteLeft() + ".." + droppableLabel.getAbsoluteRight()
                                    + " x "
                                    + droppableLabel.getAbsoluteTop() + ".." + droppableLabel.getAbsoluteBottom()
                                );

                            }

                            if (event.getClientX() > droppableLabel.getAbsoluteLeft()
                                && event.getClientX() < droppableLabel.getAbsoluteRight()
                                && event.getClientY() > droppableLabel.getAbsoluteTop()
                                && event.getClientY() < droppableLabel.getAbsoluteBottom())
                            {
                                log.finest("in");
                                x = droppableLabel.getOffsetLeft();
                                y = droppableLabel.getOffsetTop();
                                word = (Educator.WordEducation) droppableLabel.getPropertyObject(DROPPABLE_TARGET_ATTR);
                                break;
                            }
                        }
                        draggableWordInfo.dropWord = word;
                        dragStop(event, x, y, eventTarget, word == null);
                    }
                }
            }
        });
    }


    public Element getRootElement() {
        return rootElement;
    }


    @Override
    public void educate(List<Educator.WordEducation> inputWords) {
/*
        panel.setPixelSize(width, height);
*/

        panel.setInnerHTML("");
        int height = inputWords.size() * (HEIGHT + HEIGHT_GAP) + HEIGHT_GAP;
        int width = WIDTH*3 + WIDTH_GAP*4;
        panel.getStyle().setHeight(height, Style.Unit.PX);
//        panel.getStyle().setWidth(width, Style.Unit.PX);

        words.clear();
        droppableLabels.clear();
        List<Educator.WordEducation> draggableWords = new ArrayList<Educator.WordEducation>(inputWords);
        List<Educator.WordEducation> dropableWords = new ArrayList<Educator.WordEducation>(inputWords);
        Location l = new Location(0, 0);
        for (int i = 0; i < inputWords.size(); i++) {

            {
                Educator.WordEducation word = RandomSelector.randomSelect(draggableWords, true);
                DivElement sourceLabel = Document.get().createDivElement();
                sourceLabel.appendChild(Document.get().createTextNode(word.getWord()));
                sourceLabel.setClassName("Draggable");
                l.set(WIDTH_GAP, calculateTop(i));
                l.pos(sourceLabel);

                DraggableWordInfo wordInfo = new DraggableWordInfo(word, width-WIDTH, height-HEIGHT, sourceLabel);
                sourceLabel.setPropertyObject(DRAGGABLE_WORD_ATTR, wordInfo);
                panel.appendChild(sourceLabel);
                words.add(wordInfo);
            }

            Educator.WordEducation word = RandomSelector.randomSelect(dropableWords, true);
            {
                DivElement droppableLabel = Document.get().createDivElement();
                droppableLabel.appendChild(Document.get().createTextNode(""));
                droppableLabel.setClassName("Droppable");
                droppableLabel.getStyle().setLeft(WIDTH + WIDTH_GAP * 2, Style.Unit.PX);
                droppableLabel.getStyle().setTop(calculateTop(i), Style.Unit.PX);
                droppableLabel.setPropertyObject(DROPPABLE_TARGET_ATTR, word);
                panel.appendChild(droppableLabel);
                droppableLabels.add(droppableLabel);
            }
            {
                DivElement helpLabel = Document.get().createDivElement();
                helpLabel.appendChild(Document.get().createTextNode(word.getTranslation()));
                helpLabel.setClassName("Help");
                helpLabel.getStyle().setLeft(WIDTH * 2 + WIDTH_GAP * 3, Style.Unit.PX);
                helpLabel.getStyle().setTop(calculateTop(i), Style.Unit.PX);
                panel.appendChild(helpLabel);
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
        draggableWordInfo.onDrag(event.getClientX(), event.getClientY());
    }


    private void dragStop(Event event, int x, int y, Element eventTarget, boolean mousePos) {
        draggableWordInfo.onDragStop(x, y, mousePos);
        draggableWordInfo = null;
    }


    private void dragStart(Event event, Element eventTarget, DraggableWordInfo draggableWordInfo) {
        this.draggableWordInfo = draggableWordInfo;
        draggableWordInfo.onDragStart(event.getClientX(), event.getClientY());
    }


    //
    //
    //

    public void onFinish(Event event) {
        List<DraggableWordInfo> errors = new ArrayList<DraggableWordInfo>();
        for (DraggableWordInfo word : words) {
            log.finest("Check [" + word.word.getWord() + "]: " + (word.word == word.dropWord));
            if (word.word == word.dropWord) {
                continue;
            }
            word.element.addClassName("DraggingError");
            errors.add(word);
        }

        if (errors.isEmpty()) {
//        word.setLevel(Level.trans_fwd0);
            educator.nextEducation();
        }
    }

    static final class Location {
        private int x, y;

        Location(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void set(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void set(Location l) {
            x = l.x;
            y = l.y;
        }

        public void sub(Location l) {
            x -= l.x;
            y -= l.y;
        }

        public void add(Location l) {
            x += l.x;
            y += l.y;
        }

        public void checkBounds(Location l) {
            if (x < 0) x = 0;
            if (y < 0) y = 0;
            if (x > l.x) x = l.x;
            if (y > l.y) y = l.y;
        }

        public void pos(Element element) {
            element.getStyle().setLeft(x, Style.Unit.PX);
            element.getStyle().setTop(y, Style.Unit.PX);
        }

        @Override
        public String toString() {
            return "{" + x + ", " + y + '}';
        }
    }

    private static final class DraggableWordInfo {
        private Educator.WordEducation word;
        private Educator.WordEducation dropWord;
        /**
         * Ограничение на перемещение
         */
        private Location max;

        /**
         * Mouse inside element on drag start
         */
        private Location mouseOffet;
        private Element element;

        /**
         * Нужна просто чтоб не плодить объектов
         */
        private Location l = new Location(0, 0);

        private DraggableWordInfo(Educator.WordEducation word, int maxX, int maxY, DivElement sourceLabel) {
            this.word = word;
            this.max = new Location(maxX, maxY);
            this.element = sourceLabel;
        }

        public void onDragStart(int mouseX, int mouseY) {
            this.element.addClassName("Dragging");
            mouseOffet = new Location(mouseX-this.element.getOffsetLeft(), mouseY-this.element.getOffsetTop());
            log.finest("Drag start [" + word.getWord() + "]. Mouse: " + mouseOffet);
        }

        public void onDrag(int mouseX, int mouseY) {
            l.set(mouseX, mouseY);
            l.sub(mouseOffet);
            l.checkBounds(max);
            l.pos(element);
        }

        public void onDragStop(int clientX, int clientY, boolean mousePos) {
            if (!mousePos) {
                l.set(clientX, clientY);
                l.pos(element);
            }
            element.removeClassName("Dragging");
        }
    }
}
