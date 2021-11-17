import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:intl/intl.dart';

import 'wheel_list_selector.dart';

class UiTrainingSelector {

  final _dateTimeFormatter = DateFormat('EEE,dd HH:mm');
  final String title;

  UiTrainingSelector(this.title);

  Future<CrudEntityTraining?> selectTraining(BuildContext context, DateTimeRange range, {List<CrudEntityTrainingType>? types}) async {
    late final CrudTrainingTypeFilteredBloc trainingTypeBloc;
    late final CrudTrainingBloc trainingBloc;
    CrudEntityTraining? selectedTraining;
    return await showDialog(context: context,
        builder: (BuildContext c) => BlocProvider(
            init: (blocProvider) {
              trainingBloc = blocProvider.addBloc(bloc: CrudTrainingBloc());
              trainingTypeBloc = blocProvider.addBloc(bloc: CrudTrainingTypeFilteredBloc(trainingBloc));
              trainingTypeBloc.loadTrainings(range, types: types);
            },
            child: SimpleDialog(
                title: Text(title),
                elevation: 5,
                children: [
                  SizedBox(
                    height: 150,
                    child:
                    Row(children: [
                      Flexible(
                          flex: 10,
                          child: WheelListSelector<CrudEntityTrainingType>(
                            childBuilder: (context, index, trainingType) => Text(trainingType.trainingName),
                            onSelectedItemChanged: (ctx, i, data) => trainingTypeBloc.onTrainingTypeChange(data),
                            bloc: trainingTypeBloc,
                          )),
                      Flexible(
                          flex: 15,
                          child: WheelListSelector<CrudEntityTraining>(
                            childBuilder: (context, index, training) => Text('${_dateTimeFormatter.format(training.time)} ${training.trainer.nickName}'),
                            onSelectedItemChanged: (ctx, i, data) => selectedTraining = data,
                            bloc: trainingBloc,
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
