
import 'package:flutter/material.dart';

class MyDrawer extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    return Drawer(
// Add a ListView to the drawer. This ensures the user can scroll
// through the options in the drawer if there isn't enough vertical
// space to fit everything.
      child: ListView(
// Important: Remove any padding from the ListView.
        padding: EdgeInsets.zero,
        children: <Widget>[
          DrawerHeader(
            decoration: BoxDecoration(
              color: Colors.blue,
            ),
            child: Text('Drawer Header'),
          ),
          ListTile(
            title: Text('Главная'),
            onTap: () => Navigator.pushNamed(context, '/',),
          ),
          ListTile(
            title: Text('Абонементы'),
            onTap: () => Navigator.pushNamed(context, '/subscriptions',),
          ),
          ListTile(
            title: Text('Тренировки'),
            onTap: () => Navigator.pushNamed(context, '/trains',),
          ),
          ListTile(
            title: Text('Покупки'),
            onTap: () => Navigator.pushNamed(context, '/purchases',),
          ),
          ListTile(
            title: Text('Расписание'),
            onTap: () {
// Update the state of the app.
// ...
            },
          ),
          ListTile(
            title: Text('Тренер/Тренировки'),
            onTap: () => Navigator.pushNamed(context, '/master_trains',),
          ),
          ListTile(
            title: Text('Тренер/Люди'),
            onTap: () => Navigator.pushNamed(context, '/master_people',),
          ),
          ListTile(
            title: Text('Связаться с нами'),
            onTap: () {
// Update the state of the app.
// ...
            },
          ),
        ],
      ),
    );
  }
}
