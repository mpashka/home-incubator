
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/main.dart';
import 'package:flutter_fe/misc/configuration.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';

import 'widgets/ui_login_warning.dart';


class ScreenLogin extends StatefulWidget {
  static const routeName = '/login';

  @override
  State createState() => ScreenLoginState();
}

class ScreenLoginState extends BlocProvider<ScreenLogin> {
  late final SessionBloc sessionBloc;

  @override
  void initState() {
    super.initState();
    sessionBloc = SessionBloc(provider: this);
    log.finer('Init BlocProvider. $sessionBloc');
  }

  @override
  Widget build(BuildContext context) {
    ThemeData theme = Theme.of(context);
    double iconSize = theme.iconTheme.size ?? 24;
    double warningIconSize = iconSize / 2;
    double screenWidth = MediaQuery.of(context).size.width;
    double padding = 8;

    final configuration = Injector().get<Configuration>();
    final loginButtonColumns = <Widget>[];
    List<Widget> loginButtons = [];

    // log.finer('build login screen(). Width $screenWidth, icon size: $iconSize');
    for (var provider in loginProviders) {
      final loginProviderConfig = configuration.loginProviderConfig(provider);

      loginButtons.add(IconButton(
        padding: EdgeInsets.all(padding),
        icon: Stack(children: [
          provider.icon,
          if (loginProviderConfig.error != null) Positioned(right: 0, top: 0,
            child: Icon(Icons.error, size: warningIconSize, color: Colors.red),),
          if (loginProviderConfig.warning != null) Positioned(right: 0, top: 0,
            child: Icon(Icons.warning, size: warningIconSize, color: Colors.yellow),),
        ],),
        onPressed: () async {
          log.finer('onPressed');
          if (await UiLoginWarning(provider, loginProviderConfig).checkLogin(context)) {
            sessionBloc.login(provider).then((user) {
              // onPressed: log.fine(''),
              if (user != null) {
                Navigator.pushReplacementNamed(context, MyApp.homeRouteName,);
              }
            });
          }
        },
      ));
      if (loginButtons.length >= (screenWidth / (iconSize + 3*padding) - 1).ceil() || provider == loginProviders.last) {
        loginButtonColumns.add(Row(children: loginButtons,));
        loginButtons = [];
      }
    }

    return Scaffold(
      appBar: AppBar(title: const Text('Totem FC'),),
      body: Stack(children: [
        Column(children: [
          Image.asset(
            'assets/images/logo.png',
            fit: BoxFit.contain,
          ),
          SizedBox(height: 16,),
          Column(children: loginButtonColumns),
        ],),
        BlocProvider.streamBuilder<LoginStateInfo, SessionBloc>(
          builder: (ctx, loginState) =>
              Column(children: [
                if (loginState.state == LoginState.inProgress) AlertDialog(
                  title: Text('Loading login data'),
                  content: Center(
                      heightFactor: 1, child: CircularProgressIndicator()),
                ),
                if (loginState.state == LoginState.error) AlertDialog(
                  title: const Text('Login error'),
                  content: SingleChildScrollView(
                      child: Text(loginState.description!)),
                  actions: [
                    TextButton(
                      child: Text('Ok'),
                      onPressed: () => sessionBloc.reset(),
                    )
                  ],
                ),
              ]),),
      ]),
    );
  }
}
