import 'package:flutter/material.dart';
import 'package:flutter_fe/ui/screen_base.dart';

import 'screen_master_user.dart';
import 'widgets/ui_selector_user.dart';

class ScreenMasterUserSelect extends StatefulWidget {
  static const routeName = '/master_user_select';

  @override
  State createState() => ScreenMasterUserSelectState();
}

class ScreenMasterUserSelectState extends UiSelectorUserStateBase {

  @override
  Widget build(BuildContext context) {
    return UiScreen(body: Column(
        children: buildContent((user) => Navigator.pushNamed(context, ScreenMasterUser.routeName, arguments: user))));
  }
}
