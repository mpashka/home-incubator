import 'package:flutter/material.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:logging/logging.dart';
import 'package:flutter_web_plugins/flutter_web_plugins.dart';

import 'blocs/session.dart';
import 'ui/screen_home.dart';
import 'ui/screen_trainings.dart';
import 'ui/screen_init.dart';
import 'ui/screen_master_users.dart';
import 'ui/screen_master_trainings.dart';
import 'ui/screen_purchases.dart';
import 'ui/screen_tickets.dart';
import 'ui/screen_login.dart';
import 'misc/container.dart';
import 'misc/initializer.dart';
import 'ui/screen_about.dart';
import 'ui/widgets/ui_selector_user.dart';

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
          return MaterialPageRoute(builder: (context) => ScreenInit());
        }

        if (!session.isLoggedIn()) {
          return MaterialPageRoute(builder: (context) => ScreenLogin());
        } else if (settings.name == '/login') {
          return MaterialPageRoute(builder: (context) => ScreenHome());
        } else {
          switch (settings.name) {
            case '/': return MaterialPageRoute(builder: (context) => ScreenHome());
            case '/login': return MaterialPageRoute(builder: (context) => ScreenLogin());
            case '/init': return MaterialPageRoute(builder: (context) => ScreenInit());
            case '/about': return MaterialPageRoute(builder: (context) => ScreenAbout());
            case '/tickets': return MaterialPageRoute(builder: (context) => ScreenTickets());
            case '/trainings': return MaterialPageRoute(builder: (context) => ScreenTrainings());
            case '/purchases': return MaterialPageRoute(builder: (context) => ScreenPurchases());
            case '/master_trainings': return MaterialPageRoute(builder: (context) => ScreenMasterTrainings());
            // case '/master_users': return MaterialPageRoute(builder: (context) => ScreenMasterUsers());
            case '/master_users': return MaterialPageRoute(builder: (context) => UiSelectorUser().buildPage(context));
          }
        }
      },
    );
  }

  uiCreateTheme() {
    return ThemeData(
      primarySwatch: Colors.blue,
    );
  }

  showInitScreen(Initializer initializer) {
    return initializer.isInitialized() ? '/login' : '/init';
  }
}

