import 'dart:async';

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
    selectedTrainingBloc = SelectedTrainingBloc(provider: this);
    visitsBloc = TrainingVisitsBloc(selectedTrainingBloc, provider: this);
  }


  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return UiScreen(
      body: Column(children: [
        WheelListSelector<CrudEntityTraining, CrudTrainingBloc>(
          // childBuilder: (context, index, training) => UiTraining(training),
          childBuilder: (context, index, training) => Center(child: Text('${training.trainingType.trainingName} ${timeFormat.format(training.time)}'),),
          transformedChildBuilder: (context, index, item) => Center(child: Container(
            child: Text(item == now ? 'Сейчас': localDateFormat.format(item)),
            padding: const EdgeInsets.only(left: 8, right: 8,),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.all(Radius.circular(20.0)),
              color: theme.colorScheme.background.withAlpha(80),
            ),),
          ),
          onSelectedItemChanged: (ctx, i, training) {
            selectedTrainingBloc.state = training;
          },
          onSelectedTransformedItemChanged: (ctx, i, item) {
            selectedTrainingBloc.state = null;
          },
          selectedItem: widget.initialTraining ?? now,
          dataTransformer: addDates,
        ),
        Expanded(child: BlocProvider.streamBuilder<CrudEntityTraining?, SelectedTrainingBloc>(builder: (ctx, training) => Column(children: [
          UiDivider(training == null
            ? null
            : 'Посещения ${training.trainingType.trainingName} ${localDateTimeFormat.format(training.time)}'),
          Expanded(child: BlocProvider.streamBuilder<List<CrudEntityVisit>, TrainingVisitsBloc>(builder: (ctx, visits) {
            if (training == null) {
              return Text('Выберите тренировку');
            } else if (visits.isEmpty) {
              return Text('Никого не было');
            } else {
              return ListView(children: [
                for (var visit in visits) UiVisit(visit, forTrainer: true,),
              ],);
            }
          }),),
        ],),),),
      ],),
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
      if (newDate.isAfter(prevDate)) {
        result.add(newDate);
      }
      if (!nowAdded && training.time.isAfter(now)) {
        result.add(now);
        nowAdded = true;
      }
      prevDate = newDate;
      result.add(training);
    }
    return result;
  }
}

class SelectedTrainingBloc extends BlocBaseState<CrudEntityTraining?> {
  SelectedTrainingBloc({required BlocProvider provider, String? name}): super(state: null, provider: provider, name: name);
}

class TrainingVisitsBloc extends CrudVisitBloc {
  late final StreamSubscription<CrudEntityTraining?> selectedTrainingSubscription;

  TrainingVisitsBloc(SelectedTrainingBloc selectedTrainingBloc, {required BlocProvider provider, String? name}): super(provider: provider, name: name) {
    selectedTrainingSubscription = selectedTrainingBloc.stateOut.listen((selectedTraining) => onSelectTraining);
  }

  @override
  void dispose() {
    selectedTrainingSubscription.cancel();
    super.dispose();
  }

  Future<void> onSelectTraining(CrudEntityTraining? training) async {
    if (training == null) {
      state = [];
      return;
    }
    training.visits ??= (await backend.requestJson('GET', '/api/visit/byTraining/${training.id}') as List)
        .map((item) {
      var crudEntityVisit = CrudEntityVisit.fromJson(item);
      crudEntityVisit.training = training;
      return crudEntityVisit;
    }).toList();
    state = training.visits!;
  }
}
