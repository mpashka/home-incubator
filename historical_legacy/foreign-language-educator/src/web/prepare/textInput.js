function startTextInput() {
    $('#PageTextInput textarea#InputTextArea').text(
        'Example text \
        used to test application \
        tr \
        Example text \
        used to test application \
        tr \
        Description: Bind an event handler to the "click" \
         JavaScript event, or trigger that event on an element. \
        \
        version added: 1.0.click( handler(eventObject) ) \
        handler(eventObject)A function to execute each time the event is triggered. \
        \
        version added: 1.0.click() \
        This method is a shortcut for .bind(\'click\', \
        handler) in the first variation, and .trigger(\'click\') in the second. \
        \
        The click event is sent to an element when \
        the mouse pointer is over the element, and \
        the mouse button is pressed and released. Any HTML \
        element can receive this event.\
        \
        \
        ');

}


var text;
function finishTextInput() {
    text = $('#PageTextInput textarea#InputTextArea').text();
    loadPage('prepare/selectWords');
}
