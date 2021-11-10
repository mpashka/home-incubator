import 'dart:async';
import 'dart:collection';
import 'dart:developer' as developer;

import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:intl/intl.dart';
import 'package:logging/logging.dart';

import 'drawer.dart';
import 'widgets/scroll_list_selector.dart';
import 'widgets/ui_subscription.dart';
import 'widgets/ui_attend.dart';
import '../blocs/crud_ticket.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({Key? key}) : super(key: key);

  @override
  State<StatefulWidget> createState() => HomeScreenState();
}

class HomeScreenState extends State<HomeScreen> {
  static final Logger log = Logger('HomeScreenState');

  final GlobalKey _keyFAB = GlobalKey();

  late final Session _session;
  late final CrudTicketBloc _ticketBloc;
  late final CrudVisitBloc _visitBloc;
  late final CrudTrainingBloc _trainingBloc;

  @override
  void initState() {
    super.initState();
    final injector = Injector();
    _session = injector.get<Session>();
    _trainingBloc = injector.get<CrudTraining>().bloc();
    _ticketBloc = injector.get<CrudTicket>().bloc();
    _visitBloc = injector.get<CrudVisit>().bloc();
    _ticketBloc.crudTicket.loadTickets();
    _visitBloc.crudVisit.loadVisits(DateTime.now().subtract(const Duration(days: 14)), 10);
  }

  @override
  void dispose() {
    super.dispose();
    _ticketBloc.dispose();
    _visitBloc.dispose();
    _trainingBloc.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Totem FC'),
      ),
      drawer: MyDrawer(),
      body: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          StreamBuilder(
              stream: _ticketBloc.ticketsState,
              initialData: _ticketBloc.crudTicket.tickets,
              builder: (BuildContext context, AsyncSnapshot<List<CrudEntityTicket>> ticketsSnapshot) => Column(
                children: [
                  if (ticketsSnapshot.requireData.isNotEmpty) Row(children: const [Divider(), Text('Абонементы')]),
                  for (var ticket in ticketsSnapshot.requireData)
                    UiSubscription(ticket),
                ],)),
          StreamBuilder(
            stream: _visitBloc.visitsState,
            initialData: _visitBloc.crudVisit.visits,
            builder: (BuildContext context, AsyncSnapshot<List<CrudEntityVisit>> visitsSnapshot) {
              List<Widget> prevWidgets = [], nextWidgets = [];
              DateTime now = DateTime.now();
              for (var visit in visitsSnapshot.requireData) {
                var training = visit.training!;
                var widget = UiAttend(visit);
                (training.time.isBefore(now) ? prevWidgets : nextWidgets).add(widget);
              }
              return Column(children: [
                if (prevWidgets.isNotEmpty) Row(children: const [Divider(), Text('Прошло')]),
                for (var widget in prevWidgets) widget,
                if (nextWidgets.isNotEmpty) Row(children: const [Divider(), Text('Будет')]),
                for (var widget in nextWidgets) widget,
              ],);
            },
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        key: _keyFAB,
        onPressed: _showAddMenu,
        tooltip: 'Add',
        child: const Icon(Icons.add),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }

  _showAddMenu() async {
    developer.log("Add menu");
    _keyFAB.currentContext!.findRenderObject();
    final RenderBox renderBox = _keyFAB.currentContext!.findRenderObject()! as RenderBox;
    var size = renderBox.size;
    var position = renderBox.localToGlobal(Offset.zero);
    // developer.log("Position: $position, size: $size");

    var result = await showMenu<int>(
      context: _keyFAB.currentContext!,
      position: RelativeRect.fromLTRB(position.dx - size.width, position.dy - size.height, position.dx - size.width, position.dy - size.height),
      // position: RelativeRect.fromLTRB(100, 100, 200, 200),
      items: [
        const PopupMenuItem(
          value: 1,
          child: Text("Записаться"),
        ),
        const PopupMenuItem(
          value: 2,
          child: Text("Отметиться"),
        ),
/*
        PopupMenuItem(
          value: 3,
          child: Text("Купить еду"),
        ),
*/
      ],
      elevation: 8.0,
    );
    log.finer("Menu item $result selected");
    if (result == 1) {
      _onAddTraining(_keyFAB.currentContext!, result == 2);
    }
  }

