import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_user.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:intl/intl.dart';
import 'package:logging/logging.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';

import '../../blocs/crud_visit.dart';
import '../../blocs/crud_training.dart';

@immutable
class UiVisit extends StatelessWidget {

  static final Logger log = Logger('UiVisit');

  final CrudEntityVisit _visit;
  /// todo this is adhoc workaround. Probably later enum will be used
  final bool forTrainer;
  late final Session _session;

  UiVisit(this._visit, {this.forTrainer = false}) {
    _session = Injector().get<Session>();
  }

  @override
  Widget build(BuildContext context) {
    CrudVisitBloc visitBloc = BlocProvider.getBloc<CrudVisitBloc>(context);

    final CrudEntityTraining training = _visit.training!;
    final CrudEntityUser user = _visit.user!;
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
        title: Text(forTrainer
            ? '${user.firstName} ${user.lastName} (${user.nickName})'
            : '${training.trainingType.trainingName} (${training.trainer.nickName})'),
        trailing: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(localDateTimeFormat.format(training.time)),
            const Icon(Icons.more_vert),
          ],
        )
    );

    return GestureDetector(
      child: listTile,
      onTapDown: (TapDownDetails details) {
        _showPopupMenu(context, visitBloc, details.globalPosition, past);
      },
    );
  }

  void _showPopupMenu(BuildContext context, CrudVisitBloc visitBloc, Offset offset, bool past) async {
    log.finer("Menu position $offset");

    double left = offset.dx;
    double top = offset.dy;
    var result = await showMenu<int>(
      context: context,
      position: RelativeRect.fromLTRB(left, top, left, top),
      items: [
        if (!forTrainer && past && _visit.markSelf != CrudEntityVisitMark.on) const PopupMenuItem(
          value: 1,
          child: Text("Был"),
        ),
        if (!forTrainer && past && _visit.markSelf != CrudEntityVisitMark.off) const PopupMenuItem(
          value: 2,
          child: Text("Не был"),
        ),
        if (!forTrainer && !past && !_visit.markSchedule) const PopupMenuItem(
          value: 3,
          child: Text("Приду"),
        ),
        if (!forTrainer && !past && _visit.markSchedule) const PopupMenuItem(
          value: 4,
          child: Text("Не приду"),
        ),

        if (forTrainer && past && _visit.markMaster != CrudEntityVisitMark.on) const PopupMenuItem(
          value: 5,
          child: Text("Был"),
        ),
        if (forTrainer && past && _visit.markMaster != CrudEntityVisitMark.off) const PopupMenuItem(
          value: 6,
          child: Text("Не был"),
        ),

/*
        if (!past) const PopupMenuItem(
          value: 7,
          child: Text("Перенести"),
        ),
*/
      ],
      elevation: 8.0,
    );
    log.finer("Menu item $result selected");
    _visit.user = _session.user;

    switch (result) {
      case 1: visitBloc.markSelf(_visit, CrudEntityVisitMark.on); break;
      case 2: visitBloc.markSelf(_visit, CrudEntityVisitMark.off); break;
      case 3: visitBloc.markSchedule(_visit, true); break;
      case 4: visitBloc.markSchedule(_visit, false); break;
      case 5: visitBloc.markMaster(_visit, CrudEntityVisitMark.on); break;
      case 6: visitBloc.markMaster(_visit, CrudEntityVisitMark.off); break;
    }
  }
}
