import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

@immutable
class UiSubscription extends StatelessWidget {

  static final format = DateFormat('yyyy-MM-dd – kk:mm');

  final String name;
  final DateTime start;
  final int count;
  final int used;

  UiSubscription({
    required this.name,
    required this.start,
    required this.count,
    required this.used});

  @override
  Widget build(BuildContext context) {
    return
      Card(
          child:
          ListTile(
              leading: Icon(Icons.baby_changing_station_rounded),
              title: Text('Групповая тренировка ${used}/${count}'),
              subtitle: Text('Покупка ${format.format(start)} - до ${format.format(start)}')
          )
      );
  }
}
