import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../blocs/crud_ticket.dart';

@immutable
class UiSubscription extends StatelessWidget {

  static final format = DateFormat('yyyy-MM-dd');

  final CrudEntityTicket _ticket;

  const UiSubscription(this._ticket, {Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    var start = _ticket.start;
    var end = _ticket.end;
    String dates;
    if (end != null) {
      dates = ' закрыт ${format.format(end)}';
    } else if (start != null) {
      dates = ' ${format.format(start)} - до ${format.format(start.add(Duration(days: _ticket.ticketType.days)))}';
    } else {
      dates = ' не начался';
    }
    return Card(
        color: theme.colorScheme.secondary,
        child: ListTile(
            leading: const Icon(Icons.baby_changing_station_rounded),
            title: Text('${_ticket.ticketType.name} ${_ticket.visited}/${_ticket.ticketType.visits}'),
            subtitle: Text('Покупка ${format.format(_ticket.buy)} $dates')
        )
    );
  }
}
