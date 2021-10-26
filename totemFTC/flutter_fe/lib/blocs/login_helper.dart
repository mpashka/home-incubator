import 'package:url_launcher/url_launcher.dart';
import 'package:logging/logging.dart';

final Logger log = Logger('login_helper');

void showLoginWindow(String url, void Function(String loginParams) onLoginCallback) {
  launch(url)
      .then((value) => log.info('Launched: $value'))
      .onError((error, stackTrace) =>
      log.severe('Error url launch ', error, stackTrace));
}
