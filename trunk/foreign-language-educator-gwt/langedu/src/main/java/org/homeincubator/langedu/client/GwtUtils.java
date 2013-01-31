package org.homeincubator.langedu.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

/**
 */
public class GwtUtils {
    public static void addEventListener(Element element, int eventBits, EventListener listener) {
        com.google.gwt.user.client.Element castElem = element.cast();
        DOM.sinkEvents(castElem, eventBits);
        DOM.setEventListener(castElem, listener);
    }

    public static void stopEvent(Event event) {
        event.stopPropagation();
        event.preventDefault();
    }

}
