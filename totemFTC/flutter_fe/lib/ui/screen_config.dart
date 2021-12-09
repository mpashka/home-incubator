import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/painting.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_user.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/misc/configuration.dart';
import 'package:flutter_fe/misc/initializer.dart';
import 'package:logging/logging.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';

import 'screen_base.dart';
import 'widgets/ui_divider.dart';
import 'widgets/ui_login_warning.dart';

class ScreenConfig extends StatefulWidget {
  static const routeName = '/config';

  @override
  State createState() => ScreenConfigState();
}

class ScreenConfigState extends BlocProvider<ScreenConfig> {

  late final Session session;
  late final CrudUserBloc crudUserBloc;
  late final TextEditingController firstNameController;
  late final TextEditingController lastNameController;
  late final TextEditingController nickNameController;
  bool isNameModified = false;
  late final StreamController<bool> nameModifiedController;
  late final Sink<bool> nameModifiedIn;
  late final Stream<bool> nameModifiedOut;

  @override
  void initState() {
    super.initState();
    session = Injector().get<Session>();
    crudUserBloc = CrudUserBloc(provider: this);
    final user = crudUserBloc.state;
    for (var provider in loginProviders) {
      bool userNetworkFound = user.socialNetworks?.any((s) => s.networkName == provider.name) ?? false;
      SessionBloc(state: LoginStateInfo(userNetworkFound ? LoginState.done : LoginState.none), provider:this, name: 'sessionBloc_${provider.name}');
    }

    firstNameController = TextEditingController();
    lastNameController = TextEditingController();
    nickNameController = TextEditingController();
    nameModifiedController = StreamController<bool>();
    nameModifiedIn = nameModifiedController.sink;
    nameModifiedOut = nameModifiedController.stream.asBroadcastStream();
    _resetName(user);
    firstNameController.addListener(checkNameModified);
    lastNameController.addListener(checkNameModified);
    nickNameController.addListener(checkNameModified);
  }

  @override
  void dispose() {
    super.dispose();
    nameModifiedIn.close();
    nameModifiedController.close();
  }

  @override
  Widget build(BuildContext context) {
    _resetName(crudUserBloc.state);
    return UiScreen(body: BlocProvider.streamBuilder<CrudEntityUser, CrudUserBloc>(builder: (ctx, user) {
      return SingleChildScrollView(child: Column(children: [
        ..._renderName(user),
        UiDivider('Фото'),
        Text('To be done'),
        UiDivider('Социальные сети'),
        ..._renderSocialNetworks(),
        UiDivider('e-mail'),
        if (user.emails != null) for (var email in user.emails!) Row(children: [
          Text(email.email),
          if (email.confirmed) Icon(
              Icons.verified_user_outlined, color: Colors.green)
        ],),
        UiDivider(null),
        Container(
            padding: EdgeInsets.only(left: 3, right: 3),
            alignment: Alignment.centerRight,
            // todo add logout indicator
            child: ElevatedButton(
              child: Text('Выход'),
              onPressed: () => logout(context),
            )
        ),
      ],),);
    },));
  }

  List<Widget> _renderName(CrudEntityUser user) {
    return [
      UiDivider('Имя'),
      TextField(controller: firstNameController, decoration: InputDecoration(labelText: 'Имя', hintText: 'Введите свое имя'),),
      TextField(controller: lastNameController, decoration: InputDecoration(labelText: 'Фамилия', hintText: 'Введите свою фамилию',),),
      TextField(controller: nickNameController, decoration: InputDecoration(labelText: 'Ник', hintText: 'Введите отображаемое имя',),),
      Container(
          alignment: Alignment.centerRight,
          padding: EdgeInsets.only(top: 3, left: 3, right: 3),
          child: StreamBuilder<bool>(initialData: isNameModified, stream: nameModifiedOut, builder: (ctx, modified) => Row(mainAxisAlignment: MainAxisAlignment.end, children: [
            ElevatedButton(
              child: Text('Ok'),
              onPressed: modified.requireData ? () => _updateName(user) : null,
            ),
            ElevatedButton(
              child: Text('Reset'),
              onPressed: modified.requireData ? () => _resetName(user) : null,
            ),
          ],),),
      ),
    ];
  }

