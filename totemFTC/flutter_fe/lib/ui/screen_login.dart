
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:logging/logging.dart';

class ScreenLogin extends StatelessWidget {
  static final Logger log = Logger('ScreenLogin');

  @override
  Widget build(BuildContext context) {
    late final SessionBloc sessionBloc;
    return BlocProvider(
      init: (blocProvider) {
        sessionBloc = blocProvider.addBloc(bloc: SessionBloc());
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
                  onPressed: () =>
                      sessionBloc.login(provider).then((user) {
                        // onPressed: log.fine(''),
                        if (user != null) {
                          Navigator.pushReplacementNamed(context, '/',);
                        }
                      }),
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
