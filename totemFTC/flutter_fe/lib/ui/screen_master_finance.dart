
import 'package:flutter/material.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_finance.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/blocs/crud_user.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/misc/utils.dart';

import 'drawer.dart';
import 'screen_base.dart';
import 'widgets/ui_divider.dart';
import 'widgets/ui_selector_user.dart';
import 'widgets/ui_visit.dart';
import 'widgets/ui_wheel_list_selector.dart';

class ScreenMasterFinance extends StatefulWidget {

  static const routeName = '/master_finance';
  bool total;


  ScreenMasterFinance({required this.total});

  @override
  State createState() => ScreenMasterFinanceState();
}

class ScreenMasterFinanceState extends BlocProvider<ScreenMasterFinance> {
  static const weekStartDay = 1;  // Monday
  static const monthBackLog = 2;
  static const weekBackLog = 6;

  late final CrudIncomeBloc monthIncome;
  late final CrudIncomeBloc weekIncome;

  @override
  void initState() {
    super.initState();
    var now = DateTime.now();
    DateTime weekStart = DateTime(now.year, now.month, now.day - (weekBackLog * 7 + (now.weekday - weekStartDay) % 7));
    DateTime monthStart = DateTime(now.year, now.month - monthBackLog);

    monthIncome = CrudIncomeBloc(provider: this, name: 'IncomeByMonth');
    weekIncome = CrudIncomeBloc(provider: this, name: 'IncomeByWeek');

    if (widget.total) {
      monthIncome.loadIncome(monthStart, FinancePeriod.month);
      weekIncome.loadIncome(weekStart, FinancePeriod.week);
    } else {
      monthIncome.loadCurrentUserIncome(monthStart, FinancePeriod.month);
      weekIncome.loadCurrentUserIncome(weekStart, FinancePeriod.week);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return DefaultTabController(
        length: 2,
        child: UiScreen(
          appBarBottom: const TabBar(
            tabs: [
              Tab(text: 'Неделя'),
              Tab(text: 'Месяц'),
            ],
          ),
          body: TabBarView(
            children: [
              BlocProvider.streamBuilder<List<CrudEntityIncome>, CrudIncomeBloc>(blocName: 'IncomeByWeek',
                builder: (ctx, incomes) =>
                    ListView.builder(
                        itemCount: incomes.length+1,
                        itemBuilder: (context, index) {
                          if (index == 0) {
                            return Container(color: theme.colorScheme.background, constraints: BoxConstraints(maxHeight: 32),
                              child: Row(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
                                if (widget.total) Expanded(flex: 4, child: Text('Тренер')),
                                Expanded(flex: 1, child: Text('#')),
                                Expanded(flex: 3, child: Text('Дата')),
                                Expanded(flex: 1, child: Text('Тренировки')),
                                Expanded(flex: 1, child: Text('Посещения')),
                                Expanded(flex: 2, child: Text('Доход')),
                              ],),);
                          }
                          var income = incomes[index - 1];
                          return Container(color: index % 2 == 0 ? Colors.grey.shade200 : theme.colorScheme.surface, child: Row(children: [
                            if (widget.total) Expanded(flex: 4, child: Text(income.trainer!.displayName)),
                            Expanded(flex: 1, child: Text(((income.date.difference(DateTime(income.date.year, 1, 1, 0, 0)).inDays / 7.0).ceil()).toString())),
                            Expanded(flex: 3, child: Text(weekDateFormat.format(income.date))),
                            Expanded(flex: 1, child: Text(income.trainings.toString())),
                            Expanded(flex: 1, child: Text(income.visits.toString())),
                            Expanded(flex: 2, child: Text(income.income.toString())),
                          ],),);
                        }),
              ),
              BlocProvider.streamBuilder<List<CrudEntityIncome>, CrudIncomeBloc>(blocName: 'IncomeByMonth',
                builder: (ctx, incomes) =>
                    ListView.builder(
                        itemCount: incomes.length+1,
                        itemBuilder: (context, index) {
                          if (index == 0) {
                            return Container(color: theme.colorScheme.background, constraints: BoxConstraints(maxHeight: 32),
                              child: Row(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
                                if (widget.total) Expanded(flex: 4, child: Text('Тренер')),
                                Expanded(flex: 3, child: Text('Месяц')),
                                Expanded(flex: 1, child: Text('Тренировки')),
                                Expanded(flex: 1, child: Text('Посещения')),
                                Expanded(flex: 2, child: Text('Доход')),
                              ],),);
                          }
                          var income = incomes[index - 1];
                          return Container(color: index % 2 == 0 ? Colors.grey.shade200 : theme.colorScheme.surface, child: Row(children: [
                            if (widget.total) Expanded(flex: 4, child: Text(income.trainer!.displayName)),
                            Expanded(flex: 3, child: Text(monthDateFormat.format(income.date))),
                            Expanded(flex: 1, child: Text(income.trainings.toString())),
                            Expanded(flex: 1, child: Text(income.visits.toString())),
                            Expanded(flex: 2, child: Text(income.income.toString())),
                          ],),);
                        }),
              )
            ],
          ),
        )
    );
  }

}
