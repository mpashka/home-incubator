package org.homeincubator.langedu.client.forms;

import java.util.List;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.view.client.ListDataProvider;
import org.homeincubator.langedu.client.Educator;

/**
 */
public class PrepareWordsForm {
    private Educator educator;
    private List<Educator.WordEducation> words;

    interface PrepareWordsFormUiBinder extends UiBinder<HTMLPanel, PrepareWordsForm> {}
    private static PrepareWordsFormUiBinder ourUiBinder = GWT.create(PrepareWordsFormUiBinder.class);


    private HTMLPanel rootElement;
    @UiField CellTable<Educator.WordEducation> wordsTable;
    private ListDataProvider<Educator.WordEducation> dataProvider;

    public PrepareWordsForm(Educator educator) {
        this.educator = educator;
        rootElement = ourUiBinder.createAndBindUi(this);

        {
            // Create name column.
            TextColumn<Educator.WordEducation> wordColumn = new TextColumn<Educator.WordEducation>() {
                @Override
                public String getValue(Educator.WordEducation word) {
                    return word.getWord();
                }
            };
            wordsTable.addColumn(wordColumn);
        }

        {
            Column<Educator.WordEducation, String> translationColumn = new Column<Educator.WordEducation, String>(new EditTextCell()) {
                @Override
                public String getValue(Educator.WordEducation word) {
                    return word.getTranslation();
                }
            };
            translationColumn.setFieldUpdater(new FieldUpdater<Educator.WordEducation, String>() {
                @Override
                public void update(int index, Educator.WordEducation word, String value) {
                    word.setTranslation(value);
                }
            });
            wordsTable.addColumn(translationColumn);
        }

        {
            Column<Educator.WordEducation, String> imgColumn = new Column<Educator.WordEducation, String>(new EditTextCell()) {
                @Override
                public String getValue(Educator.WordEducation word) {
                    return word.getImageUrl();
                }
            };
            imgColumn.setFieldUpdater(new FieldUpdater<Educator.WordEducation, String>() {
                @Override
                public void update(int index, Educator.WordEducation word, String value) {
                    word.setImageUrl(value);
                }
            });
            wordsTable.addColumn(imgColumn);
        }



        // Create a data provider.
        dataProvider = new ListDataProvider<Educator.WordEducation>();
        dataProvider.addDataDisplay(wordsTable);

/*
        // Set the width of the table and put the table in fixed width mode.
        table.setWidth("100%", true);

        // Set the width of each column
        table.setColumnWidth(nameColumn, 35.0, Unit.PCT);
*/
    }

    public Element getRootElement() {
        return rootElement.getElement();
    }

    public void setWords(List<Educator.WordEducation> words) {
        this.words = words;
        dataProvider.setList(words);
    }


    @UiHandler("finishLink")
    public void finishPrepareWords(ClickEvent event) {
        educator.finishPrepareWords();
        event.preventDefault();
        event.stopPropagation();
    }
}