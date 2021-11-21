import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/blocs/crud_user.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:intl/intl.dart';

import 'ui_user.dart';
import 'wheel_list_selector.dart';

class UiSelectorUser {

  final String title;

  UiSelectorUser(this.title);

  Future<CrudEntityUser?> selectUser(BuildContext context) async {
    late final FilteredUsersBloc userBloc;
    late final TextEditingController searchTextController;
    return await showDialog(context: context,
        builder: (BuildContext c) => BlocProvider(
            init: (blocProvider) {
              userBloc = blocProvider.addBloc(bloc: FilteredUsersBloc());
              searchTextController = TextEditingController()
                ..addListener(() => userBloc.filter(searchTextController.text));
            },
            child: SimpleDialog(
                title: Text(title),
                elevation: 5,
                children: [
                  Row(children: [
                    Icon(Icons.search),
                    TextField(
                      controller: searchTextController,
                      autofocus: true,
                    ),
                  ],),
                  Flexible(
                    child: BlocProvider.streamBuilder<List<CrudEntityUser>, FilteredUsersBloc>(builder: (users) => ListView(children: [
                      for (var user in users) GestureDetector(
                        child: UiUser(user),
                        onTap: () => Navigator.pop(context, user),
                      )
                    ],),),),
                  Row(children: [
                    SimpleDialogOption(
                      child: const Text('Cancel'),
                      onPressed: () => Navigator.pop(context),
                    ),
                  ],)
                ])));
  }
}

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
