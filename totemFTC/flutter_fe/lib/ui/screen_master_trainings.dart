
import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/blocs/crud_user.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/misc/utils.dart';

import 'screen_base.dart';
import 'widgets/ui_divider.dart';
import 'widgets/ui_selector_user.dart';
import 'widgets/ui_visit.dart';
import 'widgets/ui_wheel_list_selector.dart';

class ScreenMasterTrainings extends StatefulWidget {

  static const routeName = '/master_trainings';

  final CrudEntityTraining? initialTraining;

  ScreenMasterTrainings({this.initialTraining});

  @override
  State createState() => ScreenMasterTrainingsState();
}

class ScreenMasterTrainingsState extends BlocProvider<ScreenMasterTrainings> {
  static const backlogDays = 14;
  static final now = DateTime.now();
  late final DateTime start = now.subtract(const Duration(days: backlogDays));

  late final SelectedTrainingBloc selectedTrainingBloc;
  late final CrudVisitBloc visitsBloc;

  @override
  void initState() {
    super.initState();
    var t = CrudTrainingBloc(provider: this, master: true)
      ..loadMasterTrainings(now.subtract(backMaster), now.add(forwardMaster));
    log.finest('Master trainings: ${t.state.length}');
    selectedTrainingBloc = SelectedTrainingBloc(state: widget.initialTraining, provider: this);
    visitsBloc = CrudVisitBloc(start: start, selectedTrainingBloc: selectedTrainingBloc, provider: this)
      ..loadUserVisits();
    combine2<CrudEntityTraining?, List<CrudEntityVisit>>('AllTrainingsBloc', selectedTrainingBloc, visitsBloc);
  }


  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return UiScreen(
      body: Column(children: [
        // todo merge with ui_selector_training
        UiWheelListSelector<CrudEntityTraining, CrudTrainingBloc>(
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
          onSelectedItemChanged: (ctx, i, training) => selectedTrainingBloc.state = training,
          onSelectedTransformedItemChanged: (ctx, i, item) => selectedTrainingBloc.state = null,
          selectedItem: widget.initialTraining ?? now,
          dataTransformer: (data) => addDates(data, now),
        ),
        Expanded(child: BlocProvider.streamBuilder<Combined2<CrudEntityTraining?, List<CrudEntityVisit>>, BlocBaseState<Combined2<CrudEntityTraining?, List<CrudEntityVisit>>>>(blocName: 'AllTrainingsBloc', builder: (ctx, combined) {
          CrudEntityTraining? selectedTraining = combined.state1;
          List<CrudEntityVisit> visits = combined.state2;
          return Column(children: [
            UiDivider(selectedTraining == null ? null
                : 'Посещения ${selectedTraining.trainingType.trainingName} ${localDateTimeFormat.format(selectedTraining.time)}'),
            if (selectedTraining == null) Text('Выберите тренировку')
            else if (visits.isEmpty) Text('Никого не было')
            else Expanded(child: ListView(children: [
                for (var visit in visits) UiVisit(visit, forTrainer: true,),
              ],),)
          ],);
        }),),
      ],),
      floatingActionButton: BlocProvider.streamBuilder<CrudEntityTraining?, SelectedTrainingBloc>(builder: (ctx, selectedTraining) {
        final enabled = selectedTraining != null && selectedTraining.time.isBefore(now);
        return FloatingActionButton(
          onPressed: enabled ? () => addVisit(context, selectedTraining!) : null,
          tooltip: 'Add',
          child: Icon(Icons.add),
          backgroundColor: enabled ? theme.colorScheme.secondary : Colors.grey,
        );
      }),
    );
  }

  void addVisit(BuildContext context, CrudEntityTraining training) async {
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
}
