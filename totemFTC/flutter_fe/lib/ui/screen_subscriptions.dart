import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_ticket.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/ui/widgets/ui_attend.dart';
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

  late final Session _session;
  late final CrudVisitBloc _visitBloc;

  @override
  Widget build(BuildContext context) {
    return BlocProvider(
        init: (blocProvider) {
          _session = Injector().get<Session>();
          blocProvider.dynBlocList<CrudEntityTicket, CrudTicketBloc>().loadTickets();
          _visitBloc = blocProvider.dynBlocList<CrudEntityVisit, CrudVisitBloc>();
          _visitBloc.loadVisits(DateTime.now().subtract(Duration(days: 14)), 10);
        },
        child: UiScreen(
          body: subscriptionsList(),
          floatingActionButton: FloatingActionButton(
            onPressed: () => _onAddTraining(context, _visitBloc.selectedTicket),
            tooltip: 'Add',
            child: Icon(Icons.add),
          ),
        )
    );
  }

  Widget subscriptionsList() {
    return Column(
        children: [
          BlocProvider.streamBuilderList<CrudEntityTicket>((data) => Column(children: [
            if (data.isNotEmpty) UiDivider('Абонементы'),
            for (var ticket in data)
              GestureDetector(
                onTap: () => _visitBloc.loadTicketVisits(_session.user, ticket),
                child: UiSubscription(ticket),
              )
          ])),
          BlocProvider.streamBuilderList<CrudEntityVisit>((data) => Column(
            children: [
              UiDivider(ticketName('Выберите абонемент', 'Посещения', _visitBloc.selectedTicket)),
              for (var visit in data) UiAttend(visit),
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

  Future<void> _onAddTraining(BuildContext context, CrudEntityTicket? ticket) async {
    DateTime now = DateTime.now();
    var result = await UiTrainingSelector(ticketName('Отметить посещение', 'Отметить абонемент', ticket))
        .selectTraining(context, DateTimeRange(start: now.subtract(Duration(days: 7)), end: now),
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
