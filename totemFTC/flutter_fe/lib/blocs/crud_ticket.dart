import 'dart:async';
import 'dart:core';

import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_api.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:logging/logging.dart';

import 'crud_training.dart';
import 'crud_user.dart';

part 'crud_ticket.g.dart';

class CrudTicketBloc extends BlocBaseList<CrudEntityTicket> {

  Future<void> loadTickets() async {
    state = (await backend.requestJson('GET', '/api/tickets/byUser') as List)
        .map((item) => CrudEntityTicket.fromJson(item)).toList();
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
  CrudEntityUser? user;
  @JsonKey(fromJson: dateFromJson, toJson: dateToJson)
  DateTime buy;
  @JsonKey(fromJson: dateTimeFromJson_, toJson: dateTimeToJson_)
  DateTime? start;
  @JsonKey(fromJson: dateTimeFromJson_, toJson: dateTimeToJson_)
  DateTime? end;
  int visited;
  // added
  List<CrudEntityVisit>? visits;

  CrudEntityTicket({required this.id, required this.ticketType, required this.user, required this.buy, this.start, this.end, required this.visited});
  factory CrudEntityTicket.fromJson(Map<String, dynamic> json) => _$CrudEntityTicketFromJson(json);
  Map<String, dynamic> toJson() => _$CrudEntityTicketToJson(this);
}
