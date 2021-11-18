import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_ticket.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/ui/widgets/ui_visit.dart';
import 'package:flutter_fe/ui/widgets/ui_training_selector.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:intl/intl.dart';
import 'package:logging/logging.dart';

import 'screen_base.dart';
import 'widgets/ui_divider.dart';
import 'widgets/ui_subscription.dart';

class ScreenTickets extends StatelessWidget {

  static final Logger log = Logger('SubscriptionsScreen');

  static final formatDateTime = DateFormat('yyyy-MM-dd HH:mm');

  @override
  Widget build(BuildContext context) {
    late final Session _session;
    late final CrudVisitBloc _visitBloc;
    return BlocProvider(
        init: (blocProvider) {
          _session = Injector().get<Session>();
          blocProvider.addBloc(bloc: CrudTicketBloc()).loadTickets();
          _visitBloc = blocProvider.addBloc(bloc: CrudVisitBloc());
          _visitBloc.loadVisits(DateTime.now().subtract(Duration(days: 14)), 10);
        },
        child: UiScreen(
          body: subscriptionsList(_session, _visitBloc),
          floatingActionButton: FloatingActionButton(
            onPressed: () => _onAddTraining(context, _session, _visitBloc, _visitBloc.selectedTicket),
            tooltip: 'Add',
            child: Icon(Icons.add),
          ),
        )
    );
  }

  Widget subscriptionsList(Session _session, CrudVisitBloc _visitBloc) {
    return Column(
        children: [
          BlocProvider.streamBuilder<List<CrudEntityTicket>, CrudTicketBloc>(builder: (data) => Column(children: [
            if (data.isNotEmpty) UiDivider('Абонементы'),
            for (var ticket in data)
              GestureDetector(
                onTap: () => _visitBloc.loadTicketVisits(_session.user, ticket),
                child: UiSubscription(ticket),
              )
          ])),
          BlocProvider.streamBuilder<List<CrudEntityVisit>, CrudVisitBloc>(builder: (data) => Column(
            children: [
              UiDivider(ticketName('Выберите абонемент', 'Посещения', _visitBloc.selectedTicket)),
              for (var visit in data) UiVisit(visit),
            ],
          )),
        ]
    );
  }

  String ticketName(String ifEmpty, String prefix, CrudEntityTicket? ticket) {
    if (ticket == null) {
      return ifEmpty;
    }
    return '$prefix ${ticket.ticketType.name} / ${formatDateTime.format(ticket.buy)}';
  }

  Future<void> _onAddTraining(BuildContext context, Session _session, CrudVisitBloc _visitBloc, CrudEntityTicket? ticket) async {
    DateTime now = DateTime.now();
    var result = await UiTrainingSelector(ticketName('Отметить посещение', 'Отметить абонемент', ticket))
        .selectTraining(context, range: DateTimeRange(start: now.subtract(Duration(days: 7)), end: now),
        types: ticket?.ticketType.trainingTypes
    );
    log.finer("Dialog result: $result");
    if (result != null) {
      bool _past = result.time.isBefore(now);
      CrudEntityVisit visit = CrudEntityVisit(
          user: _session.user,
          training: result,
          trainingId: result.id,
          markSchedule: _past ? false : true,
          markSelf: _past ? CrudEntityVisitMark.on : CrudEntityVisitMark.unmark,
          markMaster: CrudEntityVisitMark.unmark);
      _visitBloc.markSelf(visit, CrudEntityVisitMark.on);
    }
  }

}
