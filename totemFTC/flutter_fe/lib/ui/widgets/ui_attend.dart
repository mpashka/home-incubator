import 'dart:developer' as developer;

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:intl/intl.dart';
import 'package:logging/logging.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';

import '../../blocs/crud_visit.dart';
import '../../blocs/crud_training.dart';

@immutable
class UiAttend extends StatefulWidget {
  final CrudEntityVisit _visit;
  const UiAttend(this._visit, {Key? key}) : super(key: key);

  @override
  State createState() => UiAttendState();
}

class UiAttendState extends State<UiAttend> {
  static final Logger log = Logger('UiAttendState');

  static final format = DateFormat('yyyy-MM-dd kk:mm');

  late final CrudEntityVisit _visit;
  late final CrudVisitBloc _visitBloc;

  UiAttendState() {
    _visit = widget._visit;
    final injector = Injector();
    _visitBloc = injector.get<CrudVisit>().bloc();
  }



  @override
  Widget build(BuildContext context) {
    final CrudEntityTraining training = _visit.training!;
    bool past = training.time.isBefore(DateTime.now());
    IconData visitIcon = _visit.markSchedule ? MdiIcons.clockOutline : MdiIcons.checkboxBlankCircleOutline;
    if (past) {
      if (_visit.markMaster == CrudEntityVisitMark.on) {
        // icon = _visit.markSelf ? MdiIcons.checkCircle : MdiIcons.checkCircleOutline;
        visitIcon = _visit.markSelf == CrudEntityVisitMark.on ? MdiIcons
            .checkboxMultipleMarkedCircle : MdiIcons.checkCircle;
      } else if (_visit.markMaster == CrudEntityVisitMark.off) {
        visitIcon = _visit.markSelf == CrudEntityVisitMark.off ? MdiIcons
            .closeCircleMultipleOutline : MdiIcons.closeCircle;
      } else if (_visit.markSelf == CrudEntityVisitMark.on) {
        // icon = MdiIcons.bookmarkCheckOutline;
        visitIcon = _visit.markSchedule ? MdiIcons.clockCheckOutline : MdiIcons.checkCircleOutline;
      } else if (_visit.markSelf == CrudEntityVisitMark.off) {
        // icon = MdiIcons.bookmarkCheckOutline;
        visitIcon = _visit.markSchedule ? MdiIcons.clockRemoveOutline : MdiIcons.closeCircleOutline;
      }
    }

    var listTile = ListTile(
        leading: Row(
            // mainAxisAlignment: MainAxisAlignment.start,
            // crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              // Training - specific icon
              const Icon(Icons.agriculture_rounded),
              Icon(visitIcon),
            ]
        ),
        title: Text('${training.trainingType.trainingName} ${format.format(training.time)}'),
        trailing: const Icon(Icons.more_vert),
      );

    return GestureDetector(
      child: listTile,
      onTapDown: (TapDownDetails details) {
        _showPopupMenu(context, details.globalPosition, past);
      },
    );
  }

  void _showPopupMenu(BuildContext context, Offset offset, bool past) async {
    log.finer("Menu position $offset");

    double left = offset.dx;
    double top = offset.dy;
    var result = await showMenu<int>(
      context: context,
      position: RelativeRect.fromLTRB(left, top, left, top),
      items: [
        if (past && _visit.markSelf != CrudEntityVisitMark.on) const PopupMenuItem(
          value: 1,
          child: Text("Был"),
        ),
        if (past && _visit.markSelf != CrudEntityVisitMark.off) const PopupMenuItem(
          value: 2,
          child: Text("Не был"),
        ),
        if (!past && !_visit.markSchedule) const PopupMenuItem(
          value: 3,
          child: Text("Приду"),
        ),
        if (!past && _visit.markSchedule) const PopupMenuItem(
          value: 4,
          child: Text("Не приду"),
        ),
/*
        if (!past) const PopupMenuItem(
          value: 5,
          child: Text("Перенести"),
        ),
*/
      ],
      elevation: 8.0,
    );
    log.finer("Menu item $result selected");

    switch (result) {
      case 1: _visitBloc.markSelf(_visit, CrudEntityVisitMark.on); break;
      case 2: _visitBloc.markSelf(_visit, CrudEntityVisitMark.off); break;
      case 3: _visitBloc.markSchedule(_visit, true); break;
      case 4: _visitBloc.markSchedule(_visit, false); break;
    }
  }
}

