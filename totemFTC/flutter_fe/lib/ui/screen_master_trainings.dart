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

  late final TrainingVisitsBloc visitsBloc;

  @override
  void initState() {
    super.initState();
    var t = CrudTrainingBloc(provider: this, master: true)
      ..loadMasterTrainings(now.subtract(backMaster), now.add(forwardMaster));
    log.finest('Master trainings: ${t.state.length}');
    visitsBloc = TrainingVisitsBloc(provider: this, name: 'CrudVisitBloc')
      ..selectTraining(widget.initialTraining);
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
            visitsBloc.selectTraining(training);
          },
          onSelectedTransformedItemChanged: (ctx, i, item) {
            visitsBloc.selectTraining(null);
          },
          selectedItem: widget.initialTraining ?? now,
          dataTransformer: addDates,
        ),
        Expanded(child: BlocProvider.streamBuilder<List<CrudEntityVisit>, TrainingVisitsBloc>(blocName: 'CrudVisitBloc', builder: (ctx, visits) => Column(children: [
          UiDivider(visitsBloc.selectedTraining == null ? null
              : 'Посещения ${visitsBloc.selectedTraining?.trainingType.trainingName} ${localDateTimeFormat.format(visitsBloc.selectedTraining!.time)}'),
          if (visitsBloc.selectedTraining == null) Text('Выберите тренировку')
          else if (visits.isEmpty) Text('Никого не было')
          else Expanded(child: ListView(children: [
            for (var visit in visits) UiVisit(visit, forTrainer: true,),
          ],),)
        ],),),),
      ],),
      floatingActionButton: BlocProvider.streamBuilder<List<CrudEntityVisit>, TrainingVisitsBloc>(blocName: 'CrudVisitBloc', builder: (ctx, visits) {
        return FloatingActionButton(
          onPressed: visitsBloc.selectedTraining == null ? null : () => addVisit(context, visitsBloc, visitsBloc.selectedTraining!),
          tooltip: 'Add',
          child: Icon(Icons.add),
          backgroundColor: visitsBloc.selectedTraining == null ? Colors.grey : theme.colorScheme.secondary,
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
    bool nowAdded = false;
    void addNow(DateTime date) {
      if (!nowAdded && date.isAfter(now)) {
        result.add(now);
        nowAdded = true;
      }
    }
    DateTime prevDate = DateTime(0);
    for (var training in trainings) {
      DateTime newDate = training.time.dayDate();
      addNow(newDate);
      if (newDate.isAfter(prevDate)) {
        result.add(newDate);
      }
      addNow(training.time);
      prevDate = newDate;
      result.add(training);
    }
    addNow(DateTime(3000));  // No futurama support
    return result;
  }
}

class TrainingVisitsBloc extends CrudVisitBloc {
  CrudEntityTraining? selectedTraining;

  TrainingVisitsBloc({required BlocProvider provider, String? name}): super(provider: provider, name: name);

  Future<void> selectTraining(CrudEntityTraining? training) async {
    if (training == null) {
      state = [];
      selectedTraining = training;
      return;
    }
    training.visits ??= (await backend.requestJson('GET', '/api/visit/byTraining/${training.id}') as List)
        .map((item) => CrudEntityVisit.fromJson(item)..training = training).toList();
    selectedTraining = training;
    state = training.visits!;
  }
}
