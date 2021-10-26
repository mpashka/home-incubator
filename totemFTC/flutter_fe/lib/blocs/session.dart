import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:logging/logging.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:http/http.dart' as http;

import '../misc/configuration.dart';
import '../misc/utils.dart';
import 'base.dart';
import 'login_helper.dart' if (dart.library.js) 'login_helper_js.dart' as login_helper;

class Session {
  static final Logger log = Logger('SessionBlock');

  String _sessionId = '';
  late Configuration _configuration;
  final StreamController<LoginStateInfo> _loginController = StreamController<LoginStateInfo>();
  late final Sink<LoginStateInfo> _loginStateIn;
  late final Stream<LoginStateInfo> _loginState;

  Session(Injector injector): _configuration = injector.get<Configuration>() {
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
        + '?client_id=' + clientId
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
    var uri = Uri.parse('${_configuration.backendUrl()}/api/login/callback/${provider.name}$loginParams&action=login&client=${_configuration.clientId()}');
    final loginResponse = await http.get(uri);
    if (loginResponse.statusCode != 200) {
      log.severe('Error processing login $uri ${loginResponse.statusCode}\n${loginResponse.body}');
      _loginStateIn.add(LoginStateInfo(LoginState.error, loginResponse.body));
      return;
    }
    try {
      var login = jsonDecode(loginResponse.body);
      _sessionId = login['sessionId'];
      String userType = login['userType'];
      _loginStateIn.add(LoginStateInfo(LoginState.done));
    } catch (e,s) {
      log.severe('Error processing login $uri ${loginResponse.statusCode}\n${loginResponse.body}', e, s);
      _loginStateIn.add(LoginStateInfo(LoginState.error, 'Internal error $e'));
    }
  }

  void logout() {
  }

  bool isLoggedIn() {
    return _sessionId.length > 0;
  }

}

const loginProviders = <LoginProvider>[
  LoginProvider("facebook", "https://www.facebook.com/dialog/oauth", ["openid", "email", "public_profile", "user_gender", "user_link", "user_birthday", "user_location"], Icon(MdiIcons.facebook), true),
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
