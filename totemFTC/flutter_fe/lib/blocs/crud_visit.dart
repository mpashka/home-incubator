import 'dart:async';
import 'dart:core';

import 'package:flutter_fe/blocs/crud_api.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:logging/logging.dart';

import 'base.dart';
import 'crud_ticket.dart';
import 'crud_training.dart';
import 'crud_user.dart';
import '../misc/utils.dart';

part 'crud_visit.g.dart';

class CrudVisit {
  static final Logger log = Logger('CrudTicket');

  final CrudApi _backend;

  final StreamController<List<CrudEntityVisit>> _visitsController = StreamController<List<CrudEntityVisit>>();
  late final Sink<List<CrudEntityVisit>> _visitsStateIn;
  late final Stream<List<CrudEntityVisit>> _visitsState;
  List<CrudEntityVisit> visits = [];

  CrudVisit(Injector injector):
        _backend = injector.get<CrudApi>()
  {
    _visitsState = _visitsController.stream.asBroadcastStream();
    _visitsStateIn = _visitsController.sink;

  }

  Future<List<CrudEntityVisit>> loadVisits(DateTime from, int rows) async {
    visits = (await _backend.requestJson('GET', '/api/visit/byUser', params: {'from': dateTimeFormat.format(from), 'rows': rows}) as List)
        .map((item) => CrudEntityVisit.fromJson(item)).toList();
    _visitsStateIn.add(visits);
    return visits;
  }

  void update() {
    _visitsStateIn.add(visits);
  }
  
  void dispose() {
    
  }

  void clear() {
    visits = [];
  }

  CrudVisitBloc bloc() {
    return CrudVisitBloc(this);
  }

}

class CrudVisitBloc extends BlocBase {
  final CrudVisit crudVisit;
  // StreamSubscription? _loginStateSubscription;

  CrudVisitBloc(this.crudVisit);

  Stream<List<CrudEntityVisit>> get visitsState => crudVisit._visitsState;
  
  Future<void> markSchedule(CrudEntityVisit visit, bool mark) async {
    await crudVisit._backend.request('PUT', '/api/visit/markSchedule/$mark', body: visit);
    visit.markSchedule = mark;
    crudVisit.update();
  }

  Future<void> markSelf(CrudEntityVisit visit, CrudEntityVisitMark mark) async {
    await crudVisit._backend.request('PUT', '/api/visit/markSelf/$mark', body: visit);
    visit.markSelf = mark;
    crudVisit.update();
  }

  @override
  void dispose() {
    // if (_loginStateSubscription != null) {
    //   _loginStateSubscription!.cancel();
    // }
    // _crudTicket._ticketsState.
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
class CrudEntityVisit {
  CrudEntityUser? user;
  String? comment;
  bool markSchedule;
  CrudEntityVisitMark markSelf;
  CrudEntityVisitMark markMaster;
  /// Training is null if we fetch visits for specific training
  CrudEntityTraining? training;
  CrudEntityTicket? ticket;


  CrudEntityVisit({this.user, this.comment, required this.markSchedule, required this.markSelf,
    required this.markMaster, this.training, this.ticket});

  factory CrudEntityVisit.fromJson(Map<String, dynamic> json) => _$CrudEntityVisitFromJson(json);
  Map<String, dynamic> toJson() => _$CrudEntityVisitToJson(this);
}

enum CrudEntityVisitMark {
  on, off, unmark
}
