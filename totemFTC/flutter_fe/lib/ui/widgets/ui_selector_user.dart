import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/blocs/crud_user.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:flutter_fe/ui/screen_base.dart';
import 'package:intl/intl.dart';

import '../screen_master_user.dart';
import 'ui_user.dart';
import 'wheel_list_selector.dart';


class UiSelectorUserDialog extends StatefulWidget {

  final String title;


  const UiSelectorUserDialog(this.title);

  @override
  State createState() => UiSelectorUserDialogState();

  Future<CrudEntityUser?> selectUserDialog(BuildContext context) async {
    return await showDialog(context: context, builder: (ctx) => this);
  }
}

abstract class UiSelectorUserStateBase<TWidget extends StatefulWidget> extends BlocProvider<TWidget> {
  late final TextEditingController searchTextController;

  @override
  void initState() {
    super.initState();
    FilteredUsersBloc userBloc = FilteredUsersBloc(provider: this)
      ..loadUsers();
    searchTextController = TextEditingController();
    searchTextController.addListener(() => userBloc.filter(searchTextController.text));
  }

  List<Widget> buildContent(void Function(CrudEntityUser user) userSelector) {
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
      Expanded(child:
        BlocProvider.streamBuilder<List<CrudEntityUser>, FilteredUsersBloc>(builder: (ctx, users) => ListView(children: [
          for (var user in users) GestureDetector(
            child: UiUser(user),
            onTap: () => userSelector(user),
          )
        ],),),
      ),
    ];
  }
}

class UiSelectorUserDialogState extends UiSelectorUserStateBase<UiSelectorUserDialog> {

  @override
  Widget build(BuildContext context) {
    return Dialog(
      elevation: 5,
      child: Column(children: [
        Text(widget.title),
        ...buildContent((user) => Navigator.pop(context, user)),
        Row(mainAxisAlignment: MainAxisAlignment.end, children: [
          ElevatedButton(
            child: const Text('Cancel'),
            onPressed: () => Navigator.pop(context),
          ),
        ],)
      ],),);
  }
}

class FilteredUsersBloc extends BlocBaseList<CrudEntityUser> {
  List<CrudEntityUser> allUsers = [];

  FilteredUsersBloc({required BlocProvider provider, String? name}): super(provider: provider, name: name);

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