  final _dateTimeFormatter = DateFormat('EEE,dd HH:mm');

  Future<void> _onAddTraining(BuildContext context, bool past) async {
    DateTime now = DateTime.now();
    DateTime from, to;
    if (past) {
      to = now;
      from = now.subtract(Duration(days: 3));
    } else {
      from = now;
      to = now.add(Duration(days: 3));
    }

    List<CrudEntityTraining> allTrainings = [];

    StreamController<List<CrudEntityTrainingType>> trainingTypesCtrl = StreamController<List<CrudEntityTrainingType>>();
    StreamController<List<CrudEntityTraining>> trainingsCtrl = StreamController<List<CrudEntityTraining>>();
    List<CrudEntityTrainingType> trainingTypes = [];
    List<CrudEntityTraining> trainings = [];
    Sink<List<CrudEntityTrainingType>> trainingTypesIn = trainingTypesCtrl.sink;
    Stream<List<CrudEntityTrainingType>> trainingTypesOut = trainingTypesCtrl.stream;
    Sink<List<CrudEntityTraining>> trainingsIn = trainingsCtrl.sink;
    Stream<List<CrudEntityTraining>> trainingsOut = trainingsCtrl.stream;

    _trainingBloc.loadTrainings(from, to)
        .then((trainings) {
      allTrainings = trainings;
      Set<CrudEntityTrainingType> loadedTypes = HashSet<CrudEntityTrainingType>();
      for (var training in allTrainings) {
        loadedTypes.add(training.trainingType);
      }
      trainingTypes = List.of(loadedTypes, growable: false);
      trainingTypes.sort();
      trainingTypesIn.add(trainingTypes);
      // Scrollable.ensureVisible(context);
    });

    void onTrainingTypeChange(CrudEntityTrainingType trainingType) {
      trainings.clear();
      for (var training in allTrainings) {
        if (trainingType == training.trainingType) {
          trainings.add(training);
        }
      }
      trainingsIn.add(trainings);
    }

    CrudEntityTraining? selectedTraining;
    void onTrainingChange(CrudEntityTraining training) {
      selectedTraining = training;
    }

    var result = await showDialog(context: context,
        builder: (BuildContext c) {
          return SimpleDialog(
              title: const Text('Выберите тренировку'),
              elevation: 5,
              children: [
                SizedBox(
                    height: 150,
                    child:
                    Row(children: [
                      Flexible(
                          child: WheelListSelector<CrudEntityTrainingType>(
                            initialData: trainingTypes,
                            dataStream: trainingTypesOut,
                            childBuilder: (context, index, trainingType) => Text(trainingType.trainingName),
                            onSelectedItemChanged: onTrainingTypeChange,
                          )),
                      Flexible(
                          child: WheelListSelector<CrudEntityTraining>(
                              initialData: trainings,
                              dataStream: trainingsOut,
                              childBuilder: (context, index, training) => Text('${_dateTimeFormatter.format(training.time)} ${training.trainer.nickName}'),
                              onSelectedItemChanged: onTrainingChange
                          )),
                    ],),
                ),
                Row(children: [
                  SimpleDialogOption(
                    child: const Text('Ok'),
                    onPressed: () => Navigator.pop(context, selectedTraining),
                  ),
                  SimpleDialogOption(
                    child: const Text('Cancel'),
                    onPressed: () => Navigator.pop(context),
                  ),
                ],)
              ]
          );
        });
    log.finer("Dialog result: $result");
    if (result != null) {
      bool _past = selectedTraining!.time.isBefore(DateTime.now());
      CrudEntityVisit visit = CrudEntityVisit(
          user: _session.user,
          training: selectedTraining,
          markSchedule: _past ? false : true,
          markSelf: _past ? CrudEntityVisitMark.on : CrudEntityVisitMark.unmark,
          markMaster: CrudEntityVisitMark.unmark);
      _visitBloc.addVisit(visit);
    }
  }
}
