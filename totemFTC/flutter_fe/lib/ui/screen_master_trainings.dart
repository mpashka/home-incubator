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
import 'widgets/ui_selector_user.dart';
import 'widgets/ui_visit.dart';
import 'widgets/ui_training.dart';

class ScreenMasterTrainings extends StatefulWidget {

  static const routeName = '/master_trainings';

  final CrudEntityTraining? initialTraining;

  ScreenMasterTrainings({this.initialTraining});

  @override
  State createState() => ScreenMasterTrainingsState();
}

class ScreenMasterTrainingsState extends BlocProvider<ScreenMasterTrainings> {
  static final now = DateTime.now();
  static const intervalBefore = Duration(days: 4);
  static const intervalAfter = Duration(days: 4);

  late final TrainingVisitsBloc visitsBloc;
  late final SelectedTrainingBloc selectedTrainingBloc;

  @override
  void initState() {
    super.initState();
    CrudTrainingBloc(provider: this)
        .loadMasterTrainings(now.subtract(intervalBefore), now.add(intervalAfter));
    visitsBloc = TrainingVisitsBloc(provider: this);
    selectedTrainingBloc = SelectedTrainingBloc(provider: this);
  }


  @override
  Widget build(BuildContext context) {
    return UiScreen(body: Column(children: [
          WheelListSelector<CrudEntityTraining, CrudTrainingBloc>(
            childBuilder: (context, index, training) => UiTraining(training),
            transformedChildBuilder: (context, index, item) => item == now
                ? const Text('Сейчас')
                : Text(localDateTimeFormat.format(item)),
            onSelectedItemChanged: (ctx, i, training) {
              visitsBloc.selectTraining(training);
              selectedTrainingBloc.state = training;
            },
            onSelectedTransformedItemChanged: (ctx, i, item) {
              selectedTrainingBloc.state = null;
            },
            selectedItem: widget.initialTraining ?? now,
            dataTransformer: addDates,
          ),
          UiDivider(visitsBloc.selectedTraining == null
              ? 'Выберите тренировку'
              : 'Посещения ${visitsBloc.selectedTraining!.trainingType.trainingName} ${localDateTimeFormat.format(visitsBloc.selectedTraining!.time)}'),
          BlocProvider.streamBuilder<List<CrudEntityVisit>, TrainingVisitsBloc>(builder: (ctx, visits) {
            if (visits.isEmpty) return Text('Никого не было');
            return Expanded(
              child: ListView(children: [
                for (var visit in visits) UiVisit(visit, forTrainer: true,),
              ],),);
          }),
        ]),
        floatingActionButton: BlocProvider.streamBuilder<CrudEntityTraining?, SelectedTrainingBloc>(builder: (ctx, selectedTraining) {
          return FloatingActionButton(
            onPressed: selectedTraining == null ? null : () => addVisit(context, visitsBloc, selectedTraining),
            tooltip: 'Add',
            child: Icon(Icons.add),
          );
        }),
      );
  }

  void addVisit(BuildContext context, TrainingVisitsBloc visitsBloc, CrudEntityTraining training) async {
    CrudEntityUser? user = await UiSelectorUserDialog('Добавить посещение').selectUserDialog(context);
    if (user != null) {
      visitsBloc.markMaster(CrudEntityVisit(user: user,
          markSchedule: false,
          markSelf: CrudEntityVisitMark.unmark,
          markMaster: CrudEntityVisitMark.on,
          trainingId: training.id,
          training: training
      ), CrudEntityVisitMark.on);
    }
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


  TrainingVisitsBloc({required BlocProvider provider, String? name}): super(provider: provider, name: name);

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

class SelectedTrainingBloc extends BlocBaseState<CrudEntityTraining?> {
  SelectedTrainingBloc({required BlocProvider provider, String? name}): super(state: null, provider: provider, name: name);
}
