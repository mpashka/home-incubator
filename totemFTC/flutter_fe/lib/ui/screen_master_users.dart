import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_user.dart';

import 'drawer.dart';
import 'screen_base.dart';
import 'widgets/ui_ticket.dart';
import 'widgets/ui_visit.dart';
import 'widgets/ui_user.dart';

class ScreenMasterUsers extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
      init: (blocProvider) {},
      child: UiScreen(
        body: Column(children: [
          Text('aaa'),
        ]),
        floatingActionButton: FloatingActionButton(
          onPressed: () => {},
          tooltip: 'Add',
          child: Icon(Icons.add),
        ),
      ),);
  }
}

class SelectedUserBloc extends BlocBaseState<CrudEntityUser?> {
  SelectedUserBloc(): super(null);
}
