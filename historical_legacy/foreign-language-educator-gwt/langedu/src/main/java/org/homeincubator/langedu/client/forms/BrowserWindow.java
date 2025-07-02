package org.homeincubator.langedu.client.forms;

import org.homeincubator.langedu.client.GwtUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

/**
 */
public class BrowserWindow {
    interface BrowserWindowUiBinder extends UiBinder<DivElement, BrowserWindow> {}
    private static BrowserWindowUiBinder ourUiBinder = GWT.create(BrowserWindowUiBinder.class);

    DivElement rootElement;
    @UiField InputElement browserAddress;
    @UiField IFrameElement browserContent;
    @UiField DivElement browserBookmarks;

    public BrowserWindow() {
        rootElement = ourUiBinder.createAndBindUi(this);
        GwtUtils.addEventListener(browserAddress, Event.ONKEYDOWN, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                // todo [!] добавить поддержку загрузки по focus lost или хотя бы индикатор незагруженности
                if (event.getTypeInt() == Event.ONKEYDOWN && event.getKeyCode() == KeyCodes.KEY_ENTER) {
                    String url = browserAddress.getValue();
                    if (!url.startsWith("http")) {
                        url = "http://" + url;
                    }
                    // todo [!] добавить поддержку прокси
                    browserContent.setSrc("" + url);
                }
            }
        });
        GwtUtils.addEventListener(browserContent, Event.ONDBLCLICK, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {

            }
        });
    }

    public DivElement getRootElement() {
        return rootElement;
    }

    public void setPosition(EventTarget eventTarget) {
        Element button = eventTarget.cast();
        rootElement.getStyle().setLeft(button.getAbsoluteLeft()  + button.getOffsetWidth() + 20, Style.Unit.PX);
        rootElement.getStyle().setTop(button.getAbsoluteTop() - button.getOffsetHeight() / 2 - 50, Style.Unit.PX);
    }
}
