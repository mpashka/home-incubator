import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:flutter_fe/ui/widgets/ui_calendar.dart';
import 'package:flutter_fe/ui/widgets/ui_visits.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';

import 'screen_base.dart';
import 'widgets/ui_divider.dart';
import 'widgets/ui_selector_training.dart';

// todo Probably add icons into this screen
class ScreenTrainings extends StatefulWidget {
  static const routeName = '/trainings';

  @override
  State createState() => ScreenTrainingsState();
}

class ScreenTrainingsState extends BlocProvider<ScreenTrainings> {
  static const int weeksBeforeNow = 5;
  static const int weeksAfterNow = 1;

  final GlobalKey _keyFAB = GlobalKey();
  late final CrudVisitBloc visitBloc;
  late final CrudVisitBlocFiltered filteredVisitBloc;
  CrudEntityTrainingType? selectedTrainingType;

  late final DateTime firstDay;
  late final DateTime lastDay;

  @override
  void initState() {
    super.initState();
    // _session = Injector().get<Session>();
    final now = DateTime.now().dayDate();
    firstDay = now.subtract(Duration(days: now.weekday - 1 + 7*weeksBeforeNow));
    lastDay = now.add(Duration(days: 7 - now.weekday + 7*weeksAfterNow));
    visitBloc = CrudVisitBloc(start: firstDay.monthDate(), provider: this)
      ..loadCurrentUserVisits();
    filteredVisitBloc = CrudVisitBlocFiltered(visitBloc, firstDay, provider: this);
  }


  @override
  Widget build(BuildContext context) {
    final now = DateTime.now().dayDate();
    return UiScreen(body: BlocProvider.streamBuilder<List<CrudEntityVisit>, CrudVisitBloc>(
      builder: (ctx, data) => Column(children: [
        UiCalendar(
          // firstDay: firstDay,
          // lastDay: lastDay,
          firstDay: now.subtract(Duration(days: 7*weeksBeforeNow)),
          lastDay: now.add(Duration(days: 7*weeksAfterNow)),
          selectedDates: data.where((v) => v.isVisible()).map((v) => v.training!.time).map((t) => DateTime(t.year, t.month, t.day)).toSet(),
          onFilterChange: filteredVisitBloc.onFilterChange,
        ),
        UiDivider('Посещения'),
        Expanded(child: UiVisits((tt) => selectedTrainingType = tt)),
      ]),),
      floatingActionButton: FloatingActionButton(
        key: _keyFAB,
        onPressed: () => _onAddTraining(visitBloc, filteredVisitBloc, selectedTrainingType),
        tooltip: 'Add',
        child: const Icon(Icons.add),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }

  Future<void> _onAddTraining(CrudVisitBloc visitBloc, CrudVisitBlocFiltered filteredVisitBloc, CrudEntityTrainingType? selectedTrainingType) async {
    var _session = Injector().get<Session>();
    List<CrudEntityTrainingType>? types = selectedTrainingType != null ? [selectedTrainingType] : null;
    var result = await UiSelectorTrainingDialog(title: 'Выберите тренировку',
        dateFilter: filteredVisitBloc.filter, types: types
    ).selectTraining(context);
    log.finer("Dialog result: $result");
    if (result != null) {
      CrudEntityVisit visit = CrudEntityVisit(
          user: _session.user,
          training: result,
          trainingId: result.id,
          markSchedule: false,
          markSelf: CrudEntityVisitMark.on,
          markMaster: CrudEntityVisitMark.unmark);
      visitBloc.markSelf(visit, CrudEntityVisitMark.on);
    }
  }
}