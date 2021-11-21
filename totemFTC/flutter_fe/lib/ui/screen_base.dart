import 'package:flutter/material.dart';

import '../blocs/bloc_provider.dart';
import 'drawer.dart';

class UiScreen extends StatelessWidget {
  final Widget body;
  final Widget? floatingActionButton;

  const UiScreen({required this.body, this.floatingActionButton, Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text('Totem FC'),
        ),
        drawer: MyDrawer(),
        body: body,
        floatingActionButton: floatingActionButton,
    );
  }
}
