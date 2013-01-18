package org.homeincubator.langedu.client.forms;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.homeincubator.langedu.client.Educator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class SelectWordsForm {
    private Educator educator;

    interface SelectWordsFormUiBinder extends UiBinder<DivElement, SelectWordsForm> {
        SelectWordsStyle getSelectWordsStyle();
    }

    private static SelectWordsFormUiBinder ourUiBinder = GWT.create(SelectWordsFormUiBinder.class);

    private DivElement rootElement;
    @UiField SpanElement text;
    @UiField VerticalPanel selectedWords;

    private Map<String, SelectWordInfo> selectText = new HashMap<>();

    public SelectWordsForm(Educator educator) {
        this.educator = educator;
        rootElement = ourUiBinder.createAndBindUi(this);
    }

    public DivElement getRootElement() {
        return rootElement;
    }

    public void setText(List<Educator.WordInfo> words) {
        for (Educator.WordInfo wordInfo : words) {
            if (wordInfo.getType() == Educator.WordInfo.GAP) {
//                DOM.createElement("span")
                text.appendChild(Document.get().createTextNode(wordInfo.getWord()));
            } else if (wordInfo.getType() == Educator.WordInfo.WORD) {
                String wordNorm = wordInfo.getWord().toLowerCase();
                Label textWord = new Label(wordInfo.getWord());
                SelectWordInfo selectWordInfo = selectText.get(wordNorm);
                if (selectWordInfo == null) {
                    selectWordInfo = new SelectWordInfo(wordNorm);
                    selectText.put(wordNorm, selectWordInfo);
                }
                textWord.addClickHandler(selectWordInfo);
            }
        }

    }


    private class SelectWordInfo implements ClickHandler {
        private String word;
        private Label wordLabel;
        private List<Label> textWords = new ArrayList<>();
        boolean selected;

        public SelectWordInfo(String word) {
            this.word = word;
            this.wordLabel = new Label(word);
            wordLabel.addClickHandler(this);
        }

        public void addTextWord(Label textWord) {
            textWords.add(textWord);
        }

        @Override
        public void onClick(ClickEvent event) {
            selected = !selected;
            if (selected) {
                selectedWords.add(wordLabel);
            } else {
                selectedWords.remove(wordLabel);
            }
            for (Label textWord : textWords) {
                if (selected) textWord.addStyleName(ourUiBinder.getSelectWordsStyle().selected());
                else textWord.removeStyleName(ourUiBinder.getSelectWordsStyle().selected());
            }
        }

        public boolean isSelected() {
            return selected;
        }

        public String getWord() {
            return word;
        }
    }

    @UiHandler("startLink")
    public void onClick(ClickEvent event) {
        List<String> words = new ArrayList<>();
        for (SelectWordInfo selectWordInfo : selectText.values()) {
            if (selectWordInfo.isSelected()) {
                words.add(selectWordInfo.getWord());
            }
        }
        educator.finishSelectWords(words);
    }

}
