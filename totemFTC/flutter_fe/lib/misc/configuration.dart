import 'dart:async' show Future;
import 'dart:convert';
import 'dart:io' show Platform;

import 'package:flutter/foundation.dart' show kIsWeb, kReleaseMode;
import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:http/http.dart' as http;
import 'package:json_annotation/json_annotation.dart';
import 'package:logging/logging.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:yaml/yaml.dart';

part 'configuration.g.dart';

/*
https://stackoverflow.com/questions/44250184/setting-environment-variables-in-flutter
 */
class Configuration {

  Logger log = Logger('Configuration');

  static const sessionIdParam = 'sessionId';

  dynamic _doc;
  dynamic _buildInfo;
  String runProfile = '';
  SharedPreferences? _prefs;
  BackendConfiguration? _serverConfiguration;

  /// Note: configuration is not proper place for sessionId. But. Just to avoid
  /// circular dependency between crud_api and session
  String _sessionId = '';

  Future<void> load() async {
    try {
      if (_doc == null) {
        await _loadInternalPrefs();
        log.fine('Internal configuration loaded. ClientId: $clientId');
      }
      await _loadBackendConfiguration();
      log.finest('Backend configuration loaded');
    } catch (e,s) {
      log.warning('Configuration load error', e, s);
      rethrow;
    }
  }

  Future<void> _loadInternalPrefs() {
    WidgetsFlutterBinding.ensureInitialized();

    List<Future> tasks = [
      if (_prefs == null) SharedPreferences.getInstance().then((value) {
        _sessionId = value.getString(sessionIdParam) ?? '';
        _prefs = value;
      }),
      if (_doc == null) rootBundle.loadString('assets/config/config.yaml').then((value) => _doc = loadYaml(value)),
      if (_buildInfo == null) rootBundle.loadString('assets/config/build-info.yaml').then((value) {
        _buildInfo = loadYaml(value);
        runProfile = _buildInfo['run_profile'];
      }),
    ];

    return Future.wait(tasks).catchError((e,s) {
      log.severe('Internal configuration load task error', e, s);
      throw e;
    });
  }

  Future<void> _loadBackendConfiguration() async {
    var backend = backendUrl();
    var url = Uri.parse('$backend/api/utils/clientConfig?clientId=${Uri.encodeComponent(clientId)}');
    try {
      var response = await http.get(url);
      log.finest('Backend config response $response');
      if (response.statusCode != 200) throw Exception('Backend internal error ${response.statusCode}');
      _serverConfiguration = BackendConfiguration.fromJson(jsonDecode(utf8.decode(response.bodyBytes)));
    } on http.ClientException catch(e, s) {
      log.warning('Backend \'$url\' not ready', e, s);
      throw Exception('Backend \'$backend\' not ready');
    }
  }

  String get sessionId => _sessionId;

  set sessionId(String sessionId) {
    _prefs?.setString(sessionIdParam, sessionId);
    _sessionId = sessionId;
  }

  bool get isWeb  => kIsWeb;
  bool get isMobile  => !kIsWeb && (Platform.isAndroid || Platform.isIOS);
  String get nativeStr => kIsWeb ? "web" : "mobile";
  String prodStr = kReleaseMode ? "production" : "development";

  /// Client id used to select appropriate configuration on backend
  String get clientId => 'flutter-$nativeStr-$prodStr-$runProfile';

  String backendUrl() {
    return _getDoc(['backendUrl']);
  }

  /// OIDC provider config - client id + warning
  ConfigurationLoginProvider loginProviderConfig(LoginProvider loginProvider) {
    var name = loginProvider.name;
    var oidcClientId = _serverConfiguration!.oidcClientIds[name];
    if (oidcClientId == null) return ConfigurationLoginProvider(clientId: '', error: 'Provider $name config not found');
    return ConfigurationLoginProvider(clientId: oidcClientId,
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

  String get loginCallbackRoute => _getDoc(['oidc', 'loginCallbackRoute']);
  String get uiTitle =>_getDoc(['ui', 'title']);
  String get devTelegram => _getDoc(['ui', 'contacts', 'dev', 'telegram']);
  String get devPhone => _getDoc(['ui', 'contacts', 'dev', 'phone']);
  String get masterTelegram => _getDoc(['ui', 'contacts', 'master', 'telegram']);
  String get masterPhone => _getDoc(['ui', 'contacts', 'master', 'phone']);
  String get masterPhoneUi => _getDoc(['ui', 'contacts', 'master', 'phoneUi']);
  String get masterEmail => _getDoc(['ui', 'contacts', 'master', 'email']);
  String get serverString => '${_serverConfiguration!.serverId} ${_serverConfiguration!.serverRunProfile} ${_serverConfiguration!.serverBuild}';
  String get serverRunProfile => _serverConfiguration!.serverRunProfile;
  String get serverBuild => _serverConfiguration!.serverBuild;
  String get buildInfo => _buildInfo['build_info'] + ' ' + runProfile;

  dynamic _getDoc(List<String> path) {
    return _docPlain(['_${runProfile}_${prodStr}_$nativeStr' ,...path])
        ?? _docPlain(['_${runProfile}_$prodStr' ,...path])
        ?? _docPlain(['_${runProfile}_$nativeStr' ,...path])
        ?? _docPlain(['_$runProfile' ,...path])
        ?? _docPlain(['_${prodStr}_$nativeStr' ,...path])
        ?? _docPlain(['_$prodStr' ,...path])
        ?? _docPlain(['_$nativeStr' ,...path])
        ?? _docPlain(path);
  }

  dynamic _docPlain(List<String> path) {
    if (_doc == null) return null;
    var part = _doc;
    for (var p in path) {
      part = part[p];
      if (part == null) return null;
    }
    // log.finest('Config. Path: $path. Result: $part');
    return part;
  }
}


class ConfigurationLoginProvider {
  String clientId;
  String? error;
  String? warning;

  ConfigurationLoginProvider({required this.clientId, this.error, this.warning});
}


@JsonSerializable(explicitToJson: true)
class BackendConfiguration {
  String serverId;
  String serverRunProfile;
  String serverBuild;
  Map<String, String> oidcClientIds;

  BackendConfiguration({required this.serverId, required this.serverRunProfile, required this.serverBuild, required this.oidcClientIds});

  factory BackendConfiguration.fromJson(Map<String, dynamic> json) => _$BackendConfigurationFromJson(json);
  Map<String, dynamic> toJson() => _$BackendConfigurationToJson(this);
}
