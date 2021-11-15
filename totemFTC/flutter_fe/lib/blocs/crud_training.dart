import 'dart:async';
import 'dart:collection';
import 'dart:core';

import 'package:flutter/material.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:logging/logging.dart';

import '../misc/utils.dart';
import 'bloc_provider.dart';
import 'crud_api.dart';
import 'crud_user.dart';

part 'crud_training.g.dart';

class CrudTrainingBloc extends BlocBaseList<CrudEntityTraining> {}

class CrudTrainingTypeBloc extends BlocBaseList<CrudEntityTrainingType> {
  BlocProvider blocProvider;
  List<CrudEntityTraining> _allTrainings = [];
  CrudEntityTraining? selectedTraining;

  CrudTrainingTypeBloc(this.blocProvider);

  Future<void> loadTrainings(DateTimeRange range, {List<CrudEntityTrainingType>? types}) async {
    _allTrainings = (await backend.requestJson('GET', '/api/training/byDateInterval', params: {
      'from': dateTimeFormat.format(range.start), 'to': dateTimeFormat.format(range.end)}) as List)
        .map((item) => CrudEntityTraining.fromJson(item))
        .toList();

    Set<CrudEntityTrainingType> trainingTypesSet = HashSet<CrudEntityTrainingType>();
    _allTrainings.forEach((training) => trainingTypesSet.add(training.trainingType));
    if (types != null) trainingTypesSet.retainAll(types);
    var trainingTypes = List.of(trainingTypesSet, growable: false);
    trainingTypes.sort();
    state = trainingTypes;
    if (trainingTypes.isNotEmpty) onTrainingTypeChange(trainingTypes[0]);
  }

  Future<void> onTrainingTypeChange(CrudEntityTrainingType trainingType) async {
    var trainings = <CrudEntityTraining>[];
    _allTrainings.forEach((training) {
      if (trainingType == training.trainingType) {
        trainings.add(training);
      }
    });
    blocProvider.dynBlocList<CrudEntityTraining, CrudTrainingBloc>().state = trainings;
    selectedTraining = trainings.isNotEmpty ? trainings[0] : null;
  }
}

@JsonSerializable(explicitToJson: true)
class CrudEntityTraining implements Comparable<CrudEntityTraining> {
  int id;
  @JsonKey(fromJson: dateTimeFromJson, toJson: dateTimeToJson)
  DateTime time;
  CrudEntityUser trainer;
  CrudEntityTrainingType trainingType;
  String? comment;

  CrudEntityTraining(
      {required this.id,
      required this.time,
      required this.trainer,
      required this.trainingType,
      this.comment});

  factory CrudEntityTraining.fromJson(Map<String, dynamic> json) =>
      _$CrudEntityTrainingFromJson(json);

  Map<String, dynamic> toJson() => _$CrudEntityTrainingToJson(this);

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is CrudEntityTraining &&
          runtimeType == other.runtimeType &&
          id == other.id;

  @override
  int get hashCode => id.hashCode;

  @override
  int compareTo(CrudEntityTraining other) {
    if (id == other.id) return 0;
    int result = compare(0, time, other.time);
    result = compare(result, trainingType, other.trainingType);
    result = compare(result, trainer, other.trainer);
    return compareId(result, id, other.id);
  }
}

@JsonSerializable()
class CrudEntityTrainingType implements Comparable<CrudEntityTrainingType> {
  String trainingType;
  String trainingName;

  CrudEntityTrainingType({required this.trainingType, required this.trainingName});

  factory CrudEntityTrainingType.fromJson(Map<String, dynamic> json) =>_$CrudEntityTrainingTypeFromJson(json);
  Map<String, dynamic> toJson() => _$CrudEntityTrainingTypeToJson(this);

  @override
  bool operator ==(Object other) {
    return other is CrudEntityTrainingType ? other.trainingType == trainingType : false;
  }

  @override
  int get hashCode => trainingType.hashCode;

  @override
  int compareTo(CrudEntityTrainingType other) {
    return trainingType.compareTo(other.trainingType);
  }
}
