import 'dart:async';
import 'dart:core';

import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_api.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:logging/logging.dart';
import 'package:flutter_fe/misc/utils.dart';

import 'crud_training.dart';
import 'crud_user.dart';

part 'crud_ticket.g.dart';

class CrudTicketBloc extends BlocBaseList<CrudEntityTicket> {

  SelectedUserBloc? selectedUserBloc;

  CrudTicketBloc({this.selectedUserBloc, List<CrudEntityTicket> state = const [], required BlocProvider provider, String? name}): super(provider: provider, state: state, name: name) {
    selectedUserBloc?.listen((u) => loadUserTickets());
  }

  Future<void> loadTickets() async {
    state = (await backend.requestJson('GET', '/api/tickets/byCurrentUser') as List)
        .map((item) => CrudEntityTicket.fromJson(item)..user = session.user).toList();
  }

  // todo -> loadSelectedTickets
  Future<void> loadUserTickets() async {
    var user = selectedUserBloc?.state ?? session.user;

    if (user.tickets == null) {
      state = [];
      user.tickets = (await backend.requestJson(
          'GET', '/api/tickets/byUser/${user.userId}') as List)
          .map((item) => CrudEntityTicket.fromJson(item)..user = user)
          .toList();
    }
    state = user.tickets!;
  }

  Future<CrudEntityTicket> createTicket(CrudEntityTicket ticket) async {
    var ticketId = int.parse((await backend.request('POST', '/api/ticket', body: ticket)).body);
    ticket.id = ticketId;
    return ticket;
  }

  CrudEntityTicket updateTicketLocally(CrudEntityTicket ticket) {
    var indexOf = state.indexOf(ticket);
    if (indexOf >= 0) {
      log.finest('Ticket was found. Updating ticket[$indexOf] -> ${ticket.displayName}');
      ticket.visits = state[indexOf].visits;
      state[indexOf] = ticket;
      stateIn.add(state);
    } else {
      log.warning('Ticket was not found. Probably internal error. ${ticket.displayName}');
    }
    return ticket;
  }
}

class SelectedTicketBloc extends BlocBaseState<CrudEntityTicket?> {
  SelectedTicketBloc({required BlocProvider provider, String? name}): super(state: null, provider: provider, name: name);
}

class SelectedTicketTypeBloc extends BlocBaseState<CrudEntityTicketType?> {
  SelectedTicketTypeBloc({required BlocProvider provider, String? name}): super(state: null, provider: provider, name: name);
}

class CrudTicketTypeFilteredBloc extends BlocBaseList<CrudEntityTicketType> {
  TicketTypeFilter? ticketTypeFilter;
  CrudTicketTypeFilteredBloc({this.ticketTypeFilter, List<CrudEntityTicketType> state = const [], required BlocProvider provider, String? name}): super(provider: provider, state: state, name: name);

  Future<void> loadTicketTypes({TrainingFilter? trainingFilter}) async {
    state = await cache('ticketTypes', () async =>
        (await backend.requestJson('GET', '/api/ticketTypes/list',) as List)
            .map((item) => CrudEntityTicketType.fromJson(item)).toList());
  }
}

typedef TicketTypeFilter = bool Function(CrudEntityTicketType ticketType);

@JsonSerializable(explicitToJson: true)
class CrudEntityTicketType {
  int id;
  List<CrudEntityTrainingType>? trainingTypes;
  String name;
  int cost;
  int visits;
  int days;

  CrudEntityTicketType({required this.id, this.trainingTypes, required this.name, required this.cost,
    required this.visits, required this.days});
  factory CrudEntityTicketType.fromJson(Map<String, dynamic> json) => _$CrudEntityTicketTypeFromJson(json);
  Map<String, dynamic> toJson() => _$CrudEntityTicketTypeToJson(this);
}

@JsonSerializable(explicitToJson: true)
class CrudEntityTicket {
  int id;
  CrudEntityTicketType ticketType;
  CrudEntityUser? user;
  @JsonKey(fromJson: dateTimeFromJson, toJson: dateTimeToJson)
  DateTime buy;
  @JsonKey(fromJson: dateFromJson_, toJson: dateToJson_)
  DateTime? start;
  @JsonKey(fromJson: dateFromJson_, toJson: dateToJson_)
  DateTime? end;
  int visited;

  // added
  @JsonKey(ignore: true)
  List<CrudEntityVisit>? visits;

  CrudEntityTicket({required this.id, required this.ticketType, required this.user, required this.buy, this.start, this.end, required this.visited});
  factory CrudEntityTicket.fromJson(Map<String, dynamic> json) => _$CrudEntityTicketFromJson(json);
  Map<String, dynamic> toJson() => _$CrudEntityTicketToJson(this);

  String get displayName {
    return '${ticketType.name} / ${localDateFormat.format(buy)}';
  }

  @override
  bool operator ==(Object other) => identical(this, other) || other is CrudEntityTicket && runtimeType == other.runtimeType && id == other.id;

  @override
  int get hashCode => id.hashCode;
}
