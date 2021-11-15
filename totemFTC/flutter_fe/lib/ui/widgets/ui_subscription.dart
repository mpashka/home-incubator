import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../blocs/crud_ticket.dart';

@immutable
class UiSubscription extends StatelessWidget {

  static final formatDateTime = DateFormat('yyyy-MM-dd HH:mm');
  static final formatDate = DateFormat('yyyy-MM-dd');

  final CrudEntityTicket _ticket;

  UiSubscription(this._ticket) : super(key: ValueKey(_ticket.id));

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    var start = _ticket.start;
    var end = _ticket.end;
    String dates;
    if (end != null) {
      dates = ' закрыт ${formatDate.format(end)}';
    } else if (start != null) {
      dates = ' ${formatDate.format(start)} - до ${formatDate.format(start.add(Duration(days: _ticket.ticketType.days)))}';
    } else {
      dates = ' не начался';
    }
    return Card(
        color: theme.colorScheme.secondary,
        child: ListTile(
          leading: const Icon(Icons.baby_changing_station_rounded),
          title: Text(_ticket.ticketType.name),
          subtitle: Text('Покупка ${formatDateTime.format(_ticket.buy)} $dates'),
          trailing: Text('${_ticket.visited} из ${_ticket.ticketType.visits}', textScaleFactor: 2,),
        ),
    );
  }
}
