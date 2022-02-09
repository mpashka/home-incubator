import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/crud_api.dart';
import 'package:flutter_fe/blocs/crud_user.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:logging/logging.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';

import '../main.dart';
import '../misc/configuration.dart';
import '../misc/utils.dart';
import 'bloc_provider.dart';
import 'login_helper.dart' if (dart.library.js) 'login_helper_js.dart' as login_helper;

part 'session.g.dart';

class Session {
  static final Logger log = Logger('Session');

  final Configuration _configuration;
  final CrudApi _backend;

  CrudEntityUser _user = emptyUser;
  CrudEntityUser get user => _user;
  LoginSession? _loginSession;

  Session(Injector injector):
        _configuration = injector.get<Configuration>(),
        _backend = injector.get<CrudApi>();

  /// In order to provide better user experience and reduce latency we process all urls on client side.
  /// For web-based client we use {@link web/login-callback.html} as redirect URL
  /// For native client we use site url + deep linking + site/.well-known in order to confirm ownership and get call back in router (still TBD;)
  Future<CrudEntityUser?> login(LoginProvider provider, {SessionBloc? sessionBloc}) async {
    return _login(provider, false, sessionBloc: sessionBloc);
  }

  Future<CrudEntityUser?> link(LoginProvider provider, {SessionBloc? sessionBloc}) {
    return _login(provider, true, sessionBloc: sessionBloc);
  }

  Future<CrudEntityUser?> _login(LoginProvider provider, bool link, {SessionBloc? sessionBloc}) {
    final completer = Completer<CrudEntityUser?>();
    final redirectUrl = _configuration.loginRedirectUrl(provider);
    final config = _configuration.loginProviderConfig(provider);
    final clientId = config.clientId;
    // client_id=<client_id>&redirect_uri=<redirect_uri>&state=<state>&response_type=code&scope=<scope>&nonce=<nonce>
    final url = provider.url
        + (provider.url.contains('?') ? '&' : '?')
        + 'client_id=' + clientId
        + '&state=state_client_flutter'
            '&response_type=code'
            '&scope=' + provider.scopes.join('+')
        + '&nonce=' + getRandomString(10)
        + '&redirect_uri=' + Uri.encodeComponent(redirectUrl);
    log.info('Launch login ${provider.name} $url');
    _loginSession = login_helper.showLoginWindow(url, (loginParams, referrer) =>
        _onLoginCallback(loginParams, provider, link, sessionBloc: sessionBloc)
            .then((value) => completer.complete(value)));
    return completer.future;
  }

  Future<CrudEntityUser?> _onLoginCallback(String loginParams, LoginProvider provider, bool link, {SessionBloc? sessionBloc}) async {
    _loginSession = null;
    if (loginParams.contains('error')) {
      sessionBloc?.state = LoginStateInfo(LoginState.error, loginParams);
      return null;
    }
    sessionBloc?.state = LoginStateInfo(LoginState.inProgress);
    try {
      var login = EntityLogin.fromJson(await _backend.requestJson('GET', '/api/login/${link ? 'linkCallback' : 'loginCallback'}/${provider.name}$loginParams', params: {'clientId': _configuration.clientId()}, auth: link));
      if (!link) {
        _configuration.sessionId = login.sessionId;
      }
      final user = await loadUser();
      sessionBloc?.state = LoginStateInfo(LoginState.done);
      return user;
    } catch (e, s) {
      log.warning('Login callback processing error', e, s);
      sessionBloc?.state = LoginStateInfo(LoginState.error, 'Error $e');
    }
  }

  void onLoginCallback(String loginParams) {
    if (_loginSession != null) {
      _loginSession!.onLoginCallback(loginParams, '');
    } else {
      log.warning('Received login callback out of session');
    }
  }

  void clearLoginCallback() {
    _loginSession = null;
  }

  Future<void> logout(BuildContext context) async {
    await _backend.request('GET', '/api/login/logout');
    _configuration.sessionId = '';
    _user = emptyUser;
    Navigator.pushReplacementNamed(context, MyApp.homeRouteName);
  }

  bool isLoggedIn() {
    return _configuration.sessionId.isNotEmpty;
  }

  Future<CrudEntityUser> loadUser() async {
    _user = CrudEntityUser.fromJson(await _backend.requestJson('GET', '/api/user/current'));
    return _user;
  }

}

const loginProviders = <LoginProvider>[
  LoginProvider("facebook", "https://www.facebook.com/dialog/oauth", ["openid", "email", "public_profile", "user_gender", "user_link", "user_birthday", "user_location"], Icon(MdiIcons.facebook)),
  LoginProvider("google", "https://accounts.google.com/o/oauth2/v2/auth", ["openid", "email", "profile"], Icon(MdiIcons.google)),
  LoginProvider("apple", "", [], Icon(MdiIcons.apple)),
  LoginProvider("instagram", "https://api.instagram.com/oauth/authorize", ["openid", "user_profile"], Icon(MdiIcons.instagram)),
  LoginProvider("twitter", "", [], Icon(MdiIcons.twitter)),
  LoginProvider("github", "https://github.com/login/oauth/authorize", ["read:user", "user:email"], Icon(MdiIcons.github)),
  LoginProvider("vk", "https://oauth.vk.com/authorize", ["email"], FaIcon(FontAwesomeIcons.vk)),
  LoginProvider("mailru", "https://oauth.mail.ru/login", ["openid", "userinfo", "email", "profile", "offline_access"], Icon(Icons.alternate_email)),
  LoginProvider("okru", "https://connect.ok.ru/oauth/authorize", ["VALUABLE_ACCESS;GET_EMAIL;LONG_ACCESS_TOKEN"], Icon(MdiIcons.odnoklassniki)),
  LoginProvider("yandex", "https://oauth.yandex.ru/authorize?force_confirm=yes", ["login:birthday", "login:email", "login:info", "login:avatar"], FaIcon(FontAwesomeIcons.yandex)),
  LoginProvider("amazon", "https://www.amazon.com/ap/oa", ["profile"], Icon(FontAwesomeIcons.amazon)),
];

class SessionBloc extends BlocBaseState<LoginStateInfo> {
  StreamSubscription? _loginStateSubscription;

  SessionBloc({LoginStateInfo state = const LoginStateInfo(LoginState.none), required BlocProvider provider, String? name}): super(provider: provider, state: state, name: name);

  listenLoginState(Function(LoginStateInfo loginStateInfo) onData) {
    cancelSubscription();
    _loginStateSubscription = stateOut.listen(onData);
  }

  Future<CrudEntityUser?> login(LoginProvider provider) {
    return session.login(provider, sessionBloc: this);
  }

  @override
  void dispose() {
    super.dispose();
  }

  void cancelSubscription() {
    _loginStateSubscription?.cancel();
    _loginStateSubscription = null;
  }

  void reset() {
    state = LoginStateInfo(LoginState.none);
  }
}

class LoginProvider {
  final String name;
  final String url;
  final List<String> scopes;
  final Widget icon;

  const LoginProvider(this.name, this.url, this.scopes, this.icon);
}

class LoginStateInfo {

  final LoginState state;
  final String? description;

  const LoginStateInfo(this.state, [this.description]);
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

typedef LoginCallbackFunction = void Function(String loginParams, String referrer);

class LoginSession {
  LoginCallbackFunction onLoginCallback;

  LoginSession(this.onLoginCallback);
}