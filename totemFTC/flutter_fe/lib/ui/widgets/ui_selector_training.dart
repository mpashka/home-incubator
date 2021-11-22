import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:intl/intl.dart';

import 'wheel_list_selector.dart';

class UiSelectorTraining {

  final String title;

  UiSelectorTraining(this.title);

  Future<CrudEntityTraining?> selectTraining(BuildContext context, {List<CrudEntityTrainingType>? types, DateTimeRange? dateRange, DateFilterInfo? dateFilter, TrainingFilter? trainingFilter}) async {
    if (dateRange == null && dateFilter == null) {
      throw Exception('Internal error. Range or filter must be specified');
    }
    late final CrudTrainingTypeFilteredBloc trainingTypeBloc;
    CrudEntityTraining? selectedTraining;
    return await showDialog(context: context,
        builder: (BuildContext c) => BlocProvider(
            init: (blocProvider) {
              final CrudTrainingBloc trainingBloc = blocProvider.addBloc(bloc: CrudTrainingBloc());
              trainingTypeBloc = blocProvider.addBloc(bloc: CrudTrainingTypeFilteredBloc(trainingBloc));
              trainingTypeBloc.loadTrainings(dateRange: dateRange, types: types, dateFilter: dateFilter, trainingFilter: trainingFilter);
            },
            child: SimpleDialog(
                title: Text(title),
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
                            childBuilder: (context, index, training) => Text('${localDateTimeFormat.format(training.time)} ${training.trainer.nickName}'),
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
                ])));
  }
}
