import 'package:url_launcher/url_launcher.dart';
import 'package:logging/logging.dart';

import 'session.dart';

final Logger log = Logger('login_helper');

/// loginParams - oidc redirect login parameters string starting with '?'
LoginSession showLoginWindow(String url, LoginCallbackFunction onLoginCallback) {
  launch(url)
      .then((value) => log.info('Launched: $value'))
      .onError((error, stackTrace) =>
      log.severe('Error url launch ', error, stackTrace));

  return LoginSession(onLoginCallback);
}
