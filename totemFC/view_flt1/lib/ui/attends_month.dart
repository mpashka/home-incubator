import 'dart:html';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'dart:math';

import 'widgets/ui_attend.dart';
import 'dart:developer' as developer;

class AttendsMonthScreen extends StatelessWidget {

  Random _random = Random();

  @override
  Widget build(BuildContext context) {
    return
      Column(
        children: [
          Container(
              height: 200,
              child: ListView(
                scrollDirection: Axis.vertical,
                children: [
                  ..._buildMonth(context, "Август", 3, 1, 32),
                ],
              )
          ),

          // _buildRow(["Пн","Вт","Ср","Чт","Пт","Сб","Вс",]),
          // ..._buildMonth("Август", 3, 1, 32),
          UiAttend(name: 'Кроссфит', date: DateTime.now().subtract(Duration(days: 3, minutes: 10)), marked: true),
        ],
      );
  }

  List<Widget> _buildMonth(BuildContext context, String name, int skip, int begin, int end) {
    return [
      Container(
          width: double.infinity,
          color: Colors.grey.shade300,
          child: Center(child: Text(name))
      ),
      _buildRow(context, ["Пн","Вт","Ср","Чт","Пт","Сб","Вс",]),
      ..._buildCalendar(context, skip, begin, end),
/*
      Container(
          height: 100,
          child: ListView(
            scrollDirection: Axis.vertical,
            children: [
              ..._buildCalendar(skip, begin, end)
            ],
          )
      )
*/
    ];
  }

  List<Widget> _buildCalendar(BuildContext context, int skip, int begin, int end) {
    int rows = ((skip + end - begin) / 7.0).ceil();
    List<Widget> result = List.filled(rows, Text(""), growable: false);
    int mBegin = begin - skip;
    for (int row = 0; row < rows; row++) {
      result[row] = _buildNumberRow(context, mBegin, min(end, mBegin + 7));
      mBegin += 7;
    }
    return result;
  }

  Widget _buildNumberRow(BuildContext context, int begin, int end) {
    List<Widget> result = List.filled(7, Expanded(child: Text(""),), growable: false);
    for (var i = begin; i < end; i++) {
      if (i > 0) {
        result[i - begin] = _buildItem(context, i.toString(), _random.nextInt(100) < 30);
      }
    }
    return Row(children:result);
  }

  Widget _buildRow(BuildContext context, List<String> text) {
    List<Widget> result = List.filled(text.length, Text(""), growable: false);
    for (int i = 0; i < text.length; i++) {
      result[i] = _buildItem(context, text[i], false);
    }
    return Row(
      children: result,
      crossAxisAlignment: CrossAxisAlignment.start,
    );
  }

/*
dot
  Widget _buildItem(String text, bool train) {
    return Expanded(
        child:
        //Container(color: Colors.redAccent, child:
        Column(children: [
          Text(text),
          if (train) Icon(Icons.circle, size: 7),
        ])
      // Text(text)
    );
  }
*/

  Widget _buildItem(BuildContext context, String text, bool train) {
    final theme = Theme.of(context);
    var font = theme.textTheme.subtitle1;
    int size = mul(1, [font?.fontSize, font?.height]).round();
    // developer.log("Font2 ${font?.fontSize}  ${font?.height}, size: ${mul(1, [font?.fontSize, font?.height]).round()}");

    BoxDecoration? decoration;
    if (train) {
      decoration = BoxDecoration(
        color: Colors.blue,
        borderRadius: BorderRadius.circular(8),
      );
    }

    return Expanded(
        child: Center(
            child: Container(
              alignment: AlignmentDirectional.center,
              child: Text(text),
              // width: 20,
              constraints: BoxConstraints(
                minWidth: size + 4, // + padding * 2
                maxWidth: size * 2,
              ),
              decoration: decoration,
              padding: EdgeInsets.all(2),
              margin: EdgeInsets.all(2),
            )
        )
    );
  }

  double mul(double v1, List<double?> v2) {
    double result = v1;
    for (var value in v2) {
      if (value != null) {
        result *= value;
      }
    }
    return result;
  }
}
