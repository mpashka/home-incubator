import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_ticket.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/blocs/crud_user.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:flutter_fe/ui/screen_base.dart';
import 'package:flutter_fe/ui/widgets/ui_divider.dart';
import 'package:flutter_fe/ui/widgets/wheel_list_selector.dart';

import 'drawer.dart';
import 'widgets/ui_visit.dart';
import 'widgets/ui_training.dart';

class ScreenMasterTrainings extends StatelessWidget {

  static final DateTime now = DateTime.now();

  @override
  Widget build(BuildContext context) {
    late final TrainingVisitsBloc visitsBloc;
    DateTime? date;
    return BlocProvider(
        init: (blocProvider) {
          var now = DateTime.now();
          blocProvider.addBloc(bloc: CrudTrainingBloc()).loadMasterTrainings(now.subtract(const Duration(days: 4)), now.add(const Duration(days: 4)));
          visitsBloc = blocProvider.addBloc(bloc: TrainingVisitsBloc());
        },
        child: UiScreen((context) => Column(children: [
          WheelListSelector<CrudEntityTraining, CrudTrainingBloc>(
            childBuilder: (context, index, training) => UiTraining(training),
            transformedChildBuilder: (context, index, item) => item == now
                ? const Text('Сейчас')
                : Text(localDateTimeFormat.format(item)),
            onSelectedItemChanged: (ctx, i, training) {
              date = training.time.dayDate();
              visitsBloc.selectTraining(training);
            },
            onSelectedTransformedItemChanged: (ctx, i, item) {
              if (item == now) {
                date = now.dayDate();
              } else if (item is DateTime) {
                date = item;
              }
            },
            selectedItem: now,
            dataTransformer: addDates,
          ),
          UiDivider(visitsBloc.selectedTraining == null
              ? 'Выберите тренировку'
              : 'Посещения ${visitsBloc.selectedTraining!.trainingType.trainingName} ${localDateTimeFormat.format(visitsBloc.selectedTraining!.time)}'),
          BlocProvider.streamBuilder<List<CrudEntityVisit>, TrainingVisitsBloc>(builder: (visits) {
            if (visits.isEmpty) return Text('Никого не было');
            return Expanded(
              child: ListView(children: [
                for (var visit in visits) UiVisit(visit, forTrainer: true,),
              ],),);
          }),
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
    DateTime prevDate = DateTime(0);
    bool nowAdded = false;
    for (var training in trainings) {
      DateTime newDate = training.time.dayDate();
      if (!nowAdded && training.time.isAfter(now)) {
        result.add(now);
        nowAdded = true;
      } else if (newDate.isAfter(prevDate)) {
        result.add(newDate);
      }
      prevDate = newDate;
      result.add(training);
    }
    return result;
  }
}

class TrainingVisitsBloc extends CrudVisitBloc {
  CrudEntityTraining? selectedTraining;

  Future<void> selectTraining(CrudEntityTraining training) async {
    training.visits ??= (await backend.requestJson('GET', '/api/visit/byTraining/${training.id}') as List)
        .map((item) {
      var crudEntityVisit = CrudEntityVisit.fromJson(item);
      crudEntityVisit.training = training;
      return crudEntityVisit;
    }).toList();
    state = training.visits!;
    selectedTraining = training;
  }
}
