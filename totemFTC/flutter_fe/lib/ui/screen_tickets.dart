import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_ticket.dart';
import 'package:flutter_fe/blocs/crud_user.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:flutter_fe/ui/widgets/ui_selector_training.dart';
import 'package:flutter_fe/ui/widgets/ui_visit.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:intl/intl.dart';
import 'package:logging/logging.dart';

import 'screen_base.dart';
import 'widgets/ui_divider.dart';
import 'widgets/ui_ticket.dart';

// todo add radio selector for ticket
class ScreenTickets extends StatefulWidget {
  static const routeName = '/tickets';

  @override
  State createState() => ScreenTicketsState();
}

class ScreenTicketsState extends BlocProvider<ScreenTickets> {

  late final Session session;
  late final SelectedTicketBloc selectedTicketBloc;
  late final CrudVisitBloc visitBloc;
  late final DateTime start;

  @override
  void initState() {
    super.initState();
    session = Injector().get<Session>();
    start = DateTime.now().subtract(Duration(days: 14));
    CrudTicketBloc ticketBloc = CrudTicketBloc(provider: this)
      ..loadTickets();
    selectedTicketBloc = SelectedTicketBloc(provider: this);
    combine2<CrudEntityTicket?, List<CrudEntityTicket>>('AllTicketsBloc', selectedTicketBloc, ticketBloc);
    visitBloc = CrudVisitBloc(start: start, selectedTicketBloc: selectedTicketBloc, ticketBloc: ticketBloc, provider: this)
      ..loadCurrentUserVisits();
  }

  @override
  Widget build(BuildContext context) {
    return UiScreen(body: Column(
        children: [
          BlocProvider.streamBuilder<Combined2<CrudEntityTicket?, List<CrudEntityTicket>>, BlocBaseState<Combined2<CrudEntityTicket?, List<CrudEntityTicket>>>>(blocName: 'AllTicketsBloc', builder: (ctx, combined2) {
            var selectedTicket = combined2.state1;
            var tickets = combined2.state2;
            return Column(children: [
              UiDivider(tickets.isNotEmpty ? 'Абонементы' : null),
              if (tickets.isEmpty) Text('Абонементов нет'),
              for (var ticket in tickets)
                GestureDetector(
                  onTap: () => selectedTicketBloc.state = selectedTicket != ticket ? ticket : null,
                  child: UiTicket(ticket,
                    leading: Radio<CrudEntityTicket?>(
                      onChanged: null,
                      value: ticket,
                      groupValue: selectedTicket,
                    ),),
                ),
              UiDivider(selectedTicket != null ? 'Посещения по абонементу ${selectedTicket.displayName}' : 'Посещения с ${localDateFormat.format(start)}'),
            ],);
          }),
          BlocProvider.streamBuilder<List<CrudEntityVisit>, CrudVisitBloc>(builder: (ctx, visits) => Column(children: [
            if (selectedTicketBloc.state != null && visits.isEmpty) Text('Посещений нет'),
            for (var visit in visits) UiVisit(visit),
          ])),
        ]
    ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => _onAddTraining(context),
        tooltip: 'Add',
        child: Icon(Icons.add),
      ),
    );
  }

  String _ticketName(String ifEmpty, String prefix, CrudEntityTicket? ticket) =>
      ticket != null ? '$prefix ${ticket.displayName}' : ifEmpty;

  Future<void> _onAddTraining(BuildContext context) async {
    DateTime now = DateTime.now();
    var selectedTicket = selectedTicketBloc.state;
    var result = await UiSelectorTrainingDialog(title: _ticketName('Отметить посещение', 'Отметить абонемент', selectedTicket),
        dateRange: DateTimeRange(start: now.subtract(Duration(days: 7)), end: now),
        types: selectedTicket?.ticketType.trainingTypes
    ).selectTraining(context);
    log.finer("Dialog result: $result");
    if (result != null) {
      bool _past = result.time.isBefore(now);
      CrudEntityVisit visit = CrudEntityVisit(
          user: session.user,
          // todo [?] pass ticket to mark training on specific ticket
          // ticket: selectedTicket,
          training: result,
          trainingId: result.id,
          markSchedule: _past ? false : true,
          markSelf: _past ? CrudEntityVisitMark.on : CrudEntityVisitMark.unmark,
          markMaster: CrudEntityVisitMark.unmark);
      var ticket = await visitBloc.markSelf(visit, CrudEntityVisitMark.on);

      // Select new ticket
      selectedTicketBloc.state = ticket;

      // if (ticket != null && selectedTicketBloc.state != null && selectedTicketBloc.state!.id != ticket.id) {
    }
  }
}
