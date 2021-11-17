import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/painting.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/ui/widgets/ui_calendar.dart';
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

  final GlobalKey _keyFAB = GlobalKey();

  late final CrudVisitBloc _visitBloc;

  @override
  Widget build(BuildContext context) {


    return BlocProvider(
        init: (blocProvider) {
          // _session = Injector().get<Session>();
          _visitBloc = blocProvider.blocListCreate<CrudEntityVisit, CrudVisitBloc>();
          var now = DateTime.now();
          _visitBloc.loadVisits(DateTime(now.year, now.month-1), 10);
        },
        child: UiScreen(
          body: BlocProvider.streamBuilderList<CrudEntityVisit>((data) => Column(children: [
            UiCalendar(
              weeks: 6,
              selectedDates: data.map((v) => v.training!.time).map((t) => DateTime(t.year, t.month, t.day)).toSet(),
              onDateChange: (t, {dateTime}) => log.fine('Date selected'),
            ),
              DefaultTabController(
                  length: 3,
                child: Column(children: [
                  TabBar(tabs: [
                      Tab(icon: Icon(Icons.directions_car)),
                      Tab(icon: Icon(Icons.directions_transit)),
                      Tab(icon: Icon(Icons.directions_bike)),
                    ],),
                  TabBarView(children: [

                  ],)
          ],
              ))
          ])),
          floatingActionButton: FloatingActionButton(
            key: _keyFAB,
            onPressed: () => log.finer('aaa'),
            tooltip: 'Add',
            child: const Icon(Icons.add),
          ), // This trailing comma makes auto-formatting nicer for build methods.
        ));
  }


}
