import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:intl/intl.dart';

@immutable
class UiTraining extends StatelessWidget {

  final CrudEntityTraining _training;
  final bool forSchedule;

  UiTraining(this._training, {this.forSchedule = false});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
        color: forSchedule ? null :theme.colorScheme.secondary,
        child: ListTile(
            leading: Icon(Icons.baby_changing_station_rounded),
            title: Text('${_training.trainingType.trainingName} ${localDateTimeFormat.format(_training.time)}'),
        )
    );
  }
}
