import 'package:flutter/material.dart';
import 'package:flutter_fe/ui/screen_base.dart';

import 'widgets/ui_selector_user.dart';

class ScreenSelectorUser extends StatefulWidget {
  static const routeName = '/master_users';

  @override
  State createState() => ScreenSelectorUserState();
}

class ScreenSelectorUserState extends UiSelectorUserStateBase {

  @override
  Widget build(BuildContext context) {
    return UiScreen(body: Column(
        children: buildContent((user) => Navigator.pushNamed(context, ScreenSelectorUser.routeName, arguments: user))));
  }
}
