import 'dart:developer' as developer;

import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:logging/logging.dart';

import 'drawer.dart';
import 'widgets/scroll_list_selector.dart';
import 'widgets/ui_subscription.dart';
import 'widgets/ui_attend.dart';
import '../blocs/crud_ticket.dart';

class HomeScreen extends StatefulWidget {
  final Injector _injector;
  const HomeScreen(this._injector, {Key? key}) : super(key: key);

  @override
  State<StatefulWidget> createState() => HomeScreenState();
}

class HomeScreenState extends State<HomeScreen> {
  static final Logger log = Logger('HomeScreenState');

  final GlobalKey _keyFAB = GlobalKey();

  late final CrudTicketBloc _ticketBloc;
  late final CrudVisitBloc _visitBloc;

  @override
  void initState() {
    super.initState();
    _ticketBloc = widget._injector.get<CrudTicket>().bloc();
    _visitBloc = widget._injector.get<CrudVisit>().bloc();
    _ticketBloc.crudTicket.loadTickets();
    _visitBloc.crudVisit.loadVisits(DateTime.now().subtract(const Duration(days: 14)), 10);
  }

  @override
  void dispose() {
    super.dispose();
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
                  for (var ticket in ticketsSnapshot.requireData)
                    UiSubscription(name: ticket.ticketType.name,
                        start: ticket.start,
                        count: ticket.ticketType.visits, used: ticket.visited
                    ),
                ],)),
          StreamBuilder(
            stream: _visitBloc.visitsState,
            initialData: _visitBloc.crudVisit.visits,
            builder: (BuildContext context, AsyncSnapshot<List<CrudEntityVisit>> visitsSnapshot) {
              List<Widget> prevWidgets = [], nextWidgets = [];
              DateTime now = DateTime.now();
              for (var visit in visitsSnapshot.requireData) {
                var training = visit.training!;
                var widget = UiAttend(name: training.trainingType.trainingName, date: training.time, marked: visit.markSelf);
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
        child: Icon(Icons.add),
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
        PopupMenuItem(
          value: 1,
          child: Text("Записаться"),
        ),
        PopupMenuItem(
          value: 2,
          child: Text("Отметиться"),
        ),
        PopupMenuItem(
          value: 3,
          child: Text("Купить еду"),
        ),
      ],
      elevation: 8.0,
    );
    developer.log("Menu item $result selected");
    if (result == 1) {
      _onAddTrain(_keyFAB.currentContext!);
    }
  }

  _onAddTrain(BuildContext context) async {
    var result = await showDialog(context: context,
        builder: (BuildContext c) {
          return SimpleDialog(
            title: Text('Выберите тренировку'),
            elevation: 5,
            children: [
              Container(
          height: 300,
          child:
              Row(
                children: [
                  Flexible(child: WheelListSelector(items: [
                    'Кроссфит',
                    'Растяжка',
                    'Индивидуальная',
                  ],)),
                  Flexible(child: WheelListSelector(items: [
                    'Понедельник, 10:00',
                    'Понедельник, 12:00',
                    'Вторник 8:00',
                    'Среда 10:00',
                  ],)),
                ],
              ),
          )
          ]
          );
        });
    developer.log("Dialog result: $result");
  }

/*
              ListWheelScrollView(
                itemExtent: 42,
                children: [
                ],
                // restorationId: ,
                onSelectedItemChanged: (item) => developer.log("Item selected $item"),
              )
*/

/*
  _showAddTrainP(BuildContext context) {
    new Picker(
        adapter: PickerDataAdapter(data: [
          new PickerItem(text: Text('Кроссфит'), value: Icons.add, children: [
            new PickerItem(text: Icon(Icons.more)),
            new PickerItem(text: Icon(Icons.aspect_ratio)),
            new PickerItem(text: Icon(Icons.android)),
            new PickerItem(text: Icon(Icons.menu)),
          ]),
          new PickerItem(text: Text('Растяжка'), value: Icons.title, children: [
            new PickerItem(text: Icon(Icons.more_vert)),
            new PickerItem(text: Icon(Icons.ac_unit)),
            new PickerItem(text: Icon(Icons.access_alarm)),
            new PickerItem(text: Icon(Icons.account_balance)),
          ]),
          new PickerItem(text: Icon(Icons.face), value: Icons.face, children: [
            new PickerItem(text: Icon(Icons.add_circle_outline)),
            new PickerItem(text: Icon(Icons.add_a_photo)),
            new PickerItem(text: Icon(Icons.access_time)),
            new PickerItem(text: Icon(Icons.adjust)),
          ]),
          new PickerItem(text: Icon(Icons.linear_scale), value: Icons.linear_scale, children: [
            new PickerItem(text: Icon(Icons.assistant_photo)),
            new PickerItem(text: Icon(Icons.account_balance)),
            new PickerItem(text: Icon(Icons.airline_seat_legroom_extra)),
            new PickerItem(text: Icon(Icons.airport_shuttle)),
            new PickerItem(text: Icon(Icons.settings_bluetooth)),
          ]),
          new PickerItem(text: Icon(Icons.close), value: Icons.close),
        ]),
        title: Text(""),
        cancel: Text(""),
        confirm: Text(""),
        onConfirm: (Picker picker, List value) {
          print(value.toString());
          print(picker.getSelectedValues());
        }
    ).showDialog(context);
        // .show(_scaffoldKey.currentState); Scaffold.of(context)
  }
*/

}
