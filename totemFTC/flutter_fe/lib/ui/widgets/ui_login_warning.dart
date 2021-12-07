import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/misc/configuration.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';

class UiLoginWarning {
  LoginProvider loginProvider;
  ConfigurationLoginProvider loginProviderConfig;

  UiLoginWarning(this.loginProvider, this.loginProviderConfig);

  Future<bool> checkLogin(BuildContext context) async {
    if (loginProviderConfig.error != null) {
      showDialog(context: context, builder: (ctx) => AlertDialog(
          title: Row(children: [
            Icon(Icons.error, color: Colors.red),
            Flexible(child: Text('Вход через ${loginProvider.name} невозможен'),),
          ],),
        content: Text(loginProviderConfig.error!),
        actions: [TextButton(onPressed: () => Navigator.of(context).pop(), child: Text('Ok'))],
      ));
      return false;
    }
    if (loginProviderConfig.warning != null) {
      final result = await showDialog(context: context, builder: (ctx) => AlertDialog(
        title: Row(children: [
          Icon(Icons.warning, color: Colors.yellow),
          Flexible(child: Text('Необходима дополнительная настройка для входа через ${loginProvider.name}'),),
        ],),
        content: Text(loginProviderConfig.warning!),
        actions: [
          TextButton(onPressed: () => Navigator.of(context).pop(true), child: Text('Ok')),
          TextButton(onPressed: () => Navigator.of(context).pop(), child: Text('Cancel')),
        ],
      ));
      return result == true;
    }
    return true;
  }
}