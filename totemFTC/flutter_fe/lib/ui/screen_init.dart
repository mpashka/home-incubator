import 'package:flutter/material.dart';
import 'package:flutter_fe/misc/initializer.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';

import '../misc/configuration.dart';

class InitScreen extends StatelessWidget {
  final Initializer _initializer;
  const InitScreen(this._initializer, {Key? key}) : super(key: key);


  @override
  Widget build(BuildContext context) {
    _initializer.future.then((value) => Navigator.pushReplacementNamed(context, '/login'));

    return Scaffold(
        appBar: AppBar(title: const Text('Totem FC'),),
        body: Column(children: const [
          CircularProgressIndicator(),
          Text('Application initializing')
        ])
    );
  }
}
