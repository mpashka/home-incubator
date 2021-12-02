import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:intl/intl.dart';

import 'wheel_list_selector.dart';

class UiSelectorTrainingDialog extends StatefulWidget {

  final String title;
  final List<CrudEntityTrainingType>? types;
  final DateTimeRange? dateRange;
  final DateFilterInfo? dateFilter;
  final TrainingFilter? trainingFilter;

  UiSelectorTrainingDialog({required this.title, this.types, this.dateRange, this.dateFilter, this.trainingFilter});

  @override
  State createState() => UiSelectorTrainingDialogState();

  Future<CrudEntityTraining?> selectTraining(BuildContext context) async {
    if (dateRange == null && dateFilter == null) {
      throw Exception('Internal error. Range or filter must be specified');
    }
    return await showDialog(context: context, builder: (c) => this);
  }
}

class UiSelectorTrainingDialogState extends BlocProvider<UiSelectorTrainingDialog> {

  late final CrudTrainingTypeFilteredBloc trainingTypeBloc;
  CrudEntityTraining? selectedTraining;

  @override
  void initState() {
    super.initState();
    final CrudTrainingBloc trainingBloc = CrudTrainingBloc(provider: this);
    trainingTypeBloc = CrudTrainingTypeFilteredBloc(trainingBloc, provider: this)
      ..loadTrainings(dateRange: widget.dateRange, types: widget.types, dateFilter: widget.dateFilter, trainingFilter: widget.trainingFilter);
  }

  @override
  Widget build(BuildContext context) {
    return SimpleDialog(
        title: Text(widget.title),
        elevation: 5,
        children: [
          SizedBox(
            height: 150,
            child: Row(children: [
              Expanded(
                  flex: 10,
                  child: WheelListSelector<CrudEntityTrainingType, CrudTrainingTypeFilteredBloc>(
                    childBuilder: (context, index, trainingType) => Text(trainingType.trainingName),
                    onSelectedItemChanged: (ctx, i, data) => trainingTypeBloc.onTrainingTypeChange(data),
                  )),
              Expanded(
                  flex: 15,
                  child: WheelListSelector<CrudEntityTraining, CrudTrainingBloc>(
                    childBuilder: (context, index, training) => Text('${localDateTimeFormat.format(training.time)} ${training.trainer?.nickName}'),
                    onSelectedItemChanged: (ctx, i, data) => selectedTraining = data,
                  )),
            ],),
          ),
          Row(children: [
            SimpleDialogOption(
              child: const Text('Ok'),
              onPressed: () => Navigator.pop(context, selectedTraining),
            ),
            SimpleDialogOption(
              child: const Text('Cancel'),
              onPressed: () => Navigator.pop(context),
            ),
          ],)
        ]);
  }
}
