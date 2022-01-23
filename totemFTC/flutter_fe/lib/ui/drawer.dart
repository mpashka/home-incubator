
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

import 'screen_master_finance.dart';
import 'screen_master_user_select.dart';
import 'screen_schedule.dart';

class MyDrawer extends StatelessWidget {
  static final Logger log = Logger('MyDrawer');

  static final Map<CrudEntityUserType, IconData> userTypeIcons = {
    CrudEntityUserType.user: MdiIcons.account,
    CrudEntityUserType.trainer: MdiIcons.accountTie,
    CrudEntityUserType.admin: MdiIcons.accountHardHat,
  };

  @override
  Widget build(BuildContext context) {
    Session session = Injector().get<Session>();
    var user = session.user;
    bool isTrainer = (user.types.contains(CrudEntityUserType.trainer))
        && user.trainingTypes != null && user.trainingTypes!.isNotEmpty;
    bool isUser = user.types.contains(CrudEntityUserType.user);
    bool isAdmin = user.types.contains(CrudEntityUserType.admin);
    final theme = Theme.of(context);

    for (var type in user.types) {
      var userTypeIcon = userTypeIcons[type];
      log.fine("Type: $type, icon: $userTypeIcon");
    }

    return Drawer(
      child: ListView(
        padding: EdgeInsets.zero,
        children: <Widget>[
          DrawerHeader(
            decoration: BoxDecoration(color: theme.primaryColor,),
            child: ListTile(
              leading: Row(mainAxisSize: MainAxisSize.min,
                children: [
                  if (user.types.isEmpty) Icon(MdiIcons.accountQuestion),
                  for (var type in user.types) Icon(userTypeIcons[type]),
                ],),
              title: Text('${user.firstName} ${user.lastName}'),
            ),
          ),

/*
          if (isUser) ListTile(
            title: Text('Главная'),
            onTap: () {
              log.finer('Drawer redirect to root');
              Navigator.pushNamed(context, ScreenHome.routeName,);
            },
          ),
*/
          if (isUser) ListTile(
            title: Text('Абонементы'),
            onTap: () => Navigator.pushNamed(context, ScreenTickets.routeName,),
          ),
          if (isUser) ListTile(
            title: Text('Тренировки'),
            onTap: () => Navigator.pushNamed(context, ScreenTrainings.routeName,),
          ),
/*
          ListTile(
            title: Text('Покупки'),
            onTap: () => Navigator.pushNamed(context, '/purchases',),
          ),
*/
          if (isUser && isTrainer) Divider(),
          if (isTrainer) ListTile(
            title: Text('Мои тренировки'),
            onTap: () => Navigator.pushNamed(context, ScreenMasterTrainings.routeName,),
          ),
          if (isTrainer) ListTile(
            title: Text('Мои ученики'),
            onTap: () => Navigator.pushNamed(context, ScreenMasterUserSelect.routeName,),
          ),
          if (isTrainer) ListTile(
            title: Text('Мое расписание'),
            onTap: () => Navigator.pushNamed(context, ScreenSchedule.routeNameMaster,),
          ),

          if (isTrainer || isAdmin) Divider(),
          if (isTrainer || isAdmin) ListTile(
            title: Text('Финансы'),
            onTap: () => Navigator.pushNamed(context, ScreenMasterFinance.routeName,),
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
