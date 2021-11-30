import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/painting.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:flutter_fe/ui/widgets/ui_calendar.dart';
import 'package:flutter_fe/ui/widgets/ui_visits.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:intl/intl.dart';
import 'package:logging/logging.dart';

import 'screen_base.dart';
import 'widgets/ui_divider.dart';
import 'widgets/ui_selector_training.dart';

class ScreenTrainings extends StatefulWidget {
  @override
  State createState() => ScreenTrainingsState();
}

class ScreenTrainingsState extends State<ScreenTrainings> {
  static final Logger log = Logger('ScreenTrainings');
  static const int weeks = 6;

  final GlobalKey _keyFAB = GlobalKey();


  @override
  Widget build(BuildContext context) {
    late final CrudVisitBloc visitBloc;
    late final CrudVisitBlocFiltered filteredVisitBloc;
    CrudEntityTrainingType? selectedTrainingType;
    return BlocProvider(
        init: (blocProvider) {
          // _session = Injector().get<Session>();
          final now = DateTime.now();
          final DateTime firstDay = now.subtract(Duration(days: now.weekday - 1 + 7*(weeks-1)));
          visitBloc = blocProvider.addBloc(bloc: CrudVisitBloc());
          visitBloc.loadVisits(firstDay.monthDate(), 10);
          filteredVisitBloc = blocProvider.addBloc(bloc: CrudVisitBlocFiltered(visitBloc, firstDay));
        },
        child: UiScreen(body: BlocProvider.streamBuilder<List<CrudEntityVisit>, CrudVisitBloc>(
          builder: (ctx, data) => Column(children: [
            UiCalendar(
              weeks: weeks,
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
        ));
  }

  Future<void> _onAddTraining(CrudVisitBloc visitBloc, CrudVisitBlocFiltered filteredVisitBloc, CrudEntityTrainingType? selectedTrainingType) async {
    var _session = Injector().get<Session>();
    List<CrudEntityTrainingType>? types = selectedTrainingType != null ? [selectedTrainingType] : null;
    var result = await UiSelectorTraining('Выберите тренировку').selectTraining(context, dateFilter: filteredVisitBloc.filter, types: types);
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
