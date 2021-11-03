import 'package:flutter/material.dart';

import 'drawer.dart';
import 'widgets/ui_subscription.dart';

class SubscriptionsScreen extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Totem FC'),
      ),
      drawer: MyDrawer(),
      body: subscriptionsList(),
      floatingActionButton: FloatingActionButton(
        onPressed: () => {},//_showAddMenu(context),
        tooltip: 'Add',
        child: Icon(Icons.add),
      ),
    );
  }

  Widget subscriptionsList() {
    return Column(
        children: [
/*
          UiSubscription(name: 'Кроссфит',
              start: DateTime.now().subtract(Duration(days: 1, minutes: 10)),
              count: 10, used: 3
          ),

          UiSubscription(name: 'Растяжка',
              start: DateTime.now().subtract(Duration(days: 1, minutes: 10)),
              count: 4, used: 2
          ),
*/

          Divider(
            height: 20,
            thickness: 5,
            indent: 20,
            endIndent: 20,
          ),
        ]
    );
  }
}