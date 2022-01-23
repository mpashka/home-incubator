import 'dart:core';

import 'package:flutter/foundation.dart';
import 'package:json_annotation/json_annotation.dart';

import '../misc/utils.dart';
import 'bloc_provider.dart';
import 'crud_api.dart';
import 'crud_user.dart';

part 'crud_finance.g.dart';

class CrudIncomeBloc extends BlocBaseList<CrudEntityIncome> {

  CrudIncomeBloc({List<CrudEntityIncome> state = const [], required BlocProvider provider, String? name}): super(provider: provider, state: state, name: name);

  void loadCurrentUserIncome(DateTime from, FinancePeriod period) async {
    state = (await backend.requestJson('GET', '/api/finance/currentTrainerIncome/${describeEnum(period)}', params: {'from': dateFormat.format(from)}) as List)
        .map((item) => CrudEntityIncome.fromJson(item))
        .toList();
  }

  void loadIncome(DateTime from, FinancePeriod period) async {
    state = (await backend.requestJson('GET', '/api/finance/trainerIncome/${describeEnum(period)}', params: {'from': dateFormat.format(from)}) as List)
        .map((item) => CrudEntityIncome.fromJson(item)..trainer = session.user)
        .toList();
  }
}

@JsonSerializable(explicitToJson: true)
class CrudEntityIncome {
  @JsonKey(fromJson: dateFromJson, toJson: dateToJson)
  DateTime date;
  int trainings;
  int visits;
  double ticketIncome;
  double income;
  CrudEntityUser? trainer;

  CrudEntityIncome({required this.date, required this.trainings, required this.visits, required this.ticketIncome, required this.income, this.trainer});

  factory CrudEntityIncome.fromJson(Map<String, dynamic> json) => _$CrudEntityIncomeFromJson(json);
  Map<String, dynamic> toJson() => _$CrudEntityIncomeToJson(this);

  @override
  String toString() {
    return 'CrudEntityTraining{date: $date, trainings: $trainings, visists: $visits, income: $income}';
  }
}

enum FinancePeriod {
  week, month
}
