import 'dart:async';
import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/crud_api.dart';
import 'package:flutter_fe/blocs/crud_user.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:logging/logging.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:http/http.dart' as http;

import '../misc/configuration.dart';
import '../misc/utils.dart';
import 'bloc_provider.dart';
import 'login_helper.dart' if (dart.library.js) 'login_helper_js.dart' as login_helper;

part 'session.g.dart';

class Session {
  static final Logger log = Logger('Session');

  final Configuration _configuration;
  final CrudApi _api;
  final StreamController<LoginStateInfo> _loginController = StreamController<LoginStateInfo>();
  late final Sink<LoginStateInfo> _loginStateIn;
  late final Stream<LoginStateInfo> _loginState;

  CrudEntityUser _user = emptyUser;
  CrudEntityUser get user => _user;

  Session(Injector injector):
        _configuration = injector.get<Configuration>(),
        _api = injector.get<CrudApi>()
  {
    _loginState = _loginController.stream.asBroadcastStream();
    _loginStateIn = _loginController.sink;
  }

  SessionBloc bloc() {
    return SessionBloc(this);
  }

  void dispose() {
    _loginController.close();
  }
  
  void login(LoginProvider provider) {
    final redirectUrl = _configuration.loginRedirectUrl();
    final clientId = _configuration.loginProviderClientId(provider.name);
    // client_id=<client_id>&redirect_uri=<redirect_uri>&state=<state>&response_type=code&scope=<scope>&nonce=<nonce>
    final url = provider.url
        + (provider.url.contains('?') ? '&' : '?')
        + 'client_id=' + clientId
        + '&state=my_state'
            '&response_type=code'
            '&scope=' + provider.scopes.join('+')
        + '&nonce=' + getRandomString(10)
        + '&redirect_uri=' + Uri.encodeComponent(redirectUrl);
    log.info('Launch login ${provider.name} $url');
    login_helper.showLoginWindow(url, (loginParams) => onLoginCallback(loginParams, provider));
  }

  void onLoginCallback(String loginParams, LoginProvider provider) async {
    if (loginParams.contains('error')) {
      _loginStateIn.add(LoginStateInfo(LoginState.error, loginParams));
      return;
    }
    _loginStateIn.add(LoginStateInfo(LoginState.inProgress));
    Uri? uri;
    http.Response? loginResponse;
    try {
      uri = Uri.parse('${_configuration.backendUrl()}/api/login/callback/${provider.name}$loginParams&action=login&client=${_configuration.clientId()}');
      loginResponse = await http.get(uri);
      if (loginResponse.statusCode != 200) {
        log.severe('Error processing login $uri ${loginResponse.statusCode}\n${loginResponse.body}');
        _loginStateIn.add(LoginStateInfo(LoginState.error, loginResponse.body));
        return;
      }
      var login = EntityLogin.fromJson(jsonDecode(loginResponse.body));
      _configuration.sessionId = login.sessionId;
      await loadUser();
      _loginStateIn.add(LoginStateInfo(LoginState.done));
    } on http.ClientException catch (e,s) {
      log.severe('Http Error processing login $uri', e, s);
      _loginStateIn.add(LoginStateInfo(LoginState.error, 'Backend Server Error'));
    } catch (e,s) {
      log.severe('[${e.runtimeType}] Error processing login $uri', e, s);
      if (loginResponse != null) {
        log.severe('loginResponseStatus: ${loginResponse.statusCode}\n${loginResponse.body}', e, s);
      }
      _loginStateIn.add(LoginStateInfo(LoginState.error, 'Error $e'));
    }
  }

  void logout() {
  }

  bool isLoggedIn() {
    return _configuration.sessionId.isNotEmpty;
  }

  Future<CrudEntityUser> loadUser() async {
    var json = await _api.requestJson('GET', '/api/user');
    _user = CrudEntityUser.fromJson(json);
    return _user;
  }

}

const loginProviders = <LoginProvider>[
  LoginProvider("facebook", "https://www.facebook.com/dialog/oauth", ["openid", "email", "public_profile", "user_gender", "user_link", "user_birthday", "user_location"], Icon(MdiIcons.facebook), true),
  LoginProvider("google", "https://accounts.google.com/o/oauth2/v2/auth", ["openid", "email", "profile"], Icon(MdiIcons.google), true),
  // ?force_confirm=yes
  LoginProvider("yandex", "https://oauth.yandex.ru/authorize", ["login:birthday", "login:email", "login:info", "login:avatar"], Icon(MdiIcons.alphaYCircleOutline), true),
];

class SessionBloc extends BlocBase {
  Session session;
  StreamSubscription? _loginStateSubscription;

  SessionBloc(this.session);

  listenLoginState(Function(LoginStateInfo) onData) {
    _loginStateSubscription = session._loginState.listen(onData);
  }

  @override
  void dispose() {
    if (_loginStateSubscription != null) {
      _loginStateSubscription!.cancel();
    }
  }
}

class LoginProvider {
  final String name;
  final String url;
  final List<String> scopes;
  final Icon icon;
  final bool warning;

  const LoginProvider(this.name, this.url, this.scopes, this.icon, this.warning);
}

class LoginStateInfo {
  LoginState state;
  String? description;

  LoginStateInfo(this.state, [this.description]);
}

enum LoginState {
  none, inProgress, error, done
}

@JsonSerializable()
class EntityLogin {
  String sessionId;
  EntityLoginUserType userType;

  EntityLogin({required this.sessionId, required this.userType});
  factory EntityLogin.fromJson(Map<String, dynamic> json) => _$EntityLoginFromJson(json);
  Map<String, dynamic> toJson() => _$EntityLoginToJson(this);
}

enum EntityLoginUserType {
 newUser, existing
}
