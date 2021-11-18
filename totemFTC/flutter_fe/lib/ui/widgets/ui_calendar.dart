import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/painting.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:intl/intl.dart';
import 'package:logging/logging.dart';

class UiCalendar extends StatefulWidget {
  final int weeks;
  final Set<DateTime> selectedDates;
  final void Function(DateFilterInfo) onFilterChange;

  const UiCalendar({required this.weeks, required this.selectedDates, required this.onFilterChange, Key? key}) : super(key: key);

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
  static const charWidth = 8.0;
  late final int _weeks;
  late final int _widgetRows;
  static const selectionRadiusConst = Radius.circular(15);

  _SelectionType _selectionType = _SelectionType.none;
  int _selectionIndex = 0;
  int _selectionIndexEnd = 0;

  late final DateTime now;
  late final DateTime firstDay;
  late final DateTime lastDay;
  late final DateFilterInfo filterNone;
  /// Last value shown in calendar (used for now and hide)
  int _lastIndex = 0;

  @override
  void initState() {
    super.initState();
    _weeks = widget.weeks;
    _widgetRows = _weeks+headerRows;
    DateTime now = DateTime.now();
    now = DateTime(now.year, now.month, now.day);
    var calendarDays = now.weekday - 1 + 7*(_weeks-1);
    firstDay = now.subtract(Duration(days: calendarDays));
    lastDay = now.add(Duration(days: 1));
    this.now = now;
    filterNone = filter(_SelectionType.none, firstDay, lastDay);
    _lastIndex = widgetIndex(calendarDays);
    widget.onFilterChange(filterNone);
  }

  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
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
          _buildItem(0, 0, width, height, underline: true, vertlineEnd: true),
          _buildItem(0, 1, width, height, text: "#", underline: true, vertlineEnd: true),
          // for (var day in weekDays) _buildItem(context, size, text: day),
          for (int day = 0; day < 7; day++)
            // Week day widget
            _buildItem(0, day+headerColumns, width, height, text: weekDayFormat.format(weekDayYear.add(Duration(days: day))), underline: true,
              onTap: () => setSelection(filter(_SelectionType.column, firstDay.add(Duration(days: day)), lastDay), day+2),
            ),
        ],),
        // Dates
        for (var weekNum = 0, weekDate = firstDay.add(Duration(days: weekNum*7)); weekNum < _weeks; weekNum++, weekDate = firstDay.add(Duration(days: weekNum*7)))
          Row(children: [
            _buildItem(weekNum+headerRows, 0, width, height, underline: isLastWeek(weekDate), vertlineEnd: true),
            // Week # widget
            _buildItem(weekNum+headerRows, 1, width, height,
              text: (weekDate.difference(DateTime(weekDate.year, 1, 1, 0, 0)).inDays / 7).ceil().toString(),
              underline: isLastWeek(weekDate), vertlineEnd: true,
              onTap: () => setSelection(filter(_SelectionType.row, weekDate, weekDate.add(Duration(days: 7))), weekNum+1),
            ),
            for (var dayNum = 0, dayDate = weekDate.add(Duration(days: dayNum)); dayNum < 7; dayNum++, dayDate = weekDate.add(Duration(days: dayNum)))
              if (dayDate.isBefore(lastDay)) _buildItem(weekNum+headerRows, dayNum+headerColumns, width, height,
                  training: widget.selectedDates.contains(dayDate),
                  text: dayDate.day.toString(), underline: isLastWeek(dayDate), vertlineStart: dayNum > 0 && dayDate.day == 1,
                  onTap: () => setSelection(filter(_SelectionType.single, dayDate, dayDate.add(Duration(days: 1))), (weekNum+headerRows)*widgetColumns + dayNum+headerColumns))
              else _buildItem(weekNum+headerRows, dayNum+headerColumns, width, height, noDecoration: true)
          ],)
      ],),
      ..._buildMonthNames(width, height),
    ],);
  }

  void setSelection(DateFilterInfo filter, int selectionIndex, [int selectionIndexEnd=0]) {
    // log.finer('Selection $selectionType, $selectionIndex -> $selectionIndexEnd');
    setState(() {
      _SelectionType selectionType = _SelectionType.values[DateSelectionType.values.indexOf(filter.type)];
      if (_selectionType == selectionType && _selectionIndex == selectionIndex) {
        _selectionType = _SelectionType.none;
        widget.onFilterChange(filterNone);
      } else {
        _selectionType = selectionType;
        _selectionIndex = selectionIndex;
        _selectionIndexEnd = selectionIndexEnd;
        widget.onFilterChange(filter);
      }
    });
  }

  DateFilterInfo filter(_SelectionType selectionType, DateTime start, DateTime end) {
    return DateFilterInfo(
        selectionType == _SelectionType.column
            ? (DateTime date) => date.isAfter(start) && date.isBefore(end) && date.weekday == start.weekday
            : (DateTime date) => date.isAfter(start) && date.isBefore(end),
        DateTimeRange(start: start, end: end),
        DateSelectionType.values[_SelectionType.values.indexOf(selectionType)]);
  }

  ///
  int widgetIndex(int calendarIndex, [bool shiftZero=false]) {
    int row = calendarIndex ~/ 7;
    int column = calendarIndex % 7;
    return (row+headerRows) * widgetColumns + column + (column == 0 && shiftZero ? 0 : headerColumns);
  }

  /// firstDay - first day shown in calendar
  List<Widget> _buildMonthNames(double width, double height) {
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
            onTap: () => setSelection(filter(_SelectionType.interval, firstMonthDay, lastMonthDay), widgetIndex(firstDayIdx, true), widgetIndex(lastDayIdx)),
          )));
    }
    return monthNames;
  }

  Widget _buildItem(int row, int column, double width, double height,
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
      if (training) Positioned(bottom: 0, left: max(0, width / 2 - (text == null ? 0 : text.length * charWidth / 2 + charWidth)),
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

enum _SelectionType {
  none, single, row, column, interval
}
