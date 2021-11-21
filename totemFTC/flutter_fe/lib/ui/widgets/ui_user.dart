import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/crud_user.dart';

@immutable
class UiUser extends StatelessWidget {

  final CrudEntityUser _user;

  UiUser(this._user);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
        color: theme.colorScheme.secondary,
        child: ListTile(
            leading: Icon(Icons.baby_changing_station_rounded),
            title: Text(_user.displayName),
        )
    );
  }
}
