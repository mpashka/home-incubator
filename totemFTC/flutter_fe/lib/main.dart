import 'package:flutter/material.dart';
import 'package:flutter_fe/misc/configuration.dart';
import 'package:flutter_fe/ui/screen_config.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:logging/logging.dart';

import 'blocs/crud_training.dart';
import 'blocs/crud_user.dart';
import 'blocs/session.dart';
import 'misc/container.dart';
import 'misc/initializer.dart';
import 'ui/screen_about.dart';
import 'ui/screen_home.dart';
import 'ui/screen_init.dart';
import 'ui/screen_login.dart';
import 'ui/screen_master_trainings.dart';
import 'ui/screen_master_user.dart';
import 'ui/screen_master_user_select.dart';
import 'ui/screen_schedule.dart';
import 'ui/screen_tickets.dart';
import 'ui/screen_trainings.dart';

void main() {
  var injector = Injector();
  ModuleContainer().initialise(injector);
  injector.get<Initializer>().init();
  runApp(MyApp());
}

// todo navigator must be updated. Regular drawer call must replace route. But scenarios must remain history (e.g. user select screen -> user info)
class MyApp extends StatelessWidget {
  static final Logger log = Logger('MyApp');

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    final injector = Injector();
    var initializer = injector.get<Initializer>();
    var session = injector.get<Session>();
    var configuration = injector.get<Configuration>();

    return MaterialApp(
      title: 'Totem FTC App',
      theme: ThemeData(primarySwatch: Colors.blue),
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
      // initialRoute: showInitScreen(initializer),
      onGenerateRoute: (settings) {
        log.info('New route received: $settings');

        if (!initializer.isInitialized) {
          log.finer('Not initialized. Show ScreenInit');
          return MaterialPageRoute(builder: (context) => ScreenInit());
        }

        var routeName = settings.name;
        if (configuration.isMobile && routeName != null && routeName.startsWith(configuration.loginCallbackRoute())) {
          var start = routeName.indexOf('?');
          var loginParams = routeName.substring(start >= 0 ? start : 0).replaceAll('#', '&');
          session.onLoginCallback(loginParams);
          return null;
        }
        session.clearLoginCallback();

        if (!session.isLoggedIn()) {
          log.finer('Not logged in. Show ScreenLogin');
          return MaterialPageRoute(builder: (context) => ScreenLogin());
        } else if (routeName == ScreenLogin.routeName) {
          log.finer('Logged in. Show ScreenHome');
          return MaterialPageRoute(builder: (context) => ScreenHome());
        } else {
          log.finer('Normal screen show');
          switch (routeName) {
            case ScreenHome.routeName: return MaterialPageRoute(builder: (context) => ScreenHome());
            case ScreenLogin.routeName: return MaterialPageRoute(builder: (context) => ScreenLogin());
            case ScreenInit.routeName: return MaterialPageRoute(builder: (context) => ScreenInit());
            case ScreenAbout.routeName: return MaterialPageRoute(builder: (context) => ScreenAbout());
            case ScreenTickets.routeName: return MaterialPageRoute(builder: (context) => ScreenTickets());
            case ScreenTrainings.routeName: return MaterialPageRoute(builder: (context) => ScreenTrainings());
            case ScreenSchedule.routeName: return MaterialPageRoute(builder: (context) => ScreenSchedule());
            case ScreenSchedule.routeNameMaster: return MaterialPageRoute(builder: (context) => ScreenSchedule(forTrainer: true,));
            // case '/purchases': return MaterialPageRoute(builder: (context) => ScreenPurchases());
            case ScreenMasterTrainings.routeName:
              return MaterialPageRoute(builder: (context) => ScreenMasterTrainings(initialTraining: settings.arguments as CrudEntityTraining?));
            case ScreenMasterUserSelect.routeName: return MaterialPageRoute(builder: (context) => ScreenMasterUserSelect());
            case ScreenMasterUser.routeName:
              final user = settings.arguments as CrudEntityUser;
              return MaterialPageRoute(builder: (context) => ScreenMasterUser(user));
            case ScreenConfig.routeName: return MaterialPageRoute(builder: (context) => ScreenConfig());
          }
        }
      },
    );
  }
}

