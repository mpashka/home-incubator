import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/painting.dart';
import 'package:intl/intl.dart';
import 'package:logging/logging.dart';

import 'screen_base.dart';

class ScreenTrainings extends StatelessWidget {

  static final Logger log = Logger('ScreenTrainings');

  static const List<String> weekDays = ["Пн","Вт","Ср","Чт","Пт","Сб","Вс",];
  final monthFormat = DateFormat('MMM');

  static const weeks = 6;

  final GlobalKey _keyFAB = GlobalKey();

  @override
  Widget build(BuildContext context) {
    return UiScreen(
      body: _buildCalendar(context),
      floatingActionButton: FloatingActionButton(
        key: _keyFAB,
        onPressed: () => log.finer('aaa'),
        tooltip: 'Add',
        child: const Icon(Icons.add),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }

  Widget _buildCalendar(BuildContext context) {
    DateTime now = DateTime.now();
    now = DateTime(now.year, now.month, now.day);
    DateTime firstDay = now.subtract(Duration(days: now.weekday - 1 + 7*(weeks-1)));

    final theme = Theme.of(context);
    var font = theme.textTheme.subtitle1;
    double height = 2 + [font?.fontSize, font?.height].fold(1, (double p, double? e) => e == null ? p : p*e);
    // developer.log("Font2 ${font?.fontSize}  ${font?.height}, size: ${mul(1, [font?.fontSize, font?.height]).round()}");

    bool isLastWeek(DateTime dayTime) => dayTime.month < dayTime.add(Duration(days: 7)).month;
    bool isLastMonthDay(DateTime dayTime) => dayTime.month < dayTime.add(Duration(days: 1)).month;

    return Stack(children: [Column(
      children: [
        // Header
        Row(children: [
          _buildItem(context, 0, height, underline: true, vertline: true),
          _buildItem(context, 1, height, text: "#", underline: true, vertline: true),
          // for (var day in weekDays) _buildItem(context, size, text: day),
          for (int day = 0; day < weekDays.length; day++) _buildItem(context, day, height, text: weekDays[day], underline: true),
        ],),
        // Dates
        for (var week = 0, weekStart = firstDay.add(Duration(days: week*7)); week < weeks; week++, weekStart = firstDay.add(Duration(days: week*7)))
          Row(children: [
            _buildItem(context, 0, height, underline: isLastWeek(weekStart), vertline: true),
            _buildItem(context, 1, height, text: (weekStart.difference(DateTime(weekStart.year, 1, 1, 0, 0)).inDays / 7).ceil().toString(), underline: isLastWeek(weekStart), vertline: true),
            for (var day = 0, dayTime = weekStart.add(Duration(days: day)); day < 7; day++, dayTime = weekStart.add(Duration(days: day)))
              if (!dayTime.isAfter(now)) _buildItem(context, day, height, text: dayTime.day.toString(), underline: isLastWeek(dayTime), vertline: day < 6 && isLastMonthDay(dayTime))
              else _buildItem(context, day, height, noDecoration: true)
          ],)
      ],),
      ..._buildMonthNames(context, firstDay, now, height),
    ],);
  }

  List<Widget> _buildMonthNames(BuildContext context, DateTime firstDay, DateTime now, double height) {
    double screenWidth = MediaQuery.of(context).size.width;
    double width = screenWidth / (7 + 2);
    var monthNames = <Widget>[];
    int yearMonth(DateTime date) => date.year*12 + date.month;
    for (int ym = yearMonth(firstDay), index = 0; ym <= yearMonth(now); ym++, index++) {
      DateTime firstMonthDay = DateTime(ym ~/ 12, ym % 12);
      DateTime lastMonthDay = DateTime(ym ~/ 12, ym % 12 + 1, 0);
      int firstWeek = max(0, (firstMonthDay.difference(firstDay).inDays / 7).ceil());
      int lastWeek = min(weeks-1, (lastMonthDay.difference(firstDay).inDays / 7).truncate());
      bool last = ym == yearMonth(now);
      // log.finer('First day: $firstMonthDay');
      // log.finer('Last day: $lastMonthDay');
      log.finer('${monthFormat.format(firstMonthDay)}, firstWeek: $firstWeek, lastWeek: $lastWeek');
      monthNames.add(Positioned(
        top: height * (firstWeek + 1),
        left: 0,
        child: Container(
          width: width - 2,
          height: height * (lastWeek - firstWeek + 1) - (last ? 0 : 2),
          alignment: Alignment.center,
          child: RotatedBox(quarterTurns: -1, child: Text(monthFormat.format(firstMonthDay), textAlign: TextAlign.center,softWrap: true, overflow: TextOverflow.clip,),),
          decoration: index % 2 == 1 ? null : BoxDecoration(color: Colors.grey.shade500.withAlpha(50)),
        ),
      ));
    }
    return monthNames;
  }

  Widget _buildItem(BuildContext context, int index, double height, {String? text, bool train=false, bool underline=false, bool vertline=false, bool noDecoration=false}) {
    final theme = Theme.of(context);

    return Expanded(child: Stack(children: [
      Container(
        alignment: Alignment.center,
        child: text != null ? Text(text/*, textAlign: TextAlign.center,*/) : null,
        // width: 20,
        constraints: BoxConstraints(
          minHeight: height,
          // minWidth: size + 4, // + padding * 2
          // maxWidth: size * 2,
        ),
        decoration: index % 2 == 0 || noDecoration ? null : BoxDecoration(color: Colors.grey.shade500.withAlpha(50)),
        // padding: EdgeInsets.all(2),
        // margin: EdgeInsets.all(2),
      ),
      if (train) Positioned(bottom: 0,
        child: Icon(Icons.circle, size: 7),
      ),
      if (underline) Positioned(
          bottom: 0,
          child: Container(
            color: theme.colorScheme.onSurface,
            height: 2,
            width: 200,
          )),
      if (vertline) Positioned(
          right: 0,
          child: Container(
            color: theme.colorScheme.onSurface,
            height: 200,
            width: 2,
          )),
    ],));
  }
}
