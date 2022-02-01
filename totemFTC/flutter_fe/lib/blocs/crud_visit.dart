import 'dart:async';
import 'dart:core';

import 'package:flutter/foundation.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/ui/screen_master_trainings.dart';
import 'package:flutter_fe/ui/screen_master_user.dart';
import 'package:json_annotation/json_annotation.dart';

import '../misc/utils.dart';
import 'crud_ticket.dart';
import 'crud_training.dart';
import 'crud_user.dart';

part 'crud_visit.g.dart';

// todo in mark*** methods we must take care about returning ticket
class CrudVisitBloc extends BlocBaseList<CrudEntityVisit> {
  static const alwaysReload = true;

  final DateTime? start;
  final SelectedUserBloc? selectedUserBloc;
  final SelectedTicketBloc? selectedTicketBloc;
  final SelectedTrainingBloc? selectedTrainingBloc;
  final CrudTicketBloc? ticketBloc;
  late final Map<String, dynamic> params;

  CrudVisitBloc({this.start, this.selectedUserBloc, this.selectedTicketBloc, this.selectedTrainingBloc, this.ticketBloc,
      List<CrudEntityVisit> state = const [], required BlocProvider provider, String? name}): super(state: state, provider: provider, name: name)
  {
    selectedUserBloc?.listen((u) => loadUserVisits());
    selectedTicketBloc?.listen((t) => loadUserVisits());
    selectedTrainingBloc?.listen((t) => loadUserVisits());
    params = {'from': dateTimeFormat.format(start ?? DateTime(0))};
  }

  // todo rename loadSelectedVisits
  void loadUserVisits() async {
    log.finest('loadUserVisits()...');
    var user = selectedUserBloc?.state ?? session.user;
    var ticket = selectedTicketBloc?.state;
    var training = selectedTrainingBloc?.state;

    if (ticket != null) {
      log.finest('loadUserVisits(). Loading ticket visits');
      if (alwaysReload || ticket.visits == null) {
        state = [];
        ticket.visits = (await backend.requestJson('GET', '/api/visit/byCurrentUser/byTicket/${ticket.id}', params: params) as List)
            .map((v) => CrudEntityVisit.fromJson(v)
              ..user = user
              ..ticket = ticket)
            .toList();
      }
      state = ticket.visits!;
    } else if (training != null) {
      log.finest('loadUserVisits(). Loading training visits');
      if (alwaysReload || training.visits == null) {
        state = [];
        training.visits = (await backend.requestJson('GET', '/api/visit/byTraining/${training.id}') as List)
            .map((item) => CrudEntityVisit.fromJson(item)..training = training)
            .toList();
      }
      state = training.visits!;
    } else {
      log.finest('loadUserVisits(). Loading user visits');
      if (alwaysReload || user.visits == null) {
        state = [];
        user.visits = (await backend.requestJson('GET', '/api/visit/byCurrentUser', params: params) as List)
            .map((v) => CrudEntityVisit.fromJson(v)..user = user)
            .toList();
      }
      state = user.visits!;
    }
    log.fine('Visits: ${state.length}');
  }

  Future<void> loadCurrentUserVisits() async {
    var user = session.user;
    if (user.visits == null) {
      state = [];
      user.visits = (await backend.requestJson('GET', '/api/visit/byCurrentUser', params: params) as List)
          .map((item) => CrudEntityVisit.fromJson(item)..user = session.user)
          .toList();
    }
    state = user.visits!;
  }

  Future<CrudEntityTicket?> markSchedule(CrudEntityVisit visit, bool mark) async {
    var ticketJson = (await backend.requestJson('PUT', '/api/visit/markSchedule/$mark', body: visit));
    visit.markSchedule = mark;
    return _addAndUpdate(visit, ticketJson, (v) => v.markSchedule = mark);
  }

  Future<CrudEntityTicket?> markSelf(CrudEntityVisit visit, CrudEntityVisitMark mark) async {
    log.finer('Mark self $visit -> $mark');
    var ticketJson = (await backend.requestJson('PUT', '/api/visit/markSelf/${describeEnum(mark)}', body: visit));
    visit.markSelf = mark;
    return _addAndUpdate(visit, ticketJson, (v) => v.markSelf = mark);
  }

  Future<CrudEntityTicket?> markMaster(CrudEntityVisit visit, CrudEntityVisitMark mark) async {
    log.finer('Mark master $visit -> $mark (${visit.user?.firstName})');
    var ticketJson = (await backend.requestJson('PUT', '/api/visit/markMaster/${describeEnum(mark)}', body: visit));
    visit.markMaster = mark;
    return _addAndUpdate(visit, ticketJson, (v) => v.markMaster = mark);
  }

  CrudEntityTicket? _addAndUpdate(CrudEntityVisit visit, ticketJson, void Function(CrudEntityVisit visit) apply) {
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
    if (ticketJson != null && ticketBloc != null) {
      var ticket = CrudEntityTicket.fromJson(ticketJson)
        // todo [!] this is not correct. User here is buyer, not visitor.
        ..user = visit.user;
      ticket = ticketBloc!.updateTicketLocally(ticket);
      if (selectedTicketBloc != null && selectedTicketBloc!.state != null && selectedTicketBloc!.state!.id == ticket.id) {
        selectedTicketBloc!.state = ticket;
      }
      return ticket;
    }
  }
}

class SelectedVisitBloc extends BlocBaseState<CrudEntityVisit?> {
  SelectedVisitBloc({CrudEntityVisit? state, required BlocProvider provider, String? name}) : super(state: state, provider: provider, name: name);
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
    var now = DateTime.now();
    if (training!.time.isAfter(now)) {
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
    } else {
      return markSchedule;
    }
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) || other is CrudEntityVisit && runtimeType == other.runtimeType && user == other.user && trainingId == other.trainingId;

  @override
  int get hashCode => user.hashCode ^ trainingId.hashCode;

  @override
  int compareTo(CrudEntityVisit other) {
    int result = compareId(0, trainingId, other.trainingId);
    result = compare(result, training, other.training);
    return compare(result, user, other.user);
  }
}

enum CrudEntityVisitMark { on, off, unmark }
