import 'dart:async';
import 'dart:core';

import 'package:flutter/foundation.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_api.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:logging/logging.dart';

import 'crud_ticket.dart';
import 'crud_training.dart';
import 'crud_user.dart';
import '../misc/utils.dart';
import 'session.dart';

part 'crud_visit.g.dart';

class CrudVisitBloc extends BlocBaseList<CrudEntityVisit> {

  Future<void> loadVisits(DateTime from, int rows) async {
    state = (await backend.requestJson('GET', '/api/visit/byUser', params: {'from': dateTimeFormat.format(from), 'rows': rows}) as List)
        .map((item) {
      var crudEntityVisit = CrudEntityVisit.fromJson(item);
      crudEntityVisit.user = session.user;
      return crudEntityVisit;
    }).toList();
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

  void _addAndUpdate(CrudEntityVisit visit, void Function(CrudEntityVisit visit) apply) {
    List<CrudEntityVisit> state = this.state;
    var indexOf = state.indexOf(visit);
    if (indexOf >= 0) {
      apply(state[indexOf]);
    } else {
      state.add(visit);
      state.sort();
    }
    this.state = state;
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


  CrudEntityVisit({this.user, this.comment, required this.markSchedule, required this.markSelf,
    required this.markMaster, required this.trainingId, this.training, this.ticket});

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
