import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/crud_api.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:logging/logging.dart';
import 'package:yaml/yaml.dart';
import 'dart:async' show Future;
import 'package:flutter/services.dart' show rootBundle;
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter/foundation.dart' show kIsWeb, kReleaseMode;
import 'dart:io' show Platform;
import 'package:http/http.dart' as http;

part 'configuration.g.dart';

/*
https://stackoverflow.com/questions/44250184/setting-environment-variables-in-flutter
 */
class Configuration {

  Logger log = Logger('Configuration');

  static const sessionIdParam = 'sessionId';

  late final dynamic _doc;
  late final dynamic _buildInfo;
  late SharedPreferences _prefs;
  late ServerConfiguration _serverConfiguration;

  /// Note: configuration is not proper place for sessionId. But. Just to avoid
  /// circular dependency between crud_api and session
  String _sessionId = '';

  Future<List> load() {
    try {
      WidgetsFlutterBinding.ensureInitialized();
      return Future.wait([
        SharedPreferences.getInstance().then((value) => _prefs = value),
        rootBundle.loadString('assets/config/config.yaml').then((value) => _doc = loadYaml(value)),
        rootBundle.loadString('assets/config/build-info.yaml').then((value) => _buildInfo = loadYaml(value)),
        loadServerConfiguration(),
      ]).then((value) {
        _sessionId = _prefs.getString(sessionIdParam) ?? '';
        return value;
      }).catchError((e,s) {
        log.severe('Configuration load error', e, s);
        throw e;
      });
    } catch (e) {
      log.warning('Error loading asset', e);
      rethrow;
    }
  }

  Future<void> loadServerConfiguration() async {
    var url = Uri.parse('${backendUrl()}/clientConfig?${Uri.encodeComponent(clientId())}');
    var response = await http.get(url);
    if (response.statusCode != 200) throw ServerNotReadyException();
    _serverConfiguration = ServerConfiguration.fromJson(jsonDecode(utf8.decode(response.bodyBytes)));
  }

  String get sessionId => _sessionId;

  set sessionId(String sessionId) {
    _prefs.setString(sessionIdParam, sessionId);
    _sessionId = sessionId;
  }

  bool get isWeb  => kIsWeb;
  bool get isMobile  => !kIsWeb && (Platform.isAndroid || Platform.isIOS);
  String get nativeStr => kIsWeb ? "web" : "mobile";
  String prodStr = kReleaseMode ? "production" : "development";

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
    return ConfigurationLoginProvider(clientId: _serverConfiguration.oidcClientIds[name]!,
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

  String loginCallbackRoute() {
    return (_getDoc(['oidc', 'loginCallbackRoute']) as String);
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

  String serverString() {
    return '${_serverConfiguration.serverId} ${_serverConfiguration.serverRunProfile} ${_serverConfiguration.serverBuild}';
  }

  String serverRunProfile() {
    return _serverConfiguration.serverRunProfile;
  }

  String serverBuild() {
    return _serverConfiguration.serverBuild;
  }

  String buildInfo() {
    return _buildInfo['build_info'];
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

class ServerNotReadyException implements Exception {
}

@JsonSerializable(explicitToJson: true)
class ServerConfiguration {
  String serverId;
  String serverRunProfile;
  String serverBuild;
  Map<String, String> oidcClientIds;

  ServerConfiguration({required this.serverId, required this.serverRunProfile, required this.serverBuild, required this.oidcClientIds});

  factory ServerConfiguration.fromJson(Map<String, dynamic> json) => _$ServerConfigurationFromJson(json);
  Map<String, dynamic> toJson() => _$ServerConfigurationToJson(this);
}
