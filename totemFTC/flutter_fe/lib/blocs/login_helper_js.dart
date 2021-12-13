// ignore: avoid_web_libraries_in_flutter
import 'dart:js' as js;
import 'package:logging/logging.dart';

import 'session.dart';

const loginEventName = 'loginCompleted';

final Logger log = Logger('login_helper_js');

Function? callbackPopupJs;

LoginSession showLoginWindow(String url, LoginCallbackFunction onLoginCallback) {
  return JsLoginWindow(onLoginCallback)
    ..showLoginWindow(url);
}

class JsLoginWindow extends LoginSession {

  late final js.JsObject? popupWindow;

  JsLoginWindow(LoginCallbackFunction onLoginCallback): super(onLoginCallback);

  showLoginWindow(String url) {
    log.info('Web login $url');
    if (callbackPopupJs != null) {
      js.context.callMethod('removeEventListener', [loginEventName, callbackPopupJs]);
    }
    callbackPopupJs = js.allowInterop(callbackPopup);
    js.context.callMethod('addEventListener', [loginEventName, callbackPopupJs, js.JsObject.jsify({'once': true})]);
    popupWindow = js.JsObject.fromBrowserObject(js.context.callMethod('open', [url, 'loginWindow', 'width=600,height=600,left=600,top=200']));
  }

  void callbackPopup(js.JsObject event) {
    log.info('Callback from popup. $event');
    callbackPopupJs = null;
    try {
      popupWindow!.callMethod('close');
      onLoginCallback(event['detail']['callback'], event['detail']['referrer']);
    } catch (e) {
      log.warning('Close window error', e);
    }
  }
}
