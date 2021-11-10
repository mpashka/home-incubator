import 'package:flutter/material.dart';
import 'package:flutter_fe/misc/initializer.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';

import '../misc/configuration.dart';

class InitScreen extends StatefulWidget {
  const InitScreen({Key? key}) : super(key: key);

  @override
  State createState() => InitScreenState();
}

class InitScreenState extends State<InitScreen> {


  @override
  void initState() {
    super.initState();
    final initializer = Injector().get<Initializer>();
    initializer.future.then((value) => Navigator.pushReplacementNamed(context, '/login'));
  }


  @override
  void dispose() {
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(title: const Text('Totem FC'),),
        body: Column(children: const [
          CircularProgressIndicator(),
          Text('Application initializing')
        ])
    );
  }
}
