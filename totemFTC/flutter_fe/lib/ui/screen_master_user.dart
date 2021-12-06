import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_ticket.dart';
import 'package:flutter_fe/blocs/crud_user.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:flutter_fe/ui/widgets/ui_divider.dart';
import 'package:flutter_fe/ui/widgets/ui_selector_user.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';

import 'screen_base.dart';
import 'widgets/ui_selector_training.dart';
import 'widgets/ui_ticket.dart';
import 'widgets/ui_user.dart';
import 'widgets/ui_visit.dart';

class ScreenMasterUser extends StatefulWidget {

  static const routeName = '/master_user';
  final CrudEntityUser _user;

  const ScreenMasterUser(this._user);

  @override
  State createState() => ScreenMasterUserState();
}

class ScreenMasterUserState extends BlocProvider<ScreenMasterUser> {

  static const backlogDays = 14;
  final DateTime now = DateTime.now();
  late final DateTime start = now.subtract(const Duration(days: backlogDays));

  late final SelectedUserBloc selectedUserBloc;
  late final SelectedTicketBloc selectedTicketBloc;
  late final UserVisitsBloc visitsBloc;
  @override
  void initState() {
    super.initState();
    selectedUserBloc = SelectedUserBloc(user: widget._user, provider: this);
    var ticketsBloc = UserTicketsBloc(selectedUserBloc, provider: this)..loadUserTickets();
    selectedTicketBloc = SelectedTicketBloc(provider: this);
    combine3<CrudEntityUser, CrudEntityTicket?, List<CrudEntityTicket>>('AllTicketsBloc', selectedUserBloc, selectedTicketBloc, ticketsBloc, listen2: true, listen3: true);

    visitsBloc = UserVisitsBloc(start, selectedUserBloc, selectedTicketBloc, provider: this, name: 'CrudVisitBloc')
      ..loadUserVisits();
  }

  @override
  Widget build(BuildContext context) {
    return UiScreen(body: BlocProvider.streamBuilder<CrudEntityUser, SelectedUserBloc>(builder: (ctx, user) => Column(children: [
      Row(children: [
        Expanded(child: UiUser(user)),
        GestureDetector(
          child: Icon(Icons.more_vert),
          onTap: () async {
            CrudEntityUser? user = await UiSelectorUserDialog('Пользователи').selectUserDialog(context);
            if (user != null) selectedUserBloc.state = user;
          },
        )
      ],),
      UiDivider('Абонементы'),
      BlocProvider.streamBuilder<Combined3<CrudEntityUser, CrudEntityTicket?, List<CrudEntityTicket>>, BlocBaseState<Combined3<CrudEntityUser, CrudEntityTicket?, List<CrudEntityTicket>>>>(blocName: 'AllTicketsBloc', builder: (ctx, combined) {
        var user = combined.state1;
        var selectedTicket = combined.state2;
        var tickets = combined.state3;
        if (tickets.isEmpty) {
          return Text('Нет абонементов');
        } else {
          return Column(children: [
            for (var ticket in tickets)
              GestureDetector(
                child: UiTicket(ticket,
                  leading: Radio<CrudEntityTicket?>(
                    onChanged: null,
                    value: ticket,
                    groupValue: selectedTicket,
                  ),),
                onTap: () => selectedTicketBloc.state = ticket != selectedTicket ? ticket : null,
              ),
            if (selectedTicket == null) UiDivider('Посещения ${user.displayName} с ${localDateFormat.format(start)}')
            else UiDivider("Все посещения по абонементу '${selectedTicket.ticketType.name}'"),
          ],);
        }
      }),
      Flexible(child: BlocProvider.streamBuilder<List<CrudEntityVisit>, UserVisitsBloc>(blocName: 'CrudVisitBloc', builder: (ctx, visits) {
        log.fine('Visits: ${visits.length}');
        if (visits.isEmpty) {
          return Text('Нет посещений');
        } else {
          return ListView(children: [
            for (var visit in visits) UiVisit(visit, forTrainer: true,),
          ],);
        }
      }),),
    ]),),
      floatingActionButton: FloatingActionButton(
        onPressed: () => _onAddTraining(context, selectedUserBloc, visitsBloc),
        tooltip: 'Add',
        child: Icon(Icons.add),
      ),
    );
  }

