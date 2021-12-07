import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/misc/utils.dart';

import 'ui_wheel_list_selector.dart';

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
  static final now = DateTime.now();

  late final CrudTrainingTypeFilteredBloc trainingTypeBloc;
  late final SelectedTrainingBloc selectedTrainingBloc;

  @override
  void initState() {
    super.initState();
    final CrudTrainingBloc trainingBloc = CrudTrainingBloc(provider: this);
    selectedTrainingBloc = SelectedTrainingBloc(provider: this);
    trainingTypeBloc = CrudTrainingTypeFilteredBloc(visibleTrainingBloc: trainingBloc, selectedTrainingBloc: selectedTrainingBloc, provider: this)
      ..loadTrainings(dateRange: widget.dateRange, types: widget.types, dateFilter: widget.dateFilter, trainingFilter: widget.trainingFilter);
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return SimpleDialog(
        title: Text(widget.title),
        elevation: 5,
        children: [
          SizedBox(
            height: 150,
            child: Row(children: [
              Expanded(
                  flex: 10,
                  child: UiWheelListSelector<CrudEntityTrainingType, CrudTrainingTypeFilteredBloc>(
                    childBuilder: (context, index, trainingType) => Center(child: Text(trainingType.trainingName),),
                    onSelectedItemChanged: (ctx, i, data) {
                      trainingTypeBloc.onTrainingTypeChange(data);
                    },)),
              Expanded(
                  flex: 15,
                  child: UiWheelListSelector<CrudEntityTraining, CrudTrainingBloc>(
                    childBuilder: (context, index, training) => Center(child: Text('${timeFormat.format(training.time)} ${training.trainer?.nickName}'),),
                    transformedChildBuilder: (context, index, item) => Center(child: Container(
                      child: Text(item == now ? 'Сейчас': localDateFormat.format(item)),
                      padding: const EdgeInsets.only(left: 8, right: 8,),
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.all(Radius.circular(20.0)),
                        color: theme.colorScheme.background.withAlpha(80),
                      ),),
                    ),
                    onSelectedItemChanged: (ctx, i, data) => selectedTrainingBloc.state = data,
                    onSelectedTransformedItemChanged: (ctx, i, item) => selectedTrainingBloc.state = null,
                    selectedItem: now,
                    dataTransformer: (data) => addDates(data, now),
                  )),
            ],),
          ),
          Row(children: [
            BlocProvider.streamBuilder<CrudEntityTraining?, SelectedTrainingBloc>(builder: (ctx, selectedTraining) => TextButton(
              child: Text('Ok'),
              onPressed: selectedTraining != null ? () => Navigator.pop(context, selectedTraining) : null,
            ),),
            TextButton(
              child: const Text('Cancel'),
              onPressed: () => Navigator.pop(context),
            ),
          ],),
        ]);
  }
}
