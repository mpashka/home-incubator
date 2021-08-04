import 'package:flutter/material.dart';
import 'attends_month.dart';
import 'attends_week.dart';
import 'drawer.dart';
import 'widgets/ui_attend.dart';

class AttendsScreen extends StatelessWidget {

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
        length: 2,
        child: Scaffold(
            appBar: AppBar(
              title: Text('Totem - Тренировки'),
              bottom: TabBar(
                tabs: [
                  Text('Неделя'),
                  Text('Месяц'),
                ],
              ),
            ),
            drawer: MyDrawer(),
            body: TabBarView(
                children: [
                  AttendsWeekScreen(),
                  AttendsMonthScreen(),
                ]
            )
        )
    );
  }
}