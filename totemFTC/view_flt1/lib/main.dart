import 'package:flutter/material.dart';

import 'ui/drawer.dart';
import 'ui/screen_home.dart';
import 'ui/my_theme.dart';
import 'ui/screen_attends.dart';
import 'ui/screen_master_people.dart';
import 'ui/screen_master_trains.dart';
import 'ui/screen_purchases.dart';
import 'ui/screen_subscriptions.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {

    return MaterialApp(
      title: 'Totem FC App',
      theme: uiCreateTheme(),
      routes: {
        '/': (context) => HomeScreen(),
        '/subscriptions': (context) => SubscriptionsScreen(),
        '/trains': (context) => AttendsScreen(),
        '/purchases': (context) => PurchasesScreen(),
        '/master_trains': (context) => MasterTrainsScreen(),
        '/master_people': (context) => MasterPeopleScreen(),
      },
    );
  }
}
