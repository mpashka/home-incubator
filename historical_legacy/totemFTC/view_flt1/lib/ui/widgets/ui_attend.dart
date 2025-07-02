import 'dart:developer' as developer;

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

@immutable
class UiAttend extends StatelessWidget {
  static final format = DateFormat('yyyy-MM-dd kk:mm');

  final String name;
  final DateTime? date;
  final bool marked;


  UiAttend({
    required this.name,
    this.date,
    required this.marked});

  @override
  Widget build(BuildContext context) {
    var text;
    if (date != null) {
      text = '$name ${format.format(date!)}';
    } else {
      text = name;
    }

    var listTile = ListTile(
        leading: Row(
            // mainAxisAlignment: MainAxisAlignment.start,
            // crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(Icons.agriculture_rounded),
              Icon(Icons.assignment_turned_in_outlined),
              Icon(Icons.bookmark_added,
                  color: Colors.red),
            ]
        ),
        title: Text(text),
        trailing: Icon(Icons.more_vert),
      );

    return GestureDetector(
      child: listTile,
      onTapDown: (TapDownDetails details) {
        _showPopupMenu(context, details.globalPosition);
      },
    );
/*
    return PopupMenuButton<int>(
      child: listTile,
      itemBuilder: (context) => [
        PopupMenuItem(
          value: 1,
          child: Text("Отметить тренировку"),
        ),
        PopupMenuItem(
          value: 1,
          child: Text("Не был"),
        ),
        PopupMenuItem(
          value: 1,
          child: Text("Не приду"),
        ),
      ],
    );
*/
  }

  void _showPopupMenu(BuildContext context, Offset offset) async {
    developer.log("Menu position $offset");

    double left = offset.dx;
    double top = offset.dy;
    var result = await showMenu<int>(
      context: context,
      position: RelativeRect.fromLTRB(left, top, left, top),
      items: [
        PopupMenuItem(
          value: 1,
          child: Text("Отметить тренировку"),
        ),
        PopupMenuItem(
          value: 2,
          child: Text("Не был (прошлая)"),
        ),
        PopupMenuItem(
          value: 3,
          child: Text("Не приду (будущая)"),
        ),
        PopupMenuItem(
          value: 3,
          child: Text("Перенести"),
        ),
      ],
      elevation: 8.0,
    );
    developer.log("Menu item $result selected");
  }
}

