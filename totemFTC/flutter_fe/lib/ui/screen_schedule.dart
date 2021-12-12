import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_api.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:flutter_fe/ui/screen_master_trainings.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:flutter_sticky_header/flutter_sticky_header.dart';

import 'screen_base.dart';
import 'widgets/ui_training.dart';

class ScreenSchedule extends StatefulWidget {
  static const routeName = '/schedule';
  static const routeNameMaster = '/master_schedule';

  final bool forTrainer;

  ScreenSchedule({this.forTrainer = false, Key? key}): super(key: key);

  @override
  State createState() => ScreenScheduleState();
}

class ScreenScheduleState extends BlocProvider<ScreenSchedule> {

  static final DateTime now = DateTime.now();
  late final Session _session;

  @override
  void initState() {
    super.initState();
    _session = Injector().get<Session>();
    var crudTrainingBloc = CrudTrainingBloc(provider: this, master: widget.forTrainer);
    if (widget.forTrainer) {
      crudTrainingBloc.loadMasterTrainings(now.subtract(backMaster), now.add(forwardMaster));
    } else {
      crudTrainingBloc.loadTrainings(now.subtract(back), now.add(forward));
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return UiScreen(
      body: BlocProvider.streamBuilder<List<CrudEntityTraining>,CrudTrainingBloc>(builder: (ctx, trainings) {
        DaySchedule currentDay = DaySchedule(DateTime(0));
        List<DaySchedule> result = [];
        bool nowAdded = false;
        void addNow(DateTime date) {
          if (!nowAdded && date.isAfter(now)) {
            currentDay.schedule.add(now);
            nowAdded = true;
          }
        }
        for (var training in trainings) {
          if (widget.forTrainer && training.trainer != _session.user) continue;
          DateTime newDate = training.time.dayDate();
          addNow(newDate);
          if (newDate.isAfter(currentDay.date)) {
            currentDay = DaySchedule(newDate);
            result.add(currentDay);
          }
          addNow(training.time);
          currentDay.schedule.add(training);
        }
        addNow(DateTime(3000));

        return CustomScrollView(slivers: [
          for (var day in result) SliverStickyHeader(
            header: Card(
              elevation: 8.0,
              color: theme.colorScheme.secondary,
              child: ListTile(title: Text(localDateFormat.format(day.date),),),
            ),
            sliver: SliverList(
              delegate: SliverChildBuilderDelegate((context, i) {
                var item = day.schedule[i];
                if (item == now) {
                  return Text('Сейчас');
                } else {
                  final training = item as CrudEntityTraining;
                  var uiTraining = UiTraining(training, forSchedule: true,);
                  if (training.trainer == _session.user) {
                    return GestureDetector(child: uiTraining,
                        onTap: () => Navigator.pushNamed(context, ScreenMasterTrainings.routeName, arguments: training)
                    );
                  } else if (!widget.forTrainer && training.time.isAfter(now)) {
                    return GestureDetector(child: uiTraining,
                      onTapDown: (tapDownDetails) => _showUserPopupMenu(context, training, tapDownDetails.globalPosition),
                    );
                  }
                  return uiTraining;
                }
              },
                childCount: day.schedule.length,
              ),
            ),
          )
        ],);
      },),);
  }

  void _showUserPopupMenu(BuildContext context, CrudEntityTraining training, Offset offset) async {
    log.finer("Menu position $offset");

    double left = offset.dx;
    double top = offset.dy;
    var result = await showMenu<int>(
      context: context,
      position: RelativeRect.fromLTRB(left, top, left, top),
      items: [
        const PopupMenuItem(
          value: 1,
          child: Text("Приду"),
        ),
      ],
      elevation: 8.0,
    );
    log.finer("Menu item $result selected");

    CrudApi api = Injector().get<CrudApi>();
    await api.request('PUT', '/api/visit/markSchedule/true', body: CrudEntityVisit(
        trainingId: training.id, training: training, markSchedule: true, user: _session.user));
  }
}



class DaySchedule {
  final DateTime date;
  final List schedule = [];

  DaySchedule(this.date);
}