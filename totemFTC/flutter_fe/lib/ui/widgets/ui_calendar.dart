import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/painting.dart';
import 'package:intl/intl.dart';
import 'package:logging/logging.dart';

class UiCalendar extends StatefulWidget {
  final int weeks;
  final Set<DateTime> selectedDates;
  final void Function(DateSelectionType, {DateTime? dateTime}) onDateChange;

  const UiCalendar({required this.weeks, required this.selectedDates, required this.onDateChange, Key? key}) : super(key: key);

  @override
  State createState() => UiCalendarState();
}

class UiCalendarState extends State<UiCalendar> {
  static final Logger log = Logger('UiCalendarState');

  static final monthFormat = DateFormat('MMM');
  static final weekDayFormat = DateFormat('EE');
  static final DateTime weekDayYear = DateTime(1);

  static const headerColumns = 2;
  static const headerRows = 1;
  static const widgetColumns = 7+headerColumns;
  static const double lineWidth = 1;
  late final int _weeks;
  late final int _widgetRows;
  static const selectionRadiusConst = Radius.circular(15);

  _SelectionType _selectionType = _SelectionType.none;
  int _selectionIndex = 0;
  int _selectionIndexEnd = 0;

  /// Last value shown in calendar (used for now and hide)
  int _lastIndex = 0;


  @override
  void initState() {
    super.initState();
    _weeks = widget.weeks;
    _widgetRows = _weeks+headerRows;
  }

  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    DateTime now = DateTime.now();
    now = DateTime(now.year, now.month, now.day);
    var calendarDays = now.weekday - 1 + 7*(_weeks-1);
    DateTime firstDay = now.subtract(Duration(days: calendarDays));
    _lastIndex = widgetIndex(calendarDays);

    final theme = Theme.of(context);
    var font = theme.textTheme.subtitle1;
    double height = 2 + [font?.fontSize, font?.height].fold(1, (double p, double? e) => e == null ? p : p*e);
    double screenWidth = MediaQuery.of(context).size.width;
    double width = screenWidth / (7 + 2);

    bool isLastWeek(DateTime dayTime) => dayTime.month < dayTime.add(Duration(days: 7)).month;
    // bool isLastMonthDay(DateTime dayTime) => dayTime.month < dayTime.add(Duration(days: 1)).month;

