import 'package:flutter/material.dart';

@immutable
class UiPerson extends StatelessWidget {

  final String name;

  UiPerson({
    required this.name,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
        color: theme.accentColor,
        child: ListTile(
            leading: Icon(Icons.baby_changing_station_rounded),
            title: Text('$name'),
        )
    );
  }
}
