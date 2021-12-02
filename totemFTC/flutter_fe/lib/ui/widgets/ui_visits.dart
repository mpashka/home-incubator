import 'dart:collection';

import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:flutter_fe/ui/widgets/ui_calendar.dart';
import 'package:logging/logging.dart';

import 'ui_visit.dart';

class UiVisits extends StatelessWidget {
  static final Logger log = Logger('UiVisits');

  final void Function(CrudEntityTrainingType? trainingType) onTrainingTypeChange;

  UiVisits(this.onTrainingTypeChange);

  @override
  Widget build(BuildContext context) {
    return BlocProvider.streamBuilder<FilteredVisits, CrudVisitBlocFiltered>(builder: (ctx, visits) {
      if (visits.isEmpty()) {
        return Text('Посещений не было');
      }

      var theme = Theme.of(context);
      return DefaultTabController(length: visits.trainingTypes.length + 1, child: Builder(builder: (context) {
        var tabController = DefaultTabController.of(context)!;
        tabController.addListener(() => onTrainingTypeChange(tabController.index > 0 ? visits.trainingTypes[tabController.index - 1] : null));
        return Column(children: [
          Container(decoration: BoxDecoration(color: theme.primaryColor),
            child: TabBar(indicatorColor: theme.colorScheme.onPrimary,
              tabs: [
                // if (!d.isSingle())
                Tab(child: Text('Все / ${visits.allVisits.length}'),),
                for (var trainingType in visits.trainingTypes) Tab(
                    child: Text('${trainingType.trainingName} / ${visits.visitsByType[trainingType]!.length}')),
              ],),),
          Expanded(child: TabBarView(children: [
            // if (!d.isSingle())
            ListView(children: [
              for (var visit in visits.allVisits) UiVisit(visit),
            ]),
            for (var trainingType in visits.trainingTypes) ListView(children: [
              for (var visit in visits.visitsByType[trainingType]!) UiVisit(visit),
            ]),
          ]))
        ]);},),);
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

  /// Deprecated
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
  DateFilterInfo filter;
  Set<CrudEntityTrainingType> trainingTypesSet = {};

  CrudVisitBlocFiltered(this._visitBloc, DateTime firstDay, {required BlocProvider provider, String? name}) :
        filter = DateFilterInfo((d) => true, DateTimeRange(start: DateTime.now().subtract(Duration(days: 7 * 6)), end: DateTime.now()), DateSelectionType.none),
        super(state: FilteredVisits([], [], {}), provider: provider, name: name) {
    _visitBloc.stateOut.listen((e) => _updateVisits());
  }

  void onFilterChange(DateFilterInfo filter) {
    this.filter = filter;
    _updateVisits();
  }

  void _updateVisits() {
    List<CrudEntityVisit> allVisits = _visitBloc.state.where((v) => filter.filter(v.training!.time)).toList();
    Set<CrudEntityTrainingType> trainingTypesSet = HashSet();
    allVisits.forEach((v) => trainingTypesSet.add(v.training!.trainingType));
    List<CrudEntityTrainingType> trainingTypes = trainingTypesSet.toList();
    trainingTypes.sort();
    Map<CrudEntityTrainingType, List<CrudEntityVisit>> visitsByType = HashMap();
    trainingTypesSet.forEach((tt) => visitsByType[tt] = allVisits.where((v) => v.training!.trainingType == tt).toList());
    stateIn.add(FilteredVisits(allVisits, trainingTypes, visitsByType));
  }
}
