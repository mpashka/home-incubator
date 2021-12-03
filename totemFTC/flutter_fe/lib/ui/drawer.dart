
import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/crud_user.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/ui/screen_about.dart';
import 'package:flutter_fe/ui/screen_config.dart';
import 'package:flutter_fe/ui/screen_home.dart';
import 'package:flutter_fe/ui/screen_master_trainings.dart';
import 'package:flutter_fe/ui/screen_tickets.dart';
import 'package:flutter_fe/ui/screen_trainings.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:logging/logging.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';

import 'screen_master_user_select.dart';
import 'screen_schedule.dart';

class MyDrawer extends StatelessWidget {
  static final Logger log = Logger('MyDrawer');

  static final Map<CrudEntityUserType, IconData> userTypeIcons = {
    CrudEntityUserType.guest: MdiIcons.abjadArabic,
    CrudEntityUserType.user: MdiIcons.alpha,
    CrudEntityUserType.trainer: MdiIcons.book,
    CrudEntityUserType.admin: MdiIcons.alphaDCircle,
  };

  @override
  Widget build(BuildContext context) {
    Session session = Injector().get<Session>();
    var user = session.user;
    bool trainer = (user.type == CrudEntityUserType.trainer || user.type == CrudEntityUserType.admin)
        && user.trainingTypes != null && user.trainingTypes!.isNotEmpty;
    bool admin = user.type == CrudEntityUserType.admin;
    final theme = Theme.of(context);
    return Drawer(
// Add a ListView to the drawer. This ensures the user can scroll
// through the options in the drawer if there isn't enough vertical
// space to fit everything.
      child: ListView(
// Important: Remove any padding from the ListView.
        padding: EdgeInsets.zero,
        children: <Widget>[
          DrawerHeader(
            decoration: BoxDecoration(color: theme.primaryColor,),
            child: ListTile(
              leading: Icon(userTypeIcons[user.type] ?? MdiIcons.accessPoint),
              title: Text('${user.firstName} ${user.lastName}'),
            ),
          ),
          ListTile(
            title: Text('Главная'),
            onTap: () {
              log.finer('Drawer redirect to root');
              Navigator.pushNamed(context, ScreenHome.routeName,);
              },
          ),
          ListTile(
            title: Text('Абонементы'),
            onTap: () => Navigator.pushNamed(context, ScreenTickets.routeName,),
          ),
          ListTile(
            title: Text('Тренировки'),
            onTap: () => Navigator.pushNamed(context, ScreenTrainings.routeName,),
          ),
/*
          ListTile(
            title: Text('Покупки'),
            onTap: () => Navigator.pushNamed(context, '/purchases',),
          ),
*/
          if (trainer) Divider(),
          if (trainer) ListTile(
            title: Text('Мои тренировки'),
            onTap: () => Navigator.pushNamed(context, ScreenMasterTrainings.routeName,),
          ),
          if (trainer) ListTile(
            title: Text('Мои ученики'),
            onTap: () => Navigator.pushNamed(context, ScreenMasterUserSelect.routeName,),
          ),
          if (trainer) ListTile(
            title: Text('Мое расписание'),
            onTap: () => Navigator.pushNamed(context, ScreenSchedule.routeNameMaster,),
          ),

          Divider(),
          ListTile(
            title: Text('Расписание'),
            onTap: () => Navigator.pushNamed(context, ScreenSchedule.routeName,),
          ),
          ListTile(
            title: Text('О нас'),
            onTap: () => Navigator.pushNamed(context, ScreenAbout.routeName,),
          ),
          ListTile(
            title: Text('Настройки'),
            onTap: () => Navigator.pushNamed(context, ScreenConfig.routeName,),
          ),
        ],
      ),
    );
  }
}
