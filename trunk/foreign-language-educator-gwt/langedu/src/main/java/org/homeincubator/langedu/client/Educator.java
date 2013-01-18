package org.homeincubator.langedu.client;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import org.homeincubator.langedu.client.forms.PrepareWordsForm;
import org.homeincubator.langedu.client.forms.SelectWordsForm;
import org.homeincubator.langedu.client.forms.TextInputForm;
import org.homeincubator.langedu.client.forms.education.DragWordsEasy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class Educator {

    private static final EducationPage.Level MIN_LEVEL = EducationPage.Level.values()[0];
    private static final EducationPage.Level MAX_LEVEL = EducationPage.Level.values()[EducationPage.Level.values().length-1];

    private Element mainDiv;
    private Element form;
    private List<WordEducation> words = new ArrayList<>();
    private TextInputForm textInputForm = new TextInputForm(this);
    private SelectWordsForm selectWordsForm = new SelectWordsForm(this);
    private PrepareWordsForm prepareWordsForm = new PrepareWordsForm(this);
    private EducationPage[] educationPagesArray = new EducationPage[] {
            new DragWordsEasy()
    };
    private Map<EducationPage.Level, EducationPage> educationPages = new HashMap<>();

    public Educator(Element mainDiv) {
        this.mainDiv = mainDiv;
        for (EducationPage educationPage : educationPagesArray) {
            educationPages.put(educationPage.getLevel(), educationPage);
        }
    }

    public void startTextInput() {
        displayForm(textInputForm.getRootElement());
        textInputForm.setText("Example text " +
                "used to test application " +
                "tr " +
                "Example text " +
                "used to test application " +
                "tr " +
                "Description: Bind an event handler to the \"click\" " +
                " JavaScript event, or trigger that event on an element. " +
                "" +
                "version added: 1.0.click( handler(eventObject) ) " +
                "handler(eventObject)A function to execute each time the event is triggered. " +
                "" +
                "version added: 1.0.click() " +
                "This method is a shortcut for .bind(\\'click\\', " +
                "handler) in the first variation, and .trigger(\\'click\\') in the second. " +
                "" +
                "The click event is sent to an element when " +
                "the mouse pointer is over the element, and " +
                "the mouse button is pressed and released. Any HTML " +
                "element can receive this event.");
    }

    private void displayForm(Element form) {
        if (this.form == null) {
            mainDiv.appendChild(form);
        } else {
            mainDiv.replaceChild(form, this.form);
        }
        this.form = form;
    }

    private static final RegExp wordPattern = RegExp.compile("\\w");
    public void finishTextInput(String text) {
        List<WordInfo> textWords = new ArrayList<>();
        MatchResult matchResult;
        wordPattern.setLastIndex(0);
        int lastIndex = 0;
        while ((matchResult = wordPattern.exec(text)) != null) {
            String word = matchResult.getGroup(0);
            if (matchResult.getIndex() > lastIndex) {
                String gap = text.substring(lastIndex, matchResult.getIndex());
                if (gap.length() > 0) {
                    textWords.add(new WordInfo(gap, WordInfo.GAP));
                }
            }
            textWords.add(new WordInfo(word, WordInfo.WORD));
            lastIndex = wordPattern.getLastIndex();
        }
        if (lastIndex < text.length()-1) {
            String gap = text.substring(lastIndex);
            textWords.add(new WordInfo(gap, WordInfo.GAP));
        }
        selectWordsForm.setText(textWords);
        displayForm(selectWordsForm.getRootElement());
    }


    public void finishSelectWords(List<String> wordNames) {
        words.clear();
        for (String wordName : wordNames) {
            WordEducation word = new WordEducation(wordName);
            word.setTranslation("-" + word + "-trans");
            word.setImageUrl("-" + word + "-img");
            word.setSoundUrl("-" + word + "-snd");
            words.add(word);
        }
        prepareWordsForm.setWords(words);
        displayForm(prepareWordsForm.getRootElement());
    }

    public void finishPrepareWords() {
        displayForm(form);
    }


    private void selectEductation() {
        int minLevel = MAX_LEVEL.ordinal();
        int maxLevel = MIN_LEVEL.ordinal();
        int sum = 0;
        for (WordEducation word : words) {
            int level = word.getLevel().ordinal();
            minLevel = Math.min(level, minLevel);
            maxLevel = Math.max(level, maxLevel);
        }
    }

    /**
     * Подготовка текста к выделению.
     */
    public class WordInfo {
        public static final int WORD = 0;
        public static final int GAP = 1;
        private String word;
        private int type; // 0 - word, 1 - gap

        public WordInfo(String word, int type) {
            this.word = word;
            this.type = type;
        }

        public String getWord() {
            return word;
        }

        public int getType() {
            return type;
        }
    }

    /**
     * Обучение
     */
    public class WordEducation {
        private String word;
        private String translation;
        private String imageUrl;
        private String soundUrl;
        private EducationPage.Level level = EducationPage.Level.notseen;

        public WordEducation(String word) {
            this.word = word;
        }

        public String getWord() {
            return word;
        }

        public void setTranslation(String translation) {
            this.translation = translation;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public void setSoundUrl(String soundUrl) {
            this.soundUrl = soundUrl;
        }

        public String getTranslation() {
            return translation;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getSoundUrl() {
            return soundUrl;
        }

        public EducationPage.Level getLevel() {
            return level;
        }

        public void setLevel(EducationPage.Level level) {
            this.level = level;
        }
    }
}
