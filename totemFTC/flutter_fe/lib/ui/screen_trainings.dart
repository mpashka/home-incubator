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
    final now = DateTime.now();
    final DateTime firstDay = now.subtract(Duration(days: now.weekday - 1 + 7*(weeks-1)));
    late final CrudVisitBloc visitBloc;

    return BlocProvider(
        init: (blocProvider) {
          // _session = Injector().get<Session>();
          visitBloc = blocProvider.addBloc(bloc: CrudVisitBloc());
          visitBloc.loadVisits(DateTime(firstDay.year, firstDay.month), 10);
        },
        child: UiScreen(
          body: BlocProvider.streamBuilder<CrudEntityVisit>(builder: (data) {
            UiVisits uiVisits = UiVisits(visitBloc, firstDay);
            return Column(children: [
              UiCalendar(
                weeks: weeks,
                selectedDates: data.map((v) => v.training!.time).map((t) => DateTime(t.year, t.month, t.day)).toSet(),
                onFilterChange: uiVisits.onFilterChange,
              ),
              uiVisits,
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
