import 'dart:async';
import 'dart:collection';

import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:intl/intl.dart';
import 'package:logging/logging.dart';

import '../blocs/crud_ticket.dart';
import 'drawer.dart';
import 'screen_base.dart';
import 'widgets/wheel_list_selector.dart';
import 'widgets/ui_visit.dart';
import 'widgets/ui_divider.dart';
import 'widgets/ui_subscription.dart';
import 'widgets/ui_training_selector.dart';

class ScreenHome extends StatelessWidget {
  static final Logger log = Logger('HomeScreen');

  final GlobalKey _keyFAB = GlobalKey();

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
        init: (blocProvider) {
          blocProvider.addBloc(bloc: CrudTicketBloc()).loadTickets();
          blocProvider.addBloc(bloc: CrudVisitBloc()).loadVisits(DateTime.now().subtract(const Duration(days: 14)), 10);
        },
        child: UiScreen(
          body: Column(
            mainAxisAlignment: MainAxisAlignment.start,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: <Widget>[
              BlocProvider.streamBuilder<List<CrudEntityTicket>, CrudTicketBloc>(builder: (data) => Column(
                  children: [
                    if (data.isNotEmpty) const UiDivider('Абонементы')
                    else const UiDivider('Абонементов нет'),
                    for (var ticket in data) UiSubscription(ticket),
                  ])),
              BlocProvider.streamBuilder<List<CrudEntityVisit>, CrudVisitBloc>(builder: (data) {
                List<Widget> prevWidgets = [], nextWidgets = [];
                DateTime now = DateTime.now();
                for (var visit in data) {
                  var training = visit.training!;
                  var widget = UiVisit(visit);
                  // var widget = Text(visit.toJson().toString());
                  (training.time.isBefore(now) ? prevWidgets : nextWidgets).add(widget);
                }
                return Column(children: [
                  if (prevWidgets.isEmpty && nextWidgets.isEmpty) const UiDivider('Тренировок нет'),
                  if (prevWidgets.isNotEmpty) const UiDivider('Тренировки'),
                  for (var widget in prevWidgets) widget,
                  if (nextWidgets.isNotEmpty) const UiDivider('Записи'),
                  for (var widget in nextWidgets) widget,
                ]);
              }),
            ],
          ),
          floatingActionButton: FloatingActionButton(
            key: _keyFAB,
            onPressed: _showAddMenu,
            tooltip: 'Add',
            child: const Icon(Icons.add),
          ), // This trailing comma makes auto-formatting nicer for build methods.
        )
    );
  }

  Future<void> _showAddMenu() async {
    log.fine("Add menu");
    _keyFAB.currentContext!.findRenderObject();
    final RenderBox renderBox = _keyFAB.currentContext!.findRenderObject()! as RenderBox;
    var size = renderBox.size;
    var position = renderBox.localToGlobal(Offset.zero);
    // developer.log("Position: $position, size: $size");

    var result = await showMenu<int>(
      context: _keyFAB.currentContext!,
      position: RelativeRect.fromLTRB(position.dx - size.width, position.dy - size.height, position.dx - size.width, position.dy - size.height),
      // position: RelativeRect.fromLTRB(100, 100, 200, 200),
      items: [
        const PopupMenuItem(value: 1, child: Text("Записаться"),),
        const PopupMenuItem(value: 2, child: Text("Отметиться"),),
        // PopupMenuItem(value: 3, child: Text("Купить еду"),),
      ],
      elevation: 8.0,
    );
    log.finer("Menu item $result selected");
    if (result == 1 || result == 2) {
      _onAddTraining(_keyFAB.currentContext!, result == 2);
    }
  }

  Future<void> _onAddTraining(BuildContext context, bool past) async {
    DateTime now = DateTime.now();
    var result = await UiTrainingSelector('Выберите тренировку').selectTraining(context,
        range: DateTimeRange(
            start: now.subtract(Duration(days: past ? 4 : 0)),
            end: now.add(Duration(days: past ? 4 : 0))));
    log.finer("Dialog result: $result");
    if (result != null) {
      bool _past = result.time.isBefore(now);
      final Session session = Injector().get<Session>();
      CrudVisitBloc visitBloc = BlocProvider.getBloc(context);

      CrudEntityVisit visit = CrudEntityVisit(
          user: session.user,
          training: result,
          trainingId: result.id,
          markSchedule: _past ? false : true,
          markSelf: _past ? CrudEntityVisitMark.on : CrudEntityVisitMark.unmark,
          markMaster: CrudEntityVisitMark.unmark);
      if (_past) {
        visitBloc.markSelf(visit, CrudEntityVisitMark.on);
      } else {
        visitBloc.markSchedule(visit, true);
      }
    }
  }
}
