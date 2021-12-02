import 'dart:math';
import 'dart:developer' as developer;

import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

const _chars = 'AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz1234567890';
Random _rnd = Random();

String getRandomString(int length) => String.fromCharCodes(Iterable.generate(
    length, (_) => _chars.codeUnitAt(_rnd.nextInt(_chars.length))));

final dateFormat = DateFormat('yyyy-MM-dd');
final dateTimeFormat = DateFormat('yyyy-MM-dd HH:mm');
final localDateFormat = DateFormat('EEE, dd MMMM');
final localDateTimeFormat = DateFormat('EEE,dd HH:mm');
final timeFormat = DateFormat('HH:mm');
final fullDateFormat = DateFormat('yyyy MMMM dd, EEEE');

int compare<E>(int result, Comparable<E>? a, E? b) {
  if (result != 0) return result;
  if (a == null && b == null) return 0;
  if (a != null && b == null) return 1;
  if (a == null && b != null) return -1;
  return a!.compareTo(b!);
}

int compareId(int result, int aId, int bId) {
  if (result != 0) return result;
  if (aId == bId) return 0;
  return aId > bId ? 1 : -1;
}

extension DateTimeFactory on DateTime {
  DateTime dayDate() {
    return DateTime(year, month, day);
  }

  DateTime monthDate() {
    return DateTime(year, month);
  }
}

Type typeOf<T>() => T;

typedef DateFilter = bool Function(DateTime date);

enum DateSelectionType {
  none, day, week, weekDay, month
}

class DateFilterInfo {
  final DateFilter filter;
  final DateTimeRange range;
  final DateSelectionType type;

  DateFilterInfo(this.filter, this.range, this.type);
}

const backMaster = Duration(days: 7);
const forwardMaster = Duration(days: 7);
const back = Duration(days: 7);
const forward = Duration(days: 7);
