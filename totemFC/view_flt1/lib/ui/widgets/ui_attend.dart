import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

@immutable
class UiAttend extends StatelessWidget {
  static final format = DateFormat('yyyy-MM-dd kk:mm');

  final String name;
  final DateTime date;
  final bool marked;


  UiAttend({
    required this.name,
    required this.date,
    required this.marked});

  @override
  Widget build(BuildContext context) {
    return ListTile(
        leading: Icon(Icons.agriculture_rounded),
        title: Text('Групповая тренировка ${format.format(date)}'),
    );
  }
}

