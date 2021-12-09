// ignore: avoid_web_libraries_in_flutter
import 'dart:js' as js;
import 'package:logging/logging.dart';

import 'session.dart';

final Logger log = Logger('login_helper_js');

LoginSession showLoginWindow(String url, LoginCallbackFunction onLoginCallback) {
  return JsLoginWindow(onLoginCallback)
    ..showLoginWindow(url);
}

class JsLoginWindow extends LoginSession {

  js.JsObject? popupWindow;

  JsLoginWindow(LoginCallbackFunction onLoginCallback): super(onLoginCallback);

  showLoginWindow(String url) {
    log.info('Web login $url');
    var callbackPopupJs = js.allowInterop(callbackPopup);
    js.context['onLoginCompleted'] = callbackPopupJs;
    // html.window.onLoginCompleted = callbackPopupJs;
    // popupWindow = html.window.open('http://localhost:8083/popup-page.html?session_id=my-session&user_type=new', 'loginWindow', 'width=600,height=600,left=600,top=200');
    popupWindow = js.JsObject.fromBrowserObject(js.context.callMethod('open', [url, 'loginWindow', 'width=600,height=600,left=600,top=200']));
  }

  void callbackPopup(String urlParams) {
    log.info('Callback from popup. $urlParams');
    try {
      popupWindow!.callMethod('close');
      onLoginCallback(urlParams);
    } catch (e) {
      log.warning('Close window error', e);
    }
  }
}

