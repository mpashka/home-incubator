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
  final Session _session = Injector().get<Session>();

  UiVisit(this._visit, {this.forTrainer = false});

  @override
  Widget build(BuildContext context) {
    CrudVisitBloc visitBloc = BlocProvider.getProviderBloc<CrudVisitBloc>(context);

    final CrudEntityTraining training = _visit.training!;
    bool past = training.time.isBefore(DateTime.now());
    var icons = <Widget>[
      if (_visit.markSchedule) Icon(MdiIcons.clockOutline),
      if (_visit.markSelf == CrudEntityVisitMark.on) Icon(MdiIcons.accountCheckOutline),
      if (_visit.markSelf == CrudEntityVisitMark.off) Icon(MdiIcons.accountCancelOutline),
      if (_visit.markMaster == CrudEntityVisitMark.on) Icon(Icons.gpp_good),
      if (_visit.markMaster == CrudEntityVisitMark.off) Icon(Icons.gpp_bad),
    ];

    var listTile = ListTile(
        leading: Row(
            mainAxisSize: MainAxisSize.min,
            children: icons,
        ),
        title: Text(forTrainer
            ? _visit.user!.displayName
            : '${training.trainingType.trainingName} (${training.trainer?.nickName})'),
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
      onTapDown: (tapDownDetails) => _showPopupMenu(context, visitBloc, tapDownDetails.globalPosition, past),
    );
  }

  void _showPopupMenu(BuildContext context, CrudVisitBloc visitBloc, Offset offset, bool past) async {
    log.finer("Menu position $offset");

    final List<PopupMenuItem<int>> items = [];
    if (!forTrainer) {
      if (past) {
        if (_visit.markSelf != CrudEntityVisitMark.on) items.add(const PopupMenuItem(value: 1, child: Text("Был"),));
        if (_visit.markSelf != CrudEntityVisitMark.off) items.add(const PopupMenuItem(value: 2, child: Text("Не был"),));
      } else if (!past) {
        if (!_visit.markSchedule) items.add(const PopupMenuItem(value: 3, child: Text("Приду"),),);
        if (_visit.markSchedule) items.add(const PopupMenuItem(value: 4, child: Text("Не приду"),),);
      }
    } else if (forTrainer && past) {
        if (_visit.markMaster != CrudEntityVisitMark.on) items.add(const PopupMenuItem(value: 5, child: Text("Был"),));
        if (_visit.markMaster != CrudEntityVisitMark.off) items.add(const PopupMenuItem(value: 6, child: Text("Не был"),),);
    }
    // if (!past) const PopupMenuItem(value: 7, child: Text("Перенести"),),
    if (items.isEmpty) return;

    double left = offset.dx;
    double top = offset.dy;
    var result = await showMenu<int>(
      context: context,
      position: RelativeRect.fromLTRB(left, top, left, top),
      items: items,
      elevation: 8.0,
    );
    log.finer("Menu item $result selected");

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
