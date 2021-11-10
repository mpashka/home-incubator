import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import 'drawer.dart';
import 'widgets/scroll_list_selector.dart';

class PurchasesScreen extends StatelessWidget {
  static final format = DateFormat('yyyy-MM-dd kk:mm');

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Totem FC'),
      ),
      drawer: MyDrawer(),
      body: purchasesList(),
      floatingActionButton: FloatingActionButton(
        onPressed: () => _showAddMenu(context),
        tooltip: 'Add',
        child: Icon(Icons.add),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }

  Widget purchasesList() {
    return Column(
      children: [
        ListTile(
            leading: Icon(Icons.account_tree_outlined),
            title: Text('Кофе - капучино'),
            subtitle: Text('Покупка ${format.format(DateTime.now().subtract(Duration(days: 1, minutes: 10)))}')
        ),
        ListTile(
            leading: Icon(Icons.ac_unit_sharp),
            title: Text('Энергетик - арбуз'),
            subtitle: Text('Покупка ${format.format(DateTime.now().subtract(Duration(days: 1, minutes: 5)))}')
        ),
      ],
    );
  }

  void _showAddMenu(BuildContext context) {
    showDialog(context: context,
        builder: (BuildContext c) {
          return SimpleDialog(
              title: Text('Еда'),
              elevation: 5,
              children: [
                Container(
                  height: 300,
                  child:
                  Row(
                    children: [
/*
                      Flexible(child: WheelListSelector(items: [
                        'Энергетик',
                        'Батончик',
                        'Кофе',
                      ],)),
                      Flexible(child: WheelListSelector(items: [
                        'Банан',
                        'Яблоко',
                        'Апельсин',
                        'Груша',
                      ],)),
*/
                    ],
                  ),
                )
              ]
          );
        });
  }
}