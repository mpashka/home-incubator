import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/blocs/crud_user.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:flutter_fe/ui/screen_base.dart';
import 'package:intl/intl.dart';

import 'ui_user.dart';
import 'wheel_list_selector.dart';


class UiSelectorUser {

  Widget buildPage(BuildContext context) {
    final TextEditingController searchTextController = TextEditingController();
    return _buildBloc(UiScreen(body: Column(

        https://docs.flutter.dev/cookbook/navigation/navigate-with-arguments

      children: _buildContent(context, searchTextController, (user) => Navigator.),
    ),), searchTextController);
  }

  Future<CrudEntityUser?> selectUserDialog(BuildContext context, String title) async {
    return await showDialog(context: context,
        builder: (ctx) => _buildDialog(context, title));
  }

  SimpleDialog _buildDialog(BuildContext context, String title) {
    final TextEditingController searchTextController = TextEditingController();
    // return _buildBloc(UiScreen(body: Column(

    return SimpleDialog(
        title: Text(title),
        elevation: 5,
        children: [
          ..._buildContent(context, searchTextController, (user) => Navigator.pop(context, user)),
          Row(children: [
            SimpleDialogOption(
              child: const Text('Cancel'),
              onPressed: () => Navigator.pop(context),
            ),
          ],)
        ]);
  }

  Widget _buildBloc(Widget body, TextEditingController searchTextController) {
    return BlocProvider(
        init: (blocProvider) {
          FilteredUsersBloc userBloc = blocProvider.addBloc(bloc: FilteredUsersBloc());
          searchTextController.addListener(() => userBloc.filter(searchTextController.text));
        },
        child: body);
  }

  List<Widget> _buildContent(BuildContext context, TextEditingController searchTextController, UserListener userListener) {
    return [
      Row(children: [
        Icon(Icons.search),
        Expanded(child: TextField(
          controller: searchTextController,
          autofocus: true,
        ),),
        GestureDetector(
          child: Icon(Icons.clear),
          onTap: () => searchTextController.text = '',
        ),
      ],),
      Flexible(
        child: BlocProvider.streamBuilder<List<CrudEntityUser>, FilteredUsersBloc>(builder: (users) => ListView(children: [
          for (var user in users) GestureDetector(
            child: UiUser(user),
            onTap: () => userListener(user),
          )
        ],),),),
    ];
  }
}

typedef UserListener = void Function(CrudEntityUser user);

class FilteredUsersBloc extends BlocBaseList<CrudEntityUser> {
  List<CrudEntityUser> allUsers = [];
  void loadUsers() async {
    allUsers = state = (await backend.requestJson('GET', '/api/user/list') as List)
        .map((item) => CrudEntityUser.fromJson(item)).toList();
  }

  void filter(String searchText) {
    var searchStrings = searchText.toLowerCase().split(' ').where((s) => s.isNotEmpty).toList();
    contains(List<String?> src, List<String> searchStrings) {
      searchLoop:
      for (var search in searchStrings) {
        for (var word in src) {
          if (word != null && word.toLowerCase().contains(search)) continue searchLoop;
        }
        return false;
      }
      return true;
    }
    List<CrudEntityUser> newState = allUsers.where((u) => contains([u.firstName, u.lastName, u.nickName], searchStrings)).toList();
    if (newState != state) state = newState;
  }
}
