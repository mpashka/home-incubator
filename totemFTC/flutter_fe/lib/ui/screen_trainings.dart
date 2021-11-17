import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/painting.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/ui/widgets/ui_calendar.dart';
import 'package:flutter_fe/ui/widgets/ui_visits.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:intl/intl.dart';
import 'package:logging/logging.dart';

import 'screen_base.dart';
import 'widgets/ui_divider.dart';

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
    late final CrudVisitBlocFiltered filteredVisitBloc;
    return BlocProvider(
        init: (blocProvider) {
          // _session = Injector().get<Session>();
          final now = DateTime.now();
          final DateTime firstDay = now.subtract(Duration(days: now.weekday - 1 + 7*(weeks-1)));
          final CrudVisitBloc visitBloc = blocProvider.addBloc(bloc: CrudVisitBloc());
          visitBloc.loadVisits(DateTime(firstDay.year, firstDay.month), 10);
          filteredVisitBloc = blocProvider.addBloc(bloc: CrudVisitBlocFiltered(visitBloc, firstDay));
        },
        child: UiScreen(
          body: BlocProvider.streamBuilder<List<CrudEntityVisit>, CrudVisitBloc>(builder: (data) {
            return Column(children: [
              UiCalendar(
                weeks: weeks,
                selectedDates: data.map((v) => v.training!.time).map((t) => DateTime(t.year, t.month, t.day)).toSet(),
                onFilterChange: filteredVisitBloc.onFilterChange,
              ),
              UiDivider('Посещения'),
              UiVisits(),
            ]);}),
          floatingActionButton: FloatingActionButton(
          key: _keyFAB,
            onPressed: () => log.finer('aaa'),
            tooltip: 'Add',
            child: const Icon(Icons.add),
          ), // This trailing comma makes auto-formatting nicer for build methods.
        ));
  }
}