  Future<void> _onAddTraining(BuildContext context, SelectedUserBloc selectedUserBloc, UserVisitsBloc visitsBloc) async {
    Session session = Injector().get<Session>();
    var training = await UiSelectorTrainingDialog(title: 'Отметить тренировку', dateRange: DateTimeRange(
        start: start,
        end: now),
      trainingFilter: (training) => training.trainer == session.user,
    ).selectTraining(context);
    log.finer("Select training dialog result: $training");
    if (training != null) {
      CrudEntityVisit visit = CrudEntityVisit(
          user: selectedUserBloc.state,
          training: training,
          trainingId: training.id,
          markMaster: CrudEntityVisitMark.on);
      visitsBloc.markMaster(visit, CrudEntityVisitMark.on);
    }
  }
}

class SelectedUserBloc extends BlocBaseState<CrudEntityUser> {
  SelectedUserBloc({required CrudEntityUser user, required BlocProvider provider, String? name}): super(state: user, provider: provider, name: name);
}

class UserTicketsBloc extends BlocBaseList<CrudEntityTicket> {
  SelectedUserBloc selectedUserBloc;

  UserTicketsBloc(this.selectedUserBloc, {required BlocProvider provider, String? name}): super(provider: provider, name: name) {
    // selectedUserBloc.stateOut.forEach((user) => loadUserTickets);
    addDisposableSubscription(selectedUserBloc.stateOut.listen((u) => loadUserTickets, onError: (e,s) => log.warning('loadUserTickets(u).Error', e, s), onDone: () => log.fine('loadUserTickets(u).Done'), cancelOnError: false));
  }

  void loadUserTickets() async {
    var user = selectedUserBloc.state;

    if (user.tickets == null) {
      state = [];
      user.tickets = (await backend.requestJson(
              'GET', '/api/tickets/byUser/${user.userId}') as List)
          .map((item) => CrudEntityTicket.fromJson(item)..user = user)
          .toList();
    }
    state = user.tickets!;
  }
}

class SelectedTicketBloc extends BlocBaseState<CrudEntityTicket?> {
  SelectedTicketBloc({required BlocProvider provider, String? name}): super(state: null, provider: provider, name: name);
}

class UserVisitsBloc extends CrudVisitBloc {
  DateTime start;
  SelectedUserBloc selectedUserBloc;
  SelectedTicketBloc selectedTicketBloc;

  UserVisitsBloc(this.start, this.selectedUserBloc, this.selectedTicketBloc, {required BlocProvider provider, String? name}): super(provider: provider, name: name) {
    addDisposableSubscription(selectedUserBloc.stateOut.listen((u) => loadUserVisits, onError: (e,s) => log.warning('loadUserVisits(u).Error', e, s), onDone: () => log.fine('loadUserVisits(u).Done'), cancelOnError: false));
    // selectedTicketBloc.stateOut.forEach((user) => loadUserVisits);
    addDisposableSubscription(selectedTicketBloc.stateOut.listen((t) => loadUserVisits(), onError: (e,s) => log.warning('loadUserVisits(t).Error', e, s), onDone: () => log.fine('loadUserVisits(t).Done'), cancelOnError: false));
  }

  void loadUserVisits() async {
    log.finest('loadUserVisits()...');
    var user = selectedUserBloc.state;
    var ticket = selectedTicketBloc.state;
    var params = {'from': dateTimeFormat.format(start)};

    if (ticket == null) {
      log.finest('loadUserVisits(). No ticket. Loading user visits');
      if (user.visits == null) {
        state = [];
        user.visits = (await backend.requestJson(
                    'GET', '/api/visit/byUser/${user.userId}', params: params) as List)
            .map((v) => CrudEntityVisit.fromJson(v)..user = user)
            .toList();
      }
      state = user.visits!;
    } else {
      log.finest('loadUserVisits(). Loading ticket visits');
      if (ticket.visits == null) {
        state = [];
        ticket.visits = (await backend.requestJson(
            'GET', '/api/visit/byTicket/${ticket.id}', params: params) as List)
            .map((v) =>
        CrudEntityVisit.fromJson(v)
          ..user = user
          ..ticket = ticket).toList();
      }
      state = ticket.visits!;
    }
    log.fine('Visits: ${state.length}');
  }
}
