import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/crud_api.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:logging/logging.dart';
import 'package:yaml/yaml.dart';
import 'dart:async' show Future;
import 'package:flutter/services.dart' show rootBundle;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter/foundation.dart' show kIsWeb, kReleaseMode;
import 'dart:io' show Platform;

/*
https://stackoverflow.com/questions/44250184/setting-environment-variables-in-flutter
 */
class Configuration {

  Logger log = Logger('Configuration');

  static const sessionIdParam = 'sessionId';

  late final dynamic _doc;
  late SharedPreferences _prefs;

  /// Note: configuration is not proper place for sessionId. But. Just to avoid
  /// circular dependency between crud_api and session
  String _sessionId = '';

  Future<List> load() {
    try {
      WidgetsFlutterBinding.ensureInitialized();
      return Future.wait([
        SharedPreferences.getInstance().then((value) => _prefs = value),
        rootBundle.loadString('assets/config/config.yaml').then((value) => _doc = loadYaml(value)),
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

  bool get isWeb  => kIsWeb;
  bool get isMobile  => !kIsWeb && (Platform.isAndroid || Platform.isIOS);
  String get nativeStr => kIsWeb ? "web" : "mobile";
  String get prodStr => kReleaseMode ? "prod" : "dev";

  /// Client id used to select appropriate configuration on backend
  String clientId() {
    return 'flutter-$nativeStr-$prodStr';
  }

  String backendUrl() {
    return _getDoc(['backendUrl']);
  }

  /// OIDC provider config - client id + warning
  ConfigurationLoginProvider loginProviderConfig(LoginProvider loginProvider) {
    var name = loginProvider.name;
    final config = _doc['oidc']['providers'][name];
    // if (config == null) throw ApiException('Internal error', 'Login provider ${loginProvider.name} config not found');
    if (config == null) return ConfigurationLoginProvider(clientId: '', error: 'Provider ${name} config not found');
    return ConfigurationLoginProvider(clientId: (_getDoc(['oidc', 'providers', name, 'clientId']) ?? '').toString(),
      error: loginProviderErrorText(_getDoc(['oidc', 'providers', name, 'error']), loginProvider),
      warning: loginProviderErrorText(_getDoc(['oidc', 'providers', name, 'warning']), loginProvider),
    );
  }

  String? loginProviderErrorText(String? key, LoginProvider loginProvider) {
    if (key == null) return null;
    final text = _getDoc(['oidc', 'warnings', key]);
    return text.replaceAll('\${provider}', loginProvider.name);
  }

  String loginRedirectUrl(LoginProvider provider) {
    return (_getDoc(['oidc', 'redirectUrl']) as String)
        .replaceAll('\${backend_url}', backendUrl())
        .replaceAll('\${provider}', provider.name);
  }

  String loginCallbackUrl() {
    return (_getDoc(['oidc', 'callbackUrl']) as String);
  }

  String devTelegram() {
    return _getDoc(['contacts', 'dev', 'telegram']);
  }

  String devPhone() {
    return _getDoc(['contacts', 'dev', 'phone']);
  }

  String masterTelegram() {
    return _getDoc(['contacts', 'master', 'telegram']);
  }

  String masterPhone() {
    return _getDoc(['contacts', 'master', 'phone']);
  }

  String masterPhoneUi() {
    return _getDoc(['contacts', 'master', 'phoneUi']);
  }

  String masterEmail() {
    return _getDoc(['contacts', 'master', 'email']);
  }

  dynamic _getDoc(List<String> path) {
    return _docPlain(['_${prodStr}_$nativeStr' ,...path])
        ?? _docPlain(['_$prodStr' ,...path])
        ?? _docPlain(['_$nativeStr' ,...path])
        ?? _docPlain(path);
  }

  dynamic _docPlain(List<String> path) {
    var part = _doc;
    for (var p in path) {
      part = part[p];
      if (part == null) return null;
    }
    return part;
  }



}

class ConfigurationLoginProvider {
  String clientId;
  String? error;
  String? warning;

  ConfigurationLoginProvider({required this.clientId, this.error, this.warning});
}
