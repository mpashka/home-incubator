import 'package:flutter/material.dart';
import 'package:logging/logging.dart';

import 'attends_month.dart';
import 'attends_week.dart';
import 'drawer.dart';
import 'screen_base.dart';

class ScreenTrainings extends StatelessWidget {

  static final Logger log = Logger('ScreenTrainings');

  final GlobalKey _keyFAB = GlobalKey();

  @override
  Widget build(BuildContext context) {
    return UiScreen(
      body: Column(children: [

      ],),
      floatingActionButton: FloatingActionButton(
        key: _keyFAB,
        onPressed: () => log.finer('aaa'),
        tooltip: 'Add',
        child: const Icon(Icons.add),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }

  Widget _buildCalendar(BuildContext context) {
    return null;
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
