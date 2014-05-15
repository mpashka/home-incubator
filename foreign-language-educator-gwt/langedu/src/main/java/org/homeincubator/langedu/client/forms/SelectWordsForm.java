package org.homeincubator.langedu.client.forms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.homeincubator.langedu.client.Educator;
import org.homeincubator.langedu.client.GwtUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.Text;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

/**
 */
public class SelectWordsForm {

    private static final Logger log = Logger.getLogger(SelectWordsForm.class.getName());


    interface SelectWordsFormUiBinder extends UiBinder<DivElement, SelectWordsForm> {}
    private static SelectWordsFormUiBinder ourUiBinder = GWT.create(SelectWordsFormUiBinder.class);
    private static final String WORD_INFO_ATTR = "word";
    private static final String WORD_SELECTED_CLASS = "selected";

    private Educator educator;
    private DivElement rootElement;
    @UiField Element text;
    @UiField UListElement selectedWords;
    @UiField AnchorElement nextLink;

    private Map<String, SelectWordInfo> selectText = new HashMap<String, SelectWordInfo>();

    public SelectWordsForm(Educator educator) {
        this.educator = educator;
        rootElement = ourUiBinder.createAndBindUi(this);
        GwtUtils.addEventListener(nextLink, Event.ONCLICK, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                finishSelectWords(event);
                GwtUtils.stopEvent(event);
            }
        });

        EventListener listener = new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                EventTarget targetEvnt = event.getEventTarget();
                Element target = targetEvnt.cast();
                SelectWordInfo wordInfo = (SelectWordInfo) target.getPropertyObject(WORD_INFO_ATTR);
                if (wordInfo != null) {
                    wordInfo.switchSelected();
                } else {
                    log.finest("Click on no-word: " + target);
                }
                GwtUtils.stopEvent(event);
            }
        };
        GwtUtils.addEventListener(text, Event.ONCLICK, listener);
        GwtUtils.addEventListener(selectedWords, Event.ONCLICK, listener);
    }

     public Element getRootElement() {
        return rootElement.cast();
    }

    public void setText(List<Educator.WordInfo> words) {
//        StringBuilder textContent = new StringBuilder();
        for (Educator.WordInfo wordInfo : words) {
            String word = wordInfo.getWord();

            if (wordInfo.getType() == Educator.WordInfo.WORD) {
                Element span = DOM.createSpan();
                span.setInnerHTML(word);
                String wordNorm = wordInfo.getWord().toLowerCase();
                SelectWordInfo selectWordInfo = selectText.get(wordNorm);
                if (selectWordInfo == null) {
                    selectWordInfo = new SelectWordInfo(wordNorm);
                    selectText.put(wordNorm, selectWordInfo);
                }
                span.setPropertyObject(WORD_INFO_ATTR, selectWordInfo);
                selectWordInfo.addTextWord(span);
                text.appendChild(span);
            } else {
                Text textNode = Document.get().createTextNode(word);
                text.appendChild(textNode);
            }
        }
    }


    private class SelectWordInfo {
        private String word;
        private Element wordLabel;
        private List<Element> textWords = new ArrayList<Element>();
        boolean selected;

        public SelectWordInfo(String word) {
            this.word = word;
        }

        private Element getLabel() {
            if (wordLabel == null) {
                wordLabel = DOM.createElement(LIElement.TAG);
                wordLabel.setInnerHTML(word);
                wordLabel.setPropertyObject(WORD_INFO_ATTR, this);
            }
            return wordLabel;
        }

        public void addTextWord(Element textWord) {
            textWords.add(textWord);
        }

        public void switchSelected() {
            selected = !selected;
            log.finest("Selected [" + selected + "] word: " + word);
            if (selected) {
                selectedWords.appendChild(getLabel());
            } else {
                selectedWords.removeChild(wordLabel);
            }
            for (Element textWord : textWords) {
                textWord.setClassName(selected ? WORD_SELECTED_CLASS : "");
            }
        }

        public boolean isSelected() {
            return selected;
        }

        public String getWord() {
            return word;
        }
    }

    public void finishSelectWords(Event event) {
        log.finest("Finish select words");
        List<String> words = new ArrayList<String>();
        for (SelectWordInfo selectWordInfo : selectText.values()) {
            if (selectWordInfo.isSelected()) {
                words.add(selectWordInfo.getWord());
            }
        }
        educator.finishSelectWords(words);
    }

}
