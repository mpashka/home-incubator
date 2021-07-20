import 'package:flutter/material.dart';
import 'package:view_flt1/ui/drawer.dart';
import 'package:view_flt1/ui/home.dart';
import 'package:view_flt1/ui/my_theme.dart';

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
        home: Scaffold(
            appBar: AppBar(
              title: Text('Totem FC'),
            ),
            body: Home(),
            drawer: uiCreateDrawer()
        ),
      // routes: ,
    );
  }
}
