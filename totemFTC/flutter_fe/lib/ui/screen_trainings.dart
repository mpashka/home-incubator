import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:logging/logging.dart';

import 'screen_base.dart';

class ScreenTrainings extends StatelessWidget {

  static final Logger log = Logger('ScreenTrainings');

  static const List<String> weekDays = ["Пн","Вт","Ср","Чт","Пт","Сб","Вс",];

  static const weeks = 5;

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
    DateTime firstDay = now.subtract(Duration(days: now.weekday - 1 + 7*weeks));

    final theme = Theme.of(context);
    var font = theme.textTheme.subtitle1;
    double size = [font?.fontSize, font?.height].fold(1, (double p, double? e) => e == null ? p : p*e);
    // developer.log("Font2 ${font?.fontSize}  ${font?.height}, size: ${mul(1, [font?.fontSize, font?.height]).round()}");

    bool isLastWeekDay(DateTime dayTime) => dayTime.month < dayTime.add(Duration(days: 7)).month;
    bool isLastMonth(DateTime dayTime) => dayTime.month < dayTime.add(Duration(days: 1)).month;

    return Column(
      children: [
        Row(children: [
          _buildItem(context, 1, size, text: "#"),
          // for (var day in weekDays) _buildItem(context, size, text: day),
          for (int day = 0; day < weekDays.length; day++) _buildItem(context, day, size, text: weekDays[day]),
        ],),
        for (var week = 0, weekStart = firstDay.add(Duration(days: week*7)); week <= weeks; week++, weekStart = firstDay.add(Duration(days: week*7)))
          Row(children: [
            _buildItem(context, 1, size, text: (weekStart.difference(DateTime(weekStart.year, 1, 1, 0, 0)).inDays / 7).ceil().toString(), underline: isLastWeekDay(weekStart)),
            for (var day = 0, dayTime = weekStart.add(Duration(days: day)); day < 7; day++, dayTime = weekStart.add(Duration(days: day)))
              if (!dayTime.isAfter(now)) _buildItem(context, day, size, text: dayTime.day.toString(), underline: isLastWeekDay(dayTime), vertline: day < 6 && isLastMonth(dayTime))
              else _buildItem(context, day, size)
          ],)
      ],
    );
  }

  Widget _buildItem(BuildContext context, int index, double size, {String? text, bool? train, bool? underline, bool? vertline}) {
    final theme = Theme.of(context);

    BoxDecoration? decoration = index % 2 == 0 ? null : BoxDecoration(color: Colors.grey.shade500.withAlpha(50));

    return Expanded(child: text == null ?
    Container(
      constraints: BoxConstraints(
        minHeight: size,
        minWidth: size + 4, // + padding * 2
        maxWidth: size * 2,
      ),
    ) :
    Stack(children: [
      Container(
        alignment: Alignment.center,
        child: Text(text/*, textAlign: TextAlign.center,*/),
        // width: 20,
        constraints: BoxConstraints(
          minHeight: size,
          // minWidth: size + 4, // + padding * 2
          // maxWidth: size * 2,
        ),
        decoration: decoration,
        padding: EdgeInsets.all(2),
        // margin: EdgeInsets.all(2),
      ),
      if (train == true) Positioned(bottom: 0,
        child: Icon(Icons.circle, size: 7),
      ),
      if (underline == true) Positioned(
          bottom: 0,
          child: Container(
            color: theme.colorScheme.onSurface,
            height: 2,
            width: 200,
          )),
      if (vertline == true) Positioned(
          right: 0,
          child: Container(
            color: theme.colorScheme.onSurface,
            height: 200,
            width: 2,
          )),
    ],));
  }
}
