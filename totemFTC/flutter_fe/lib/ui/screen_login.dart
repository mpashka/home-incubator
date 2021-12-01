
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:logging/logging.dart';

class ScreenLogin extends StatelessWidget {
  late final Logger log/* = Logger('ScreenLogin')*/;

  ScreenLogin() {
    log = Logger('ScreenLogin [$hashCode]');
    // log.finer('Create', null, StackTrace.current);
  }

  @override
  Widget build(BuildContext context) {
    log.finer('build()');

    late final SessionBloc sessionBloc;
    return BlocProvider(
      parentWidget: hashCode,
      init: (blocProvider) {
        sessionBloc = blocProvider.addBloc(bloc: SessionBloc());
        log.finer('Init BlocProvider. $sessionBloc');
      },
      child: Scaffold(
        appBar: AppBar(title: const Text('Totem FC'),),
        // drawer: MyDrawer(),
        body: Stack(children: [
          Column(children: [
            // Text('Login'),
            Image.asset(
              'images/logo.png',
              fit: BoxFit.contain,
            ),
            Row(children: [
              for (var provider in loginProviders)
                IconButton(
                  icon: provider.icon,
                  onPressed: () {
                    log.finer('onPressed');

                    sessionBloc.login(provider).then((user) {
                      // onPressed: log.fine(''),
                      if (user != null) {
                        Navigator.pushReplacementNamed(context, '/',);
                      }
                    });
                  }
                )
            ])
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
      ),);
  }
}
