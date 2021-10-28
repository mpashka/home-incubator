import 'package:flutter/material.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:logging/logging.dart';
import 'package:flutter_web_plugins/flutter_web_plugins.dart';

import 'blocs/session.dart';
import 'ui/screen_home.dart';
import 'ui/my_theme.dart';
import 'ui/screen_attends.dart';
import 'ui/screen_init.dart';
import 'ui/screen_master_people.dart';
import 'ui/screen_master_trains.dart';
import 'ui/screen_purchases.dart';
import 'ui/screen_subscriptions.dart';
import 'ui/screen_login.dart';
import 'misc/container.dart';
import 'misc/initializer.dart';
import 'ui/screen_about.dart';

void main() {
  var injector = Injector();
  ModuleContainer().initialise(injector);
  injector.get<Initializer>().init();
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  static final Logger log = Logger('MyApp');

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    final injector = Injector();
    var initializer = injector.get<Initializer>();
    var session = injector.get<Session>();

    return MaterialApp(
      title: 'Totem FC App',
      theme: uiCreateTheme(),
/*
      routes: {
        '/': (context) => HomeScreen(),
        '/login': (context) => LoginScreen(injector),
        '/init': (context) => InitScreen(initializer),
        '/about': (context) => AboutScreen(),
        '/subscriptions': (context) => SubscriptionsScreen(),
        '/trains': (context) => AttendsScreen(),
        '/purchases': (context) => PurchasesScreen(),
        '/master_trains': (context) => MasterTrainsScreen(),
        '/master_people': (context) => MasterPeopleScreen(),
      },
*/
      initialRoute: showInitScreen(initializer),
      onGenerateRoute: (settings) {
        log.info('New route received: ${settings.name}, ${settings.arguments} / $settings');

        if (!initializer.isInitialized()) {
          return MaterialPageRoute(builder: (context) => InitScreen(initializer));
        }

        if (!session.isLoggedIn()) {
          return MaterialPageRoute(builder: (context) => LoginScreen(injector));
        } else if (settings.name == '/login') {
          return MaterialPageRoute(builder: (context) => HomeScreen(injector));
        } else {
          switch (settings.name) {
            case '/': return MaterialPageRoute(builder: (context) => HomeScreen(injector));
            case '/login': return MaterialPageRoute(builder: (context) => LoginScreen(injector));
            case '/init': return MaterialPageRoute(builder: (context) => InitScreen(initializer));
            case '/about': return MaterialPageRoute(builder: (context) => const AboutScreen());
            case '/subscriptions': return MaterialPageRoute(builder: (context) => SubscriptionsScreen());
            case '/trains': return MaterialPageRoute(builder: (context) => AttendsScreen());
            case '/purchases': return MaterialPageRoute(builder: (context) => PurchasesScreen());
            case '/master_trains': return MaterialPageRoute(builder: (context) => MasterTrainsScreen());
            case '/master_people': return MaterialPageRoute(builder: (context) => MasterPeopleScreen());
          }
        }
      },
    );
  }

  showInitScreen(Initializer initializer) {
    return initializer.isInitialized() ? '/login' : '/init';
  }
}

