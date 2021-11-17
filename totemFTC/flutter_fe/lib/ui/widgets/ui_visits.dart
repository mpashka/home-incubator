import 'dart:collection';

import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/ui/widgets/ui_calendar.dart';

import 'ui_visit.dart';

class UiVisits extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    return BlocProvider.streamBuilder<FilteredVisits, CrudVisitBlocFiltered>(builder: (d) {
      if (d.isEmpty()) {
        return Text('Посещений не было');
      }

      return DefaultTabController(
          length: d.typesCount(),
          child: Column(children: [
            Container(decoration: BoxDecoration(color: Theme.of(context).primaryColor),
            child: TabBar(tabs: [
              if (!d.isSingle()) Tab(child: Text('Все / ${d.allVisits.length}'),),
              for (var trainingType in d.trainingTypes) Tab(
                  child: Text('${trainingType.trainingName} / ${d.visitsByType[trainingType]!.length}')),
            ],),),
            Container(height: 200,
            child: TabBarView(children: [

              if (!d.isSingle()) ListView(children: [
                for (var visit in d.allVisits) UiVisit(visit),
              ]),
              for (var trainingType in d.trainingTypes) ListView(children: [
                for (var visit in d.visitsByType[trainingType]!) UiVisit(visit),
              ]),

              // if (!d.isSingle()) Text('all'),
              // for (var trainingType in d.trainingTypes) Text('Tr ${trainingType.trainingName}'),
            ]))
          ]));

      // return Text('Tabs...');
    });
  }
}

class FilteredVisits {
  List<CrudEntityVisit> allVisits;
  List<CrudEntityTrainingType> trainingTypes;
  Map<CrudEntityTrainingType, List<CrudEntityVisit>> visitsByType;

  FilteredVisits(this.allVisits, this.trainingTypes, this.visitsByType);

  bool isEmpty() {
    return allVisits.isEmpty;
  }

  bool isSingle() {
    return trainingTypes.length == 1;
  }

  int typesCount() {
    if (allVisits.isEmpty) {
      return 0;
    } else if (trainingTypes.length == 1) {
      return 1;
    } else {
      return trainingTypes.length + 1;
    }
  }
}

class CrudVisitBlocFiltered extends BlocBaseState<FilteredVisits> {

  final CrudVisitBloc _visitBloc;
  final DateTime _firstDay;
  DateSelectionType selectionType = DateSelectionType.none;
  DateTime date = DateTime.now();


  CrudVisitBlocFiltered(this._visitBloc, this._firstDay) : super(FilteredVisits([], [], {})) {
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
    List<CrudEntityVisit> allVisits;
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
    Set<CrudEntityTrainingType> trainingTypesSet = HashSet();
    allVisits.forEach((v) => trainingTypesSet.add(v.training!.trainingType));
    List<CrudEntityTrainingType> trainingTypes = trainingTypesSet.toList();
    trainingTypes.sort();
    Map<CrudEntityTrainingType, List<CrudEntityVisit>> visitsByType = HashMap();
    trainingTypesSet.forEach((tt) => visitsByType[tt] = allVisits.where((v) => v.training!.trainingType == tt).toList());
    stateIn.add(FilteredVisits(allVisits, trainingTypes, visitsByType));
  }
}
