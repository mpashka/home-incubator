import 'package:flutter/material.dart';
import 'package:flutter/painting.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_user.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:logging/logging.dart';
import 'package:url_launcher/url_launcher.dart';

import 'screen_base.dart';
import 'widgets/ui_divider.dart';

class ScreenConfig extends StatefulWidget {
  @override
  State createState() => ScreenConfigState();
}

class ScreenConfigState extends State<ScreenConfig> {
  static final Logger log = Logger('ScreenConfig');

  late final FocusNode firstNameFocusNode;
  late final FocusNode lastNameFocusNode;
  late final FocusNode nickNameFocusNode;
  late final TextEditingController firstNameController;
  late final TextEditingController lastNameController;
  late final TextEditingController nickNameController;

  @override
  void initState() {
    super.initState();
    firstNameFocusNode = FocusNode();
    lastNameFocusNode = FocusNode();
    nickNameFocusNode = FocusNode();
    firstNameController = TextEditingController();
    lastNameController = TextEditingController();
    nickNameController = TextEditingController();
  }

  @override
  void dispose() {
    firstNameFocusNode.dispose();
    lastNameFocusNode.dispose();
    nickNameFocusNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    late final CrudUserBloc crudUserBloc;
    return BlocProvider(
      init: (blocProvider) {
        crudUserBloc = blocProvider.addBloc(bloc: CrudUserBloc());
        final user = crudUserBloc.state;
        for (var provider in loginProviders) {
          var userNetwork = user.socialNetworks?.firstWhere((s) => s.networkName == provider.name);
          blocProvider.addBloc(bloc: SessionBloc(userNetwork == null ? LoginState.none : LoginState.done), name: 'sessionBloc_${provider.name}');
        }
      },
      child: UiScreen(body: _renderConfig(crudUserBloc)
        ,),);
  }

  Widget _renderConfig(CrudUserBloc crudUserBloc) {
    return BlocProvider.streamBuilder<CrudEntityUser, CrudUserBloc>(builder: (ctx, user) {
      return Column(children: [
        ..._renderName(crudUserBloc, user),
        UiDivider('Фото'),
        Text('To be done'),
        UiDivider('Социальные сети'),
        ..._renderSocialNetworks(ctx, crudUserBloc),
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
            child: ElevatedButton(
              child: Text('Выход'),
              onPressed: () => {},
            )
        ),
      ],);
    },);
  }

  List<Widget> _renderName(CrudUserBloc crudUserBloc, CrudEntityUser user) {
    firstNameController.text = user.firstName ?? '';
    lastNameController.text = user.lastName ?? '';
    nickNameController.text = user.nickName ?? '';

    return [
      UiDivider('Имя'),
      Container(
        padding: EdgeInsets.only(left: 3, right: 3),
        child: GestureDetector(
            onTap: () => firstNameFocusNode.requestFocus(),
            child: Column(children: [
              Text('Имя'),
              TextField(
                focusNode: firstNameFocusNode,
                controller: firstNameController,
              ),
            ],)),),
      Container(
        padding: EdgeInsets.only(left: 3, right: 3),
        child: GestureDetector(
            onTap: () => lastNameFocusNode.requestFocus(),
            child: Column(children: [
              Text('Фамилия'),
              TextField(
                focusNode: lastNameFocusNode,
                controller: lastNameController,
              ),
            ],)),),
      Container(
        padding: EdgeInsets.only(left: 3, right: 3),
        child: GestureDetector(
            onTap: () => nickNameFocusNode.requestFocus(),
            child: Column(children: [
              Text('Отображаемое имя'),
              TextField(
                focusNode: nickNameFocusNode,
                controller: nickNameController,
              ),
            ],)),),
      Container(
          padding: EdgeInsets.only(left: 3, right: 3),
          alignment: Alignment.centerRight,
          child: ElevatedButton(
            child: Text('Ok'),
            onPressed: () => _updateName(crudUserBloc, user),
          )
      ),
    ];
  }

  List<Widget> _renderSocialNetworks(BuildContext context, CrudUserBloc crudUserBloc) {
    final user = crudUserBloc.state;
    List<Widget> result = [];
    for (var provider in loginProviders) {
      final blocName = 'sessionBloc_${provider.name}';
      final sessionBloc = BlocProvider.getBloc<SessionBloc>(context, name: blocName);
      result.add(BlocProvider.streamBuilder<LoginStateInfo, SessionBloc>(
          name: blocName,
          builder: (ctx, state) {
            CrudEntityUserSocialNetwork? userNetwork = user.socialNetworks?.firstWhere((s) => s.networkName == provider.name);
            final text = userNetwork?.displayName != null ? userNetwork!.displayName! : provider.name;
            final uiWidgets = <Widget>[
              provider.icon,
              Text(text),
            ];
            GestureTapCallback? tapAction;
            switch (state.state) {
              case LoginState.none:
                tapAction = () => crudUserBloc.link(provider, sessionBloc: sessionBloc);
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

  void _updateName(CrudUserBloc crudUserBloc, CrudEntityUser user) {
    user.firstName = firstNameController.text.isNotEmpty ? firstNameController.text : null;
    user.lastName = lastNameController.text.isNotEmpty ? lastNameController.text : null;
    user.nickName = nickNameController.text.isNotEmpty ? nickNameController.text : null;
    crudUserBloc.updateUser(user);
  }
}
