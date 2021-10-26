import 'package:flutter/material.dart';
import 'package:logging/logging.dart';
import 'package:yaml/yaml.dart';
import 'dart:async' show Future;
import 'package:flutter/services.dart' show rootBundle;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter/foundation.dart' show kIsWeb;

/*
https://stackoverflow.com/questions/53811932/flutter-multiline-for-text
https://flutter.dev/docs/development/ui/assets-and-images
https://flutter.dev/docs/deployment/android (release)

https://stackoverflow.com/questions/44250184/setting-environment-variables-in-flutter
 */
class Configuration {
  Logger log = Logger('Configuration');

  var _doc;
  late SharedPreferences _prefs;

  Future load() {
    try {
      WidgetsFlutterBinding.ensureInitialized();
      return Future.wait([
        SharedPreferences.getInstance().then((value) => _prefs = value),
        rootBundle.loadString('assets/config/config-dev.yaml').then((value) => _doc = loadYaml(value)),
      ]);
    } catch (e) {
      log.warning('Error loading asset', e);
      rethrow;
    }
  }

  bool isWeb() {
    return kIsWeb;
  }

  /// Client id used to select appropriate configuration on
  /// backend
  String clientId() {
    return 'flutter-${isWeb() ? "web" : "native"}-${_doc['config']}';
  }

  String backendUrl() {
    return _doc['backendUrl'];
  }

  /// OIDC provider client id
  String loginProviderClientId(String provider) {
    return _doc['oidc']['providers'][provider]['clientId'];
  }

  String loginRedirectUrl() {
    return _doc['oidc'][kIsWeb ? 'redirectUrlWeb' : 'redirectUrl'];
  }
}
