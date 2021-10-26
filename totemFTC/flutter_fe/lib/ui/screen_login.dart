import 'dart:async';
import 'dart:developer' as developer;

import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/misc/initializer.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:logging/logging.dart';

import 'drawer.dart';
import 'widgets/scroll_list_selector.dart';
import 'widgets/ui_subscription.dart';
import 'widgets/ui_attend.dart';

class LoginScreen extends StatefulWidget {
  final Injector _injector;

  const LoginScreen(this._injector, {Key? key}) : super(key: key);

  @override
  State<LoginScreen> createState() {
    return LoginScreenState(_injector);
  }
}

class LoginScreenState extends State<LoginScreen> {
  static final Logger log = Logger('LoginScreen');

  final SessionBloc _session;
  bool dialogVisible = false;

  LoginScreenState(Injector injector): _session = injector.get<Session>().bloc();


  @override
  void initState() {
    super.initState();
    _session.listenLoginState((loginStateInfo) {
      switch (loginStateInfo.state) {
        case LoginState.done:
          Navigator.pushReplacementNamed(context, '/',);
          break;
        case LoginState.inProgress:
          _showDialog(const AlertDialog(
            title: Text('Loading login data'),
            content: Center(heightFactor: 1,child: CircularProgressIndicator()),
          ));
          break;
        case LoginState.error:
          _showDialog(AlertDialog(
            title: const Text('Login error'),
            content: Column(children: [
              const Text('Login error'),
              Text(loginStateInfo.description!),
            ],),
          ));
          break;
        default:
      }
    });
  }

  _showDialog(Widget widget) {
    if (dialogVisible) {
      log.info('Hide dialog');
      Navigator.of(context).pop();
    }
    dialogVisible = true;
    log.info('Show dialog');
    showDialog(context: context, builder: (context) => widget)
    .then((value) {
      log.info('OnHide dialog');
      dialogVisible = false;
    });
  }

  @override
  void dispose() {
    _session.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Totem FC'),),
      // drawer: MyDrawer(),
      body: Column(children: [
        // Text('Login'),
        Image.asset(
          'images/logo.png',
          fit: BoxFit.contain,
        ),
        Row(children: [
          for (var provider in loginProviders)
            IconButton(
              icon: provider.icon,
              onPressed: () => _session.session.login(provider),
              // onPressed: log.fine(''),
            )
        ])
      ]),
    );
  }
}
