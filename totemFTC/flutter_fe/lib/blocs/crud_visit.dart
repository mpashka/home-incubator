import 'dart:async';
import 'dart:core';

import 'package:flutter/foundation.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:json_annotation/json_annotation.dart';

import '../misc/utils.dart';
import 'crud_ticket.dart';
import 'crud_training.dart';
import 'crud_user.dart';

part 'crud_visit.g.dart';

// todo in mark*** methods we must take care about returning ticket
class CrudVisitBloc extends BlocBaseList<CrudEntityVisit> {

  CrudVisitBloc({List<CrudEntityVisit> state = const [], required BlocProvider provider, String? name}): super(state: state, provider: provider, name: name);


  Future<void> loadVisits(DateTime from, int rows) async {
    state = (await backend.requestJson('GET', '/api/visit/byUser', params: {'from': dateTimeFormat.format(from), 'rows': rows}) as List)
        .map((item) => CrudEntityVisit.fromJson(item)..user = session.user).toList();
  }

  Future<void> markSchedule(CrudEntityVisit visit, bool mark) async {
    await backend.request('PUT', '/api/visit/markSchedule/$mark', body: visit);
    visit.markSchedule = mark;
    _addAndUpdate(visit, (v) => v.markSchedule = mark);
  }

  Future<void> markSelf(CrudEntityVisit visit, CrudEntityVisitMark mark) async {
    log.finer('Mark self $visit -> $mark');
    await backend.request('PUT', '/api/visit/markSelf/${describeEnum(mark)}', body: visit);
    visit.markSelf = mark;
    _addAndUpdate(visit, (v) => v.markSelf = mark);
  }

  Future<void> markMaster(CrudEntityVisit visit, CrudEntityVisitMark mark) async {
    log.finer('Mark master $visit -> $mark (${visit.user?.firstName})');
    await backend.request('PUT', '/api/visit/markMaster/${describeEnum(mark)}', body: visit);
    visit.markMaster = mark;
    _addAndUpdate(visit, (v) => v.markMaster = mark);
  }

  void _addAndUpdate(CrudEntityVisit visit, void Function(CrudEntityVisit visit) apply) {
    var indexOf = state.indexOf(visit);
    if (indexOf >= 0) {
      log.finest('Visit was found. Updating [$indexOf] -> ${state[indexOf].user?.displayName}');
      apply(state[indexOf]);
    } else {
      log.finest('Visit was not found. Add new ${visit.user?.firstName}');
      state.add(visit);
      state.sort();
    }
    stateIn.add(state);
  }
}

@JsonSerializable(explicitToJson: true)
class CrudEntityTicketType {
  int id;
  List<CrudEntityTrainingType> trainingTypes;
  String name;
  int cost;
  int visits;
  int days;

  CrudEntityTicketType({required this.id, required this.trainingTypes, required this.name, required this.cost,
    required this.visits, required this.days});
  factory CrudEntityTicketType.fromJson(Map<String, dynamic> json) => _$CrudEntityTicketTypeFromJson(json);
  Map<String, dynamic> toJson() => _$CrudEntityTicketTypeToJson(this);
}

@JsonSerializable(explicitToJson: true)
class CrudEntityVisit implements Comparable<CrudEntityVisit> {
  CrudEntityUser? user;
  String? comment;
  bool markSchedule;
  CrudEntityVisitMark markSelf;
  CrudEntityVisitMark markMaster;
  int trainingId;
  /// Training is null if we fetch visits for specific training
  CrudEntityTraining? training;
  CrudEntityTicket? ticket;


  CrudEntityVisit({this.user, this.comment, this.markSchedule = false, this.markSelf = CrudEntityVisitMark.unmark,
    this.markMaster = CrudEntityVisitMark.unmark, required this.trainingId, this.training, this.ticket});

  factory CrudEntityVisit.fromJson(Map<String, dynamic> json) => _$CrudEntityVisitFromJson(json);
  Map<String, dynamic> toJson() => _$CrudEntityVisitToJson(this);

  String get key {
    return 't${trainingId}u${user!.userId}';
  }

  bool isVisible() {
    switch (markMaster) {
      case CrudEntityVisitMark.off: return false;
      case CrudEntityVisitMark.unmark:
        switch (markSelf) {
          case CrudEntityVisitMark.off: return false;
          case CrudEntityVisitMark.unmark: return markSchedule;
          default: return true;
        }
      default: return true;
    }
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is CrudEntityVisit &&
          runtimeType == other.runtimeType &&
          user == other.user &&
          trainingId == other.trainingId;

  @override
  int get hashCode => user.hashCode ^ trainingId.hashCode;

  @override
  int compareTo(CrudEntityVisit other) {
    int result = compareId(0, trainingId, other.trainingId);
    result = compare(result, training, other.training);
    return compare(result, user, other.user);
  }
}

enum CrudEntityVisitMark {
  on, off, unmark
}
