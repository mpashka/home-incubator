import 'package:flutter/material.dart';
import 'package:flutter_fe/misc/configuration.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';

import '../blocs/bloc_provider.dart';
import 'drawer.dart';

class UiScreen extends StatelessWidget {
  final Widget body;
  final Widget? floatingActionButton;
  final PreferredSizeWidget? appBarBottom;

  const UiScreen({required this.body, this.floatingActionButton, this.appBarBottom, Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    Configuration configuration = Injector().get<Configuration>();

    return Scaffold(
        appBar: AppBar(
          title: Text(configuration.uiTitle),
          bottom: appBarBottom,
        ),
        drawer: MyDrawer(),
        body: body,
        floatingActionButton: floatingActionButton,
    );
  }
}
