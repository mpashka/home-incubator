package org.homeincubator.langedu.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.homeincubator.langedu.client.forms.BrowserWindow;
import org.homeincubator.langedu.client.forms.PrepareWordsForm;
import org.homeincubator.langedu.client.forms.SelectWordsForm;
import org.homeincubator.langedu.client.forms.TextInputForm;
import org.homeincubator.langedu.client.forms.education.DragWordsEasy;
import org.homeincubator.langedu.client.forms.education.ShowWord;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 */
public class Educator {

    private static final Logger log = Logger.getLogger(Educator.class.getName());

//    private static final EducationPage.Level MIN_LEVEL = EducationPage.Level.values()[0];
//    private static final int MAX_LEVEL = EducationPage.Level.values().length-1;
    private static final int LEVEL_COUNT = EducationPage.Level.values().length;

    private Element mainDiv;
    private Element form;
    private List<WordEducation> words = new ArrayList<WordEducation>();
    private TextInputForm textInputForm = new TextInputForm(this);
    private SelectWordsForm selectWordsForm = new SelectWordsForm(this);
    private PrepareWordsForm prepareWordsForm = new PrepareWordsForm(this);
    private BrowserWindow browserWindow = new BrowserWindow();
    private EducationPage[] educationPagesArray = new EducationPage[] {
            new ShowWord(this),
            new DragWordsEasy(this)
    };
    private Map<EducationPage.Level, EducationPage> educationPages = new HashMap<EducationPage.Level, EducationPage>();

    public int[] questionLastRun = new int[LEVEL_COUNT];


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
        log.finest("Display form");

        if (this.form == null) {
            mainDiv.appendChild(form);
        } else {
            mainDiv.replaceChild(form, this.form);
        }
        this.form = form;
    }

    private static final RegExp wordPattern = RegExp.compile("[a-zA-Z\\u0080-\\u2000]+", "g");
    public void finishTextInput(String text) {
        List<WordInfo> textWords = new ArrayList<WordInfo>();
        MatchResult matchResult;
        wordPattern.setLastIndex(0);
        int lastIndex = 0;
        while ((matchResult = wordPattern.exec(text)) != null) {
            String word = matchResult.getGroup(0);
//            log.finest("Next word:" + word);
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
            word.setTranslation("-" + wordName + "-trans");
            word.setImageUrl("-" + wordName + "-img");
            word.setSoundUrl("-" + wordName + "-snd");
            words.add(word);
        }
        prepareWordsForm.setWords(words);
        displayForm(prepareWordsForm.getRootElement());
    }

    public void finishPrepareWords(List<WordEducation> words) {
        this.words = words;
        nextEducation();
    }

    public void nextEducation() {
        log.finest("Next education");
        for (int i = 0; i < questionLastRun.length; i++) {
            questionLastRun[i]++;
        }
        EducationPage educationPage = selectEductation();
        EducationPage.Level level = educationPage.getLevel();
        questionLastRun[level.ordinal()] = 0;

        log.finest("    Page selected [" + level + "] " + GwtUtils.getClassSimpleName(educationPage.getClass()));
        List<WordEducation> allEducationWords = new ArrayList<WordEducation>();
        for (WordEducation word : words) {
            if (word.getLevel() == level) {
                allEducationWords.add(word);
            }
        }
        log.finest("    All suitable words:" + allEducationWords.size());
        List<WordEducation> educationWords = new ArrayList<WordEducation>();
        for (int i = 0; i < educationPage.getWordsCount() && !allEducationWords.isEmpty(); i++) {
            WordEducation word = RandomSelector.randomSelect(allEducationWords, true);
            educationWords.add(word);
        }
        log.finest("    Lesson words [" + educationWords.size() + "]");
        educationPage.educate(educationWords);
        displayForm(educationPage.getRootElement());
    }

    private EducationPage selectEductation() {
        int[] wordsCount = new int[LEVEL_COUNT];
        for (WordEducation word : words) {
            int level = word.getLevel().ordinal();
            wordsCount[level]++;
        }

        RandomSelector<EducationPage.Level> levelSelector = new RandomSelector<EducationPage.Level>();
        for (int i = 0; i < wordsCount.length; i++) {
            if (wordsCount[i] > 0) {
                double probability = wordsCount[i] * Math.pow(questionLastRun[i], 1.5);
                log.finest("Level[" + i + "] probability: " + probability);
                levelSelector.add(EducationPage.Level.values()[i], probability);
            }
        }
        EducationPage.Level level = levelSelector.select();
        log.finest("Level selected: " + level);

        List<EducationPage> appropriatePages = new ArrayList<EducationPage>();
        for (EducationPage educationPage : educationPagesArray) {
            if (educationPage.getLevel() == level) {
                appropriatePages.add(educationPage);
            }
        }
        EducationPage educationPage = RandomSelector.randomSelect(appropriatePages, false);

        return educationPage;
    }

    public void showBrowserWindow(EventTarget eventTarget) {
        mainDiv.appendChild(browserWindow.getRootElement());
        browserWindow.setPosition(eventTarget);
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
    public static final class WordEducation {
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
