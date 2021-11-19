import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

@immutable
class UiTraining extends StatelessWidget {

  static final format = DateFormat('yyyy-MM-dd kk:mm');

  final String name;
  final DateTime date;

  UiTraining({
    required this.name,
    required this.date,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
        color: theme.accentColor,
        child: ListTile(
            leading: Icon(Icons.baby_changing_station_rounded),
            title: Text('$name ${format.format(date)}'),
        )
    );
  }
}
