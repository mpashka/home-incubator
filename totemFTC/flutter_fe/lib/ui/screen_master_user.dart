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
import 'widgets/ui_selector_ticket_type.dart';
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
  late final CrudTicketBloc ticketBloc;
  late final CrudVisitBloc visitsBloc;

  @override
  void initState() {
    super.initState();
    selectedUserBloc = SelectedUserBloc(user: widget._user, provider: this);
    ticketBloc = CrudTicketBloc(selectedUserBloc: selectedUserBloc, provider: this)..loadUserTickets();
    selectedTicketBloc = SelectedTicketBloc(provider: this);
    combine3<CrudEntityUser, CrudEntityTicket?, List<CrudEntityTicket>>('AllTicketsBloc', selectedUserBloc, selectedTicketBloc, ticketBloc);

    visitsBloc = CrudVisitBloc(start: start, selectedUserBloc: selectedUserBloc, selectedTicketBloc: selectedTicketBloc, ticketBloc: ticketBloc,
        provider: this, name: 'CrudVisitBloc')
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
      Row(children: [
        const Expanded(child: Divider(thickness: 3,)),
        Container(
          padding: EdgeInsets.symmetric(horizontal: 8),
          child: Text('Абонементы'),
        ),
        if (user.types.contains(CrudEntityUserType.admin)) GestureDetector(
          child: Icon(Icons.add_circle_outline),
          onTap: () => _onAddTicket(context),
        ),
      ]),
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
      Flexible(child: BlocProvider.streamBuilder<List<CrudEntityVisit>, CrudVisitBloc>(blocName: 'CrudVisitBloc', builder: (ctx, visits) {
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
        onPressed: () => _onAddTraining(context),
        tooltip: 'Add',
        child: Icon(Icons.add),
      ),
    );
  }

  Future<void> _onAddTraining(BuildContext context) async {
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

  Future<void> _onAddTicket(BuildContext context) async {
    var user = selectedUserBloc.state;
    var ticketType = await UiSelectorTicketTypeDialog(title: 'Добавить абонемент для ${user.displayName}',)
        .selectTicketType(context);
    log.finer("Select ticket type dialog result: $ticketType");
    if (ticketType != null) {
      CrudEntityTicket ticket = CrudEntityTicket(
        id: -1,
        ticketType: ticketType,
        user: selectedUserBloc.state,
        buy: DateTime.now(),
        visited: 0,
      );
      ticket = await ticketBloc.createTicket(ticket);
      selectedTicketBloc.state = ticket;
    }
  }
}
