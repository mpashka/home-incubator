import 'dart:developer' as developer;

import 'package:flutter/material.dart';

import 'drawer.dart';
import 'widgets/scroll_list_selector.dart';
import 'widgets/ui_subscription.dart';
import 'widgets/ui_attend.dart';

class AboutScreen extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Totem FC'),
      ),
      drawer: MyDrawer(),
      body: Text('About'),
    );
  }
}