  void checkNameModified() {
    CrudEntityUser user = crudUserBloc.state;

    bool same(String text, String? value) => value != null ? text == value : text.isEmpty;
    bool modified = !same(firstNameController.text.trim(), user.firstName?.trim()) ||
        !same(lastNameController.text.trim(), user.lastName?.trim()) ||
        !same(nickNameController.text.trim(), user.nickName?.trim());

    if (modified != isNameModified) {
      isNameModified = modified;
      nameModifiedIn.add(modified);
    }
  }

  void _resetName(CrudEntityUser user) {
    firstNameController.text = user.firstName?.trim() ?? '';
    lastNameController.text = user.lastName?.trim() ?? '';
    nickNameController.text = user.nickName?.trim() ?? '';
  }

  void _updateName(CrudEntityUser user) {
    String? name(String text) => text.isNotEmpty ? text : null;
    user.firstName = name(firstNameController.text.trim());
    user.lastName = name(lastNameController.text.trim());
    user.nickName = name(nickNameController.text.trim());
    crudUserBloc.updateUser(user);
  }

  List<Widget> _renderSocialNetworks() {
    final user = crudUserBloc.state;
    List<Widget> result = [];
    final configuration = Injector().get<Configuration>();
    for (var provider in loginProviders) {
      var loginProviderConfig = configuration.loginProviderConfig(provider);
      final blocName = 'sessionBloc_${provider.name}';
      final sessionBloc = getBloc<SessionBloc>(blocName);
      result.add(BlocProvider.streamBuilder<LoginStateInfo, SessionBloc>(
          blocName: blocName,
          builder: (ctx, state) {

            var networkIter = user.socialNetworks?.where((s) => s.networkName == provider.name).iterator;
            CrudEntityUserSocialNetwork? userNetwork = networkIter?.moveNext() == true ? networkIter?.current : null;
            final text = userNetwork?.displayName != null ? userNetwork!.displayName! : provider.name;
            final uiWidgets = <Widget>[
              provider.icon,
              Text(text),
            ];
            GestureTapCallback? tapAction;
            switch (state.state) {
              case LoginState.none:
                tapAction = () async {
                  if (await UiLoginWarning(provider, loginProviderConfig).checkLogin(context)) {
                    crudUserBloc.link(provider, sessionBloc: sessionBloc);
                  }
                };
                break;
              case LoginState.error:
                uiWidgets.add(IconButton(
                    icon: Icon(Icons.warning, color: Colors.yellow),
                    onPressed: () => showDialog(context: context, builder: (ctx) => AlertDialog(
                      title: Text('Link ${provider.name} error'),
                      content: SingleChildScrollView(child: Text(state.description!)),
                    ))));
                break;
              case LoginState.inProgress:
                uiWidgets.add(CircularProgressIndicator());
                break;
              case LoginState.done:
                if (userNetwork != null) {
                  if (userNetwork.link != null) {
                    tapAction = () => launch(userNetwork.link!);
                  }
                  uiWidgets.add(IconButton(
                    icon: Icon(Icons.clear, color: Colors.red),
                    onPressed: () {
                      sessionBloc.state = LoginStateInfo(LoginState.inProgress);
                      crudUserBloc.unlink(userNetwork);
                      sessionBloc.state = LoginStateInfo(LoginState.none);
                    },
                  ));
                }
                break;
            }
            final row = Row(children: uiWidgets,);
            return tapAction != null ? GestureDetector(child: row, onTap: tapAction,) : row;
          }
      ));
    }
    return result;
  }

  void logout(BuildContext context) async {
    bool? logout = await showDialog<bool>(context: context, builder: (ctx) => AlertDialog(
      title: Text('Выйти'),
      elevation: 8,
      actions: [
        TextButton(onPressed: () => Navigator.pop(context, true), child: Text('Да')),
        TextButton(onPressed: () => Navigator.pop(context, false), child: Text('Нет')),
      ],
    ));
    if (logout == true) {
      session.logout(context);
    }
  }
}
