import 'dart:async';
import 'dart:core';

import 'package:flutter_fe/blocs/crud_api.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:logging/logging.dart';

import 'base.dart';
import 'crud_training.dart';
import 'crud_user.dart';

part 'crud_ticket.g.dart';

class CrudTicket {
  static final Logger log = Logger('CrudTicket');

  final CrudApi _backend;

  final StreamController<List<CrudEntityTicket>> _ticketsController = StreamController<List<CrudEntityTicket>>();
  late final Sink<List<CrudEntityTicket>> _ticketsStateIn;
  late final Stream<List<CrudEntityTicket>> _ticketsState;
  List<CrudEntityTicket> tickets = [];

  CrudTicket(Injector injector):
        _backend = injector.get<CrudApi>()
  {
    _ticketsState = _ticketsController.stream.asBroadcastStream();
    _ticketsStateIn = _ticketsController.sink;

  }

  Future<List<CrudEntityTicket>> loadTickets() async {
    tickets = (await _backend.get('/api/tickets/byUser') as List)
        .map((item) => CrudEntityTicket.fromJson(item)).toList();
    log.fine('Tickets received: $tickets');
    _ticketsStateIn.add(tickets);
    return tickets;
  }
  
  dispose() {
    
  }

  clear() {
    tickets = [];
  }
  
  bloc() {
    return CrudTicketBloc(this);
  }

}

class CrudTicketBloc extends BlocBase {
  final CrudTicket crudTicket;
  // StreamSubscription? _loginStateSubscription;

  CrudTicketBloc(this.crudTicket);

  Stream<List<CrudEntityTicket>> get ticketsState => crudTicket._ticketsState;

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
class CrudEntityTicket {
  int id;
  CrudEntityTicketType ticketType;
  CrudEntityUser user;
  @JsonKey(fromJson: dateTimeFromJson, toJson: dateTimeToJson)
  DateTime start;
  DateTime? end;
  int visited;

  CrudEntityTicket({required this.id, required this.ticketType, required this.user, required this.start, this.end, required this.visited});
  factory CrudEntityTicket.fromJson(Map<String, dynamic> json) => _$CrudEntityTicketFromJson(json);
  Map<String, dynamic> toJson() => _$CrudEntityTicketToJson(this);
}
