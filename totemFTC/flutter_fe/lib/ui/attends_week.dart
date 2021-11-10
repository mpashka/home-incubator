
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

import 'widgets/ui_attend.dart';

class AttendsWeekScreen extends StatelessWidget {

  List<String> days = [
    "Пн, 13",
    "Ср, 17",
    "Пт, 19",
    "Сб, 20",
  ];

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
                children:
                _buildDays(
                    5,
                    [
                      ...days,
                      ...days,
                      ...days,
                      ...days
                    ]
/*
                )
                for (String day in days)
                  _buildDay(day, false),
                _buildDay("Пн, 23", true),
                for (String day in days)
                  _buildDay(day, false),
              ],
*/
                )
            )
          // physics: const FixedExtentScrollPhysics(),
          // controller: FixedExtentScrollController(),
        ),
        // todo
        // UiAttend(name: 'Кроссфит', date: DateTime.now().subtract(Duration(days: 3, minutes: 10)), marked: true),
      ],
    );
  }

  List<Widget> _buildDays(int selectedIndex, List<String> days) {
    var result = List<Widget>.filled(days.length, Text(""), growable: false);
    var count = 0;
    for (var day in days) {
      result[count] = _buildDay(day, selectedIndex == count, count % 2 == 0);
      count++;
      // developer.log("Count: $count, %2: ${count % 2}, b: ${count % 2 == 0}");
    }
    return result;
  }

  Container _buildDay(String day, bool selected, bool odd) {
    return Container(
      decoration: BoxDecoration(
        color: selected ? Colors.blue : (odd ? Colors.grey.shade200 : null),
        borderRadius: BorderRadius.circular(12),
      ),
      margin: EdgeInsets.all(4),
      padding: EdgeInsets.all(4),
      child: Text(day),
    );
  }


}