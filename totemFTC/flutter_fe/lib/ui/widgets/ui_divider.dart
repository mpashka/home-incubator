import 'package:flutter/material.dart';

@immutable
class UiDivider extends StatelessWidget {

  final String? _name;

  const UiDivider(this._name, {Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Row(children: [
      const Expanded(child: Divider(thickness: 3,)),
      if (_name != null) Container(
        padding: EdgeInsets.symmetric(horizontal: 8),
        child: Text(_name!),
      )
    ]);
  }
}
