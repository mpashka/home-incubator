import 'package:flutter/material.dart';
import 'package:view_flt1/ui/widgets/fab_multi.dart';
import 'package:view_flt1/ui/widgets/subscription.dart';
import 'package:view_flt1/ui/widgets/ui_attend.dart';
import 'dart:developer' as developer;
import 'package:flutter_picker/flutter_picker.dart';

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

          Divider(
            height: 20,
            thickness: 2,
            indent: 20,
            endIndent: 20,
          ),

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
      floatingActionButton: FabMulti(
        distance: 112.0,
        children: [
          RoundedButton(
            onPressed: () => _showAction(context, 0),
            text: 'Записаться',
          ),
          RoundedButton(
            onPressed: () => _showAction(context, 1),
            text: 'Отметиться',
          ),
          RoundedButton(
            onPressed: () => _showAction(context, 1),
            text: 'Купить еду',
          ),
        ],
      ),
    );
  }

  _showAction(BuildContext context, int i) {
    if (i == 0) {
      _showAddTrain(context);
    }
  }

  _showAddTrain(BuildContext context) async {
    var result = await showDialog(context: context,
        builder: (BuildContext c) {
          return SimpleDialog(
            title: Text('Выберите тренировку'),
          );
        });
    developer.log("Dialog result: $result");
  }


  _showAddTrainP(BuildContext context) {
    new Picker(

        adapter: PickerDataAdapter(data: [
          new PickerItem(text: Text('Кроссфит'), value: Icons.add, children: [
            new PickerItem(text: Icon(Icons.more)),
            new PickerItem(text: Icon(Icons.aspect_ratio)),
            new PickerItem(text: Icon(Icons.android)),
            new PickerItem(text: Icon(Icons.menu)),
          ]),
          new PickerItem(text: Text('Растяжка'), value: Icons.title, children: [
            new PickerItem(text: Icon(Icons.more_vert)),
            new PickerItem(text: Icon(Icons.ac_unit)),
            new PickerItem(text: Icon(Icons.access_alarm)),
            new PickerItem(text: Icon(Icons.account_balance)),
          ]),
          new PickerItem(text: Icon(Icons.face), value: Icons.face, children: [
            new PickerItem(text: Icon(Icons.add_circle_outline)),
            new PickerItem(text: Icon(Icons.add_a_photo)),
            new PickerItem(text: Icon(Icons.access_time)),
            new PickerItem(text: Icon(Icons.adjust)),
          ]),
          new PickerItem(text: Icon(Icons.linear_scale), value: Icons.linear_scale, children: [
            new PickerItem(text: Icon(Icons.assistant_photo)),
            new PickerItem(text: Icon(Icons.account_balance)),
            new PickerItem(text: Icon(Icons.airline_seat_legroom_extra)),
            new PickerItem(text: Icon(Icons.airport_shuttle)),
            new PickerItem(text: Icon(Icons.settings_bluetooth)),
          ]),
          new PickerItem(text: Icon(Icons.close), value: Icons.close),
        ]),
        title: new Text(""),
        onConfirm: (Picker picker, List value) {
          print(value.toString());
          print(picker.getSelectedValues());
        }
    ).showDialog(context);
        // .show(_scaffoldKey.currentState); Scaffold.of(context)
  }
}