    return Stack(children: [Column(
      children: [
        // Header
        Row(children: [
          _buildItem(context, 0, 0, width, height, underline: true, vertlineEnd: true),
          _buildItem(context, 0, 1, width, height, text: "#", underline: true, vertlineEnd: true),
          // for (var day in weekDays) _buildItem(context, size, text: day),
          for (int day = 0; day < 7; day++)
            _buildItem(context, 0, day+headerColumns, width, height, text: weekDayFormat.format(weekDayYear.add(Duration(days: day))), underline: true,
              onTap: () => setSelection(_SelectionType.column, weekDayYear.add(Duration(days: day)), day+2),
            ),
        ],),
        // Dates
        for (var week = 0, weekStart = firstDay.add(Duration(days: week*7)); week < _weeks; week++, weekStart = firstDay.add(Duration(days: week*7)))
          Row(children: [
            _buildItem(context, week+headerRows, 0, width, height, underline: isLastWeek(weekStart), vertlineEnd: true),
            _buildItem(context, week+headerRows, 1, width, height,
              text: (weekStart.difference(DateTime(weekStart.year, 1, 1, 0, 0)).inDays / 7).ceil().toString(),
              underline: isLastWeek(weekStart), vertlineEnd: true,
              onTap: () => setSelection(_SelectionType.row, weekStart, week+1),
            ),
            for (var day = 0, dayTime = weekStart.add(Duration(days: day)); day < 7; day++, dayTime = weekStart.add(Duration(days: day)))
              if (!dayTime.isAfter(now)) _buildItem(context, week+headerRows, day+headerColumns, width, height,
                  training: widget.selectedDates.contains(dayTime),
                  text: dayTime.day.toString(), underline: isLastWeek(dayTime), vertlineStart: day > 0 && dayTime.day == 1,
                  onTap: () => setSelection(_SelectionType.single, dayTime, (week+headerRows)*widgetColumns + day+headerColumns))
              else _buildItem(context, week+headerRows, day+headerColumns, width, height, noDecoration: true)
          ],)
      ],),
      ..._buildMonthNames(context, firstDay, now, width, height),
    ],);
  }

  void setSelection(_SelectionType selectionType, DateTime dateTime, int selectionIndex, [int selectionIndexEnd=0]) {
    // log.finer('Selection $selectionType, $selectionIndex -> $selectionIndexEnd');
    setState(() {
      if (_selectionType == selectionType && _selectionIndex == selectionIndex) {
        _selectionType = _SelectionType.none;
        widget.onDateChange(DateSelectionType.none);
      } else {
        _selectionType = selectionType;
        _selectionIndex = selectionIndex;
        _selectionIndexEnd = selectionIndexEnd;
        widget.onDateChange(DateSelectionType.values[_SelectionType.values.indexOf(selectionType)], dateTime: dateTime);
      }
    });
  }

  ///
  int widgetIndex(int calendarIndex, [bool shiftZero=false]) {
    int row = calendarIndex ~/ 7;
    int column = calendarIndex % 7;
    return (row+headerRows) * widgetColumns + column + (column == 0 && shiftZero ? 0 : headerColumns);
  }

  /// firstDay - first day shown in calendar
  List<Widget> _buildMonthNames(BuildContext context, DateTime firstDay, DateTime now, double width, double height) {
    var monthNames = <Widget>[];
    int yearMonth(DateTime date) => date.year*12 + date.month;
    for (int ym = yearMonth(firstDay), index = 0; ym <= yearMonth(now); ym++, index++) {
      DateTime firstMonthDay = DateTime(ym ~/ 12, ym % 12);
      DateTime lastMonthDay = DateTime(ym ~/ 12, ym % 12 + 1, 0);
      int firstWeek = max(0, (firstMonthDay.difference(firstDay).inDays / 7).ceil());
      int lastWeek = min(_weeks-1, (lastMonthDay.difference(firstDay).inDays / 7).truncate());
      bool last = ym == yearMonth(now);
      // log.finer('First day: $firstMonthDay');
      // log.finer('Last day: $lastMonthDay');
      // log.finer('${monthFormat.format(firstMonthDay)}, firstWeek: $firstWeek, lastWeek: $lastWeek');
      int firstDayIdx = max(0, firstMonthDay.difference(firstDay).inDays);
      int lastDayIdx = min(lastMonthDay.difference(firstDay).inDays, now.difference(firstDay).inDays);
      monthNames.add(Positioned(
          top: height * (firstWeek + 1),
          left: 0,
          child: GestureDetector(
            child: Container(
              width: width - 2,
              height: height * (lastWeek - firstWeek + 1) - (last ? 0 : 2),
              alignment: Alignment.center,
              child: RotatedBox(quarterTurns: -1, child: Text(monthFormat.format(firstMonthDay), textAlign: TextAlign.center,softWrap: true, overflow: TextOverflow.clip,),),
              decoration: index % 2 == 1 ? null : BoxDecoration(color: Colors.grey.shade500.withAlpha(50)),
            ),
            onTap: () => setSelection(_SelectionType.interval, firstMonthDay, widgetIndex(firstDayIdx, true), widgetIndex(lastDayIdx)),
          )));
    }
    return monthNames;
  }

  Widget _buildItem(BuildContext context, int row, int column, double width, double height,
      {String? text, GestureTapCallback? onTap, bool training=false, bool underline=false, bool vertlineStart=false, bool vertlineEnd=false, bool noDecoration=false}) {
    final theme = Theme.of(context);
    BorderRadiusGeometry? selectionRadius;
    bool selection = false;
    int widgetIndex = row * widgetColumns + column;
    if (_selectionType == _SelectionType.column && column == _selectionIndex && widgetIndex <= _lastIndex) {
      selection = true;
      if (row == 0) {
        selectionRadius = BorderRadius.only(topLeft: selectionRadiusConst, topRight: selectionRadiusConst);
      } else if (row == _widgetRows-1 || widgetIndex+widgetColumns > _lastIndex) {
        selectionRadius = BorderRadius.only(bottomLeft: selectionRadiusConst, bottomRight: selectionRadiusConst);
      }
    } else if (_selectionType == _SelectionType.row && row == _selectionIndex && column > 0 && widgetIndex <= _lastIndex) {
      selection = true;
      if (column == 1) {
        selectionRadius = BorderRadius.only(topLeft: selectionRadiusConst, bottomLeft: selectionRadiusConst);
      } else if (column == widgetColumns-1 || widgetIndex == _lastIndex) {
        selectionRadius = BorderRadius.only(topRight: selectionRadiusConst, bottomRight: selectionRadiusConst);
      }
    } else if (_selectionType == _SelectionType.interval && widgetIndex >= _selectionIndex && widgetIndex <= _selectionIndexEnd) {
      selection = true;
      bool firstLine = widgetIndex-widgetColumns < _selectionIndex;
      bool lastLine = widgetIndex+widgetColumns > _selectionIndexEnd;
      if (widgetIndex == _selectionIndex) {
        selectionRadius = BorderRadius.only(topLeft: selectionRadiusConst, topRight: column == widgetColumns-1 ? selectionRadiusConst : Radius.zero);
      } else if (widgetIndex == _selectionIndexEnd) {
        selectionRadius = BorderRadius.only(bottomRight: selectionRadiusConst);
      } else if (column == 0 && firstLine) {
        selectionRadius = BorderRadius.only(topLeft: selectionRadiusConst);
      } else if (column == 0 && lastLine) {
        selectionRadius = BorderRadius.only(bottomLeft: selectionRadiusConst);
      } else if (column == widgetColumns-1 && firstLine) {
        selectionRadius = BorderRadius.only(topRight: selectionRadiusConst);
      } else if (column == widgetColumns-1 && lastLine) {
        selectionRadius = BorderRadius.only(bottomRight: selectionRadiusConst);
      }
    } else if (_selectionType == _SelectionType.single && widgetIndex == _selectionIndex) {
      selection = true;
      selectionRadius = BorderRadius.all(selectionRadiusConst);
    }

    return Expanded(child: Stack(children: [
      if (selection) Container(
        decoration: BoxDecoration(
          color: theme.colorScheme.secondary.withAlpha(150),
          borderRadius: selectionRadius,
        ),
        width: width,
        height: height,
      ),
      GestureDetector(child: Container(
        alignment: Alignment.center,
        child: text != null ? Text(text/*, textAlign: TextAlign.center,*/) : null,
        constraints: BoxConstraints(minHeight: height,),
        decoration: column % 2 == 0 || noDecoration ? null : BoxDecoration(color: Colors.grey.shade500.withAlpha(50)),
      ),
        behavior: HitTestBehavior.opaque,
        onTap: onTap,
      ),
      if (training) Positioned(bottom: 0,
        child: Icon(Icons.circle, size: 7),
      ),
      if (underline) Positioned(
          bottom: 0,
          child: Container(
            color: theme.colorScheme.onSurface,
            height: lineWidth,
            width: width+1,
          )),
      if (vertlineStart) Positioned(
          left: 0,
          child: Container(
            color: theme.colorScheme.onSurface,
            height: height+1,
            width: lineWidth,
          )),
      if (vertlineEnd) Positioned(
          right: 0,
          child: Container(
            color: theme.colorScheme.onSurface,
            height: height+1,
            width: lineWidth,
          )),
    ],));
  }

}

enum DateSelectionType {
  none, day, week, weekDay, month
}

enum _SelectionType {
  none, single, row, column, interval
}
