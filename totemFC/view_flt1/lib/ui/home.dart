import 'package:flutter/material.dart';
import 'package:view_flt1/ui/widgets/expandable_fab.dart';
import 'package:view_flt1/ui/widgets/subsription.dart';
import 'package:view_flt1/ui/widgets/ui_attend.dart';
import 'dart:developer' as developer;

class Home extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          UiSubscription(name: 'Кроссфит',
            start: DateTime.now().subtract(Duration(days: 1, minutes: 10)),
            count: 10, used: 3
          ),

          UiSubscription(name: 'Растяжка',
            start: DateTime.now().subtract(Duration(days: 1, minutes: 10)),
            count: 4, used: 2
          ),

          Divider(
            height: 20,
            thickness: 5,
            indent: 20,
            endIndent: 20,
          ),

          UiAttend(name: 'Кроссфит', date: DateTime.now().subtract(Duration(days: 3, minutes: 10)), marked: true),
          UiAttend(name: 'Кроссфит', date: DateTime.now().subtract(Duration(days: 2, minutes: 10)), marked: true),
          UiAttend(name: 'Растяжка', date: DateTime.now().subtract(Duration(days: 2, minutes: 10)), marked: true),
          UiAttend(name: 'Кроссфит', date: DateTime.now().add(Duration(days: 2, minutes: 10)), marked: false),
        ],
      ),
/*
      floatingActionButton: FloatingActionButton(
        // onPressed: _incrementCounter,
        tooltip: 'Add',
        child: Icon(Icons.add),
      ), // This trailing comma makes auto-formatting nicer for build methods.
*/
      floatingActionButton: ExpandableFab(
        distance: 112.0,
        children: [
          ActionButton(
            onPressed: () => _showAction(context, 0),
            icon: const Icon(Icons.format_size),
          ),
          ActionButton(
            onPressed: () => _showAction(context, 1),
            icon: const Icon(Icons.insert_photo),
          ),
          ActionButton(
            onPressed: () => _showAction(context, 2),
            icon: const Icon(Icons.videocam),
          ),
        ],
      ),
    );
  }

  _showAction(BuildContext context, int i) {
    developer.log('Action $i');
  }
}
