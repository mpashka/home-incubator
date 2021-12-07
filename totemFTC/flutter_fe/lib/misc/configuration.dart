import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/crud_api.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:logging/logging.dart';
import 'package:yaml/yaml.dart';
import 'dart:async' show Future;
import 'package:flutter/services.dart' show rootBundle;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter/foundation.dart' show kIsWeb;

/*
https://stackoverflow.com/questions/44250184/setting-environment-variables-in-flutter
 */
class Configuration {

  Logger log = Logger('Configuration');

  static const sessionIdParam = 'sessionId';

  var _doc;
  late SharedPreferences _prefs;

  /// Note: configuration is not proper place for sessionId. But. Just to avoid
  /// circular dependency between crud_api and session
  String _sessionId = '';

  Future<List> load() {
    try {
      WidgetsFlutterBinding.ensureInitialized();
      return Future.wait([
        SharedPreferences.getInstance().then((value) => _prefs = value),
        rootBundle.loadString('assets/config/config-dev.yaml').then((value) => _doc = loadYaml(value)),
      ]).then((value) {
        _sessionId = _prefs.getString(sessionIdParam) ?? '';
        return value;
      } ,onError: (e,s) => log.severe('Configuration load error', e, s));
    } catch (e) {
      log.warning('Error loading asset', e);
      rethrow;
    }
  }

  String get sessionId => _sessionId;

  set sessionId(String sessionId) {
    _prefs.setString(sessionIdParam, sessionId);
    _sessionId = sessionId;
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
  ConfigurationLoginProvider loginProviderConfig(LoginProvider loginProvider) {
    final config = _doc['oidc']['providers'][loginProvider.name];
    // if (config == null) throw ApiException('Internal error', 'Login provider ${loginProvider.name} config not found');
    if (config == null) return ConfigurationLoginProvider(clientId: '', error: 'Provider ${loginProvider.name} config not found');
    return ConfigurationLoginProvider(clientId: config['clientId'] ?? '',
      error: loginProviderErrorText(config['error'], loginProvider),
      warning: loginProviderErrorText(config['warning'], loginProvider),
    );
  }

  String? loginProviderErrorText(String? key, LoginProvider loginProvider) {
    if (key == null) return null;
    final text = _doc['oidc']['warnings'][key];
    return text.replaceAll('\${provider}', loginProvider.name);
  }

  String loginProviderClientId(String provider) {
    return _doc['oidc']['providers'][provider]['clientId'];
  }

  String loginRedirectUrl() {
    return _doc['oidc'][kIsWeb ? 'redirectUrlWeb' : 'redirectUrl'];
  }

  String devTelegram() {
    return _doc['contacts']['dev']['telegram'];
  }

  String devPhone() {
    return _doc['contacts']['dev']['phone'];
  }

  String masterTelegram() {
    return _doc['contacts']['master']['telegram'];
  }

  String masterPhone() {
    return _doc['contacts']['master']['phone'];
  }

  String masterPhoneUi() {
    return _doc['contacts']['master']['phoneUi'];
  }

  String masterEmail() {
    return _doc['contacts']['master']['email'];
  }

  dynamic get doc => _doc;
}

class ConfigurationLoginProvider {
  String clientId;
  String? error;
  String? warning;

  ConfigurationLoginProvider({required this.clientId, this.error, this.warning});
}
