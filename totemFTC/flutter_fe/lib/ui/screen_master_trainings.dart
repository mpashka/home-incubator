import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_ticket.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/blocs/crud_user.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:flutter_fe/ui/screen_base.dart';
import 'package:flutter_fe/ui/widgets/wheel_list_selector.dart';

import 'drawer.dart';
import 'widgets/ui_visit.dart';
import 'widgets/ui_training.dart';

class ScreenMasterTrainings extends StatelessWidget {

  static final Object nowItem = Object();

  @override
  Widget build(BuildContext context) {
    late final CrudVisitBloc visitBloc;
    return BlocProvider(
        init: (blocProvider) {
          var now = DateTime.now();
          blocProvider.addBloc(bloc: CrudTrainingBloc()).loadMasterTrainings(now.subtract(const Duration(days: 4)), now.add(const Duration(days: 4)));
          visitBloc = blocProvider.addBloc(bloc: CrudVisitBloc());
        },
        child: UiScreen(
          body: Column(children: [
            WheelListSelector<CrudEntityTraining, CrudTrainingBloc>(
              childBuilder: (context, index, training) => UiTraining(training),
              transformedChildBuilder: (context, index, item) => item == nowItem ? const Text('Сейчас') :
                  Text(localDateTimeFormat.format(item)),
              onSelectedItemChanged: (ctx, i, training) => visitBloc.state = training.visits ?? [],
              dataTransformer: addDates,
            ),

          ]),
          floatingActionButton: FloatingActionButton(
            onPressed: () => {},
            tooltip: 'Add',
            child: Icon(Icons.add),
          ),
        ));
  }

  List addDates(List<CrudEntityTraining> trainings) {
    List result = [];
    DateTime? date;
    DateTime? now = DateTime.now();
    for (var training in trainings) {
      DateTime newDate = DateTime(training.time.year, training.time.month, training.time.day);
      if (now.isAfter(training.time)) {
        result.add(nowItem);
      } else if (date == null || newDate.isAfter(date)) {
        result.add(date);
      }
      date = newDate;
      result.add(training);
    }
    return result;
  }
}

