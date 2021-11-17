import 'dart:collection';

import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/ui/widgets/ui_calendar.dart';

class UiVisits extends StatelessWidget {
  final CrudVisitBloc _visitBloc;
  final DateTime _firstDay;

  UiVisits(this._visitBloc, this._firstDay);

  @override
  Widget build(BuildContext context) {
    CrudVisitBlocFiltered filteredVisitBloc = BlocProvider.of(context).addBloc(bloc: CrudVisitBlocFiltered(_visitBloc, _firstDay));

    return BlocProvider.streamBuilder(bloc: filteredVisitBloc, builder: (d) => DefaultTabController(
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
        ));
  }

}

class CrudVisitBlocFiltered extends BlocBaseList<CrudEntityVisit> {

  final CrudVisitBloc _visitBloc;
  final DateTime _firstDay;
  DateSelectionType selectionType = DateSelectionType.none;
  DateTime date = DateTime.now();

  List<CrudEntityVisit> allVisits = [];
  List<CrudEntityTrainingType> trainingTypes = [];
  Map<CrudEntityTrainingType, List<CrudEntityVisit>> visitsByType = {};

  CrudVisitBlocFiltered(this._visitBloc, this._firstDay) {
    _visitBloc.stateOut.listen((e) => _updateVisits());
  }

  void onFilterChange(DateSelectionType selectionType, {DateTime? date}) {
    this.selectionType = selectionType;
    if (date != null) {
      this.date = date;
    }
    _updateVisits();
  }

  void _updateVisits() {
    List<CrudEntityVisit> visits = selectionType == DateSelectionType.month ? _visitBloc.state : _visitBloc.state.where((v) => !v.training!.time.isBefore(_firstDay)).toList();
    switch (selectionType) {
      case DateSelectionType.none:
        allVisits = visits;
        break;
      case DateSelectionType.month:
        var month = date.month;
        allVisits = visits.where((v) => v.training!.time.month == month).toList();
        break;
      case DateSelectionType.week:
        DateTime lastDay = _firstDay.add(Duration(days: 7));
        allVisits = visits.where((v) => v.training!.time.isAfter(_firstDay) && v.training!.time.isBefore(lastDay)).toList();
        break;
      case DateSelectionType.day:
        var month = date.month;
        var day = date.day;
        allVisits = visits.where((v) => v.training!.time.month == month && v.training!.time.day == day).toList();
        break;
      case DateSelectionType.weekDay:
        var weekday = date.weekday;
        allVisits = visits.where((v) => v.training!.time.weekday == weekday).toList();
        break;
      default: throw Exception('Internal error. Unknown selection type $selectionType');
    }
    Set<CrudEntityTrainingType> trainingTypes = HashSet();
    allVisits.forEach((v) => trainingTypes.add(v.training!.trainingType));
    this.trainingTypes = trainingTypes.toList();
    this.trainingTypes.sort();
    Map<CrudEntityTrainingType, List<CrudEntityVisit>> visitsByType = HashMap();
    trainingTypes.forEach((tt) => visitsByType[tt] = allVisits.where((v) => v.training!.trainingType == tt).toList());
    this.visitsByType = visitsByType;
    stateIn.add(allVisits);
  }
}
