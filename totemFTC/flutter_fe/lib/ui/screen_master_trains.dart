import 'package:flutter/material.dart';

import 'drawer.dart';
import 'widgets/ui_attend.dart';
import 'widgets/ui_workout.dart';

class MasterTrainsScreen extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Totem FC'),
      ),
      drawer: MyDrawer(),
      body: trainsList(),
      floatingActionButton: FloatingActionButton(
        onPressed: () => {},//_showAddMenu(context),
        tooltip: 'Add',
        child: Icon(Icons.add),
      ),
    );
  }

  Widget trainsList() {
    return Column(
      children: [
        UiWorkout(name: 'Индивидуальная', date: _minus(3),),
        UiWorkout(name: 'Кроссфит групповая', date: _minus(2),),
        UiWorkout(name: 'Кроссфит групповая', date: _minus(1),),
        Divider(
          height: 20,
          thickness: 5,
          indent: 20,
          endIndent: 20,
        ),
        // todo
        // UiAttend(name: 'Павел М.', marked: true),
        // UiAttend(name: 'Рома Р.', marked: false)
      ],
    );
  }

  DateTime _minus(int hour) {
    var sub = DateTime.now()
        .subtract(Duration(hours: hour));
    return DateTime(sub.year, sub.month, sub.day, sub.hour);
  }
}
