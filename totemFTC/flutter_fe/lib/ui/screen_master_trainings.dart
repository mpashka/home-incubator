import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/ui/screen_base.dart';
import 'package:flutter_fe/ui/widgets/wheel_list_selector.dart';

import 'drawer.dart';
import 'widgets/ui_visit.dart';
import 'widgets/ui_training.dart';

class ScreenMasterTrainings extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
        init: (blocProvider) {
          var now = DateTime.now();
          blocProvider.addBloc(bloc: CrudTrainingBloc()).loadMasterTrainings(now.subtract(const Duration(days: 4)), now.add(const Duration(days: 4)));
        },
        child: UiScreen(
          body: Column(children: [
            WheelListSelector<CrudEntityTraining, CrudTrainingBloc>(
              childBuilder: (context, index, training) => UiTraining(training),
              onSelectedItemChanged: (ctx, i, data) => trainingTypeBloc.onTrainingTypeChange(data),
            )
          ]),
          floatingActionButton: FloatingActionButton(
            onPressed: () => {},
            tooltip: 'Add',
            child: Icon(Icons.add),
          ),
        ));
  }

}
