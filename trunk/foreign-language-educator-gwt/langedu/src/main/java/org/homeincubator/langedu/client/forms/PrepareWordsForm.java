package org.homeincubator.langedu.client.forms;

import java.util.List;

import org.homeincubator.langedu.client.Educator;
import org.homeincubator.langedu.client.GwtUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.AnchorElement;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.dom.client.Text;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

/**
 */
public class PrepareWordsForm {
    interface PrepareWordsFormUiBinder extends UiBinder<DivElement, PrepareWordsForm> {}
    private static PrepareWordsFormUiBinder ourUiBinder = GWT.create(PrepareWordsFormUiBinder.class);


    private Educator educator;
    private List<Educator.WordEducation> words;
    private DivElement rootElement;
    @UiField AnchorElement finishLink;
    @UiField TableSectionElement wordsTableBody;

    public PrepareWordsForm(Educator educator) {
        this.educator = educator;
        rootElement = ourUiBinder.createAndBindUi(this);
        GwtUtils.addEventListener(finishLink, Event.ONCLICK, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                finishPrepareWords(event);
                GwtUtils.stopEvent(event);
            }
        });
    }

    public Element getRootElement() {
        return rootElement;
    }

    public void setWords(List<Educator.WordEducation> words) {
        this.words = words;
        for (Educator.WordEducation word : words) {
            TableRowElement tr = wordsTableBody.insertRow(-1);
            Document document = Document.get();

            {   TableCellElement wordCell = tr.insertCell(-1);
                wordCell.appendChild(document.createTextNode(word.getWord()));
            }
            {   TableCellElement translationCell = tr.insertCell(-1);
                DivElement divElement = document.createDivElement();
                InputElement translationInput = document.createTextInputElement();
                translationInput.setValue(word.getTranslation());
                divElement.appendChild(translationInput);
                ButtonElement browserButtonElement = document.createPushButtonElement();
                GwtUtils.addEventListener(browserButtonElement, Event.ONCLICK, new EventListener() {
                    @Override
                    public void onBrowserEvent(Event event) {
                        educator.showBrowserWindow(event.getEventTarget());
                    }
                });
                divElement.appendChild(browserButtonElement);
                translationCell.appendChild(divElement);
            }
            {   TableCellElement imageCell = tr.insertCell(-1);
                InputElement imageInput = document.createTextInputElement();
                imageInput.setValue(word.getImageUrl());
                imageCell.appendChild(imageInput);
            }
            {   TableCellElement soundCell = tr.insertCell(-1);
                InputElement soundInput = document.createTextInputElement();
                soundInput.setValue(word.getSoundUrl());
                soundCell.appendChild(soundInput);
            }
        }
    }


    public void finishPrepareWords(Event event) {
        words.clear();
        NodeList<TableRowElement> rows = wordsTableBody.getRows();
        for (int i = 0; i < rows.getLength(); i++) {
            Educator.WordEducation wordEducation;

            TableRowElement row = rows.getItem(i);
            NodeList<TableCellElement> cells = row.getCells();
            {   Text wordCell = (Text) cells.getItem(0).getChildNodes().getItem(0);
                wordEducation = new Educator.WordEducation(wordCell.getData());
            }
            {   InputElement translationCell = (InputElement) cells.getItem(1).getChildNodes().getItem(0);
                wordEducation.setTranslation(translationCell.getValue());
            }
            {   InputElement imageCell = (InputElement) cells.getItem(2).getChildNodes().getItem(0);
                wordEducation.setImageUrl(imageCell.getValue());
            }
            {   InputElement soundCell = (InputElement) cells.getItem(3).getChildNodes().getItem(0);
                wordEducation.setSoundUrl(soundCell.getValue());
            }

            words.add(wordEducation);
        }

        educator.finishPrepareWords(words);
    }
}