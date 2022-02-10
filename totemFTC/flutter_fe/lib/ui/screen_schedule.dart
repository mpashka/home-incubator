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
import 'package:logging/logging.dart';

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
  static const sticky = false;

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

        return (sticky ? renderSticky : renderAnimation)(result);
      },),);
  }

  Widget renderSticky(List<DaySchedule> result) {
    return CustomScrollView(slivers: [
      for (var day in result) DayScheduleUi(day, now, widget.forTrainer, true)
    ],);
  }

  Widget renderAnimation(List<DaySchedule> result) {
    log.fine('Render schedule');
    return ListView(children: [
        for (var day in result) DayScheduleUi(day, now, widget.forTrainer, false)
      ],
    );
  }

}

class DaySchedule {
  final DateTime date;
  final List schedule = [];

  DaySchedule(this.date);
}

class DayScheduleUi extends StatefulWidget {
  final DaySchedule daySchedule;
  final DateTime now;
  final bool forTrainer;
  final bool sticky;

  DayScheduleUi(this.daySchedule, this.now, this.forTrainer, this.sticky);

  State createState() => sticky ? DayScheduleUiStickyState() : DayScheduleUiAnimateState();
}

abstract class DayScheduleUiState extends State<DayScheduleUi> {
  final Logger log = Logger('screen_schedule.DayScheduleUiState');

  late final DaySchedule daySchedule;
  late final DateTime now;
  late final Session _session;
  late final bool forTrainer;

  @override
  void initState() {
    super.initState();
    _session = Injector().get<Session>();
    daySchedule = widget.daySchedule;
    now = widget.now;
    forTrainer = widget.forTrainer;
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

class DayScheduleUiStickyState extends DayScheduleUiState {

  bool collapsed = true;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return SliverStickyHeader(
      header: Card(
        elevation: 8.0,
        color: theme.colorScheme.secondary,
        child: ListTile(
          title: Text(localDateFormat.format(daySchedule.date),),
          trailing:IconButton(
            onPressed: _expand,
            icon: Icon(collapsed ? Icons.add_circle_outline : Icons.remove_circle_outline),
          ),
        ),
      ),
      sliver: SliverList(
          delegate: SliverChildBuilderDelegate((context, i) {
            var item = daySchedule.schedule[i];
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
            childCount: collapsed ? 0 : daySchedule.schedule.length,
          ),),
    );
  }

  void _expand() {
    setState(() {
      collapsed = !collapsed;
    });
  }
}

class DayScheduleUiAnimateState extends DayScheduleUiState with TickerProviderStateMixin {

  late final AnimationController _animationController;
  late final Animation<double> _angle;


  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(duration: Duration(milliseconds: 100), vsync: this);
    _angle = Tween<double>(begin: 0, end: -0.25).animate(_animationController);
  }

  @override
  void dispose() {
    _animationController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    var uiItems = <Widget>[];
    for (var item in daySchedule.schedule) {
      if (item == now) {
        uiItems.add(Text('Сейчас'));
      } else {
        final training = item as CrudEntityTraining;
        Widget uiTraining = UiTraining(training, forSchedule: true,);
        if (training.trainer == _session.user) {
          uiTraining = GestureDetector(child: uiTraining,
              onTap: () => Navigator.pushNamed(context, ScreenMasterTrainings.routeName, arguments: training)
          );
        } else if (!widget.forTrainer && training.time.isAfter(now)) {
          uiTraining = GestureDetector(child: uiTraining,
            onTapDown: (tapDownDetails) => _showUserPopupMenu(context, training, tapDownDetails.globalPosition),
          );
        }
        uiItems.add(uiTraining);
      }
    }

    return Column(mainAxisSize: MainAxisSize.min,
      children: [
        GestureDetector(
          onTap: startAnimation,
          child: Card(
            elevation: 8.0,
            color: theme.colorScheme.secondary,
            child: ListTile(
              title: Text(localDateFormat.format(daySchedule.date),),
              trailing: RotationTransition(
                turns: _angle,
                child: Icon(Icons.arrow_back_ios),
              ),
            ),
          ),),
        SizeTransition(
          sizeFactor: _animationController,
          axis: Axis.vertical,
          axisAlignment: 1,
          child: Column(children: uiItems,),
        ),
      ],
    );
  }

  void startAnimation() {
    if (_animationController.isAnimating) {
      var status = _animationController.status;
      _animationController.stop();
      if (status == AnimationStatus.forward) {
        _animationController.animateBack(0);
      } else {
        _animationController.forward();
      }
    } else if (_animationController.isCompleted) {
      _animationController.animateBack(0);
    } else {
      _animationController.forward();
    }
  }
}
