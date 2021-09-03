import 'package:flutter/material.dart';
import 'package:view_flt1/ui/widgets/person.dart';

import 'drawer.dart';
import 'widgets/subscription.dart';
import 'widgets/ui_attend.dart';

class MasterPeopleScreen extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Totem FC'),
      ),
      drawer: MyDrawer(),
      body: peopleList(),
      floatingActionButton: FloatingActionButton(
        onPressed: () => {},//_showAddMenu(context),
        tooltip: 'Add',
        child: Icon(Icons.add),
      ),
    );
  }

  Widget peopleList() {
    return Column(
      children: [
        UiPerson(name: 'Павел М.'),
        UiPerson(name: 'Рома Р.'),

        Divider(
          height: 20,
          thickness: 5,
          indent: 20,
          endIndent: 20,
        ),

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

        UiAttend(name: '', date: DateTime.now().subtract(Duration(days: 3)), marked: true),
        UiAttend(name: '', date: DateTime.now().subtract(Duration(days: 2, hours: 2)), marked: true),

      ],
    );
  }
}
