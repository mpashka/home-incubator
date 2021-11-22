import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_ticket.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/blocs/crud_user.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:flutter_fe/ui/widgets/ui_divider.dart';
import 'package:flutter_fe/ui/widgets/ui_selector_user.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:logging/logging.dart';

import 'drawer.dart';
import 'screen_base.dart';
import 'widgets/ui_selector_training.dart';
import 'widgets/ui_ticket.dart';
import 'widgets/ui_visit.dart';
import 'widgets/ui_user.dart';

class ScreenMasterUser extends StatelessWidget {
  static final Logger log = Logger('ScreenMasterUser');

  static const routeName = '/master_user';
  static const backlogDays = 14;
  final CrudEntityUser _user;

  const ScreenMasterUser(this._user);

  @override
  Widget build(BuildContext context) {
    late final SelectedUserBloc selectedUserBloc;
    late final UserVisitsBloc visitsBloc;
    return BlocProvider(
      init: (blocProvider) {
        selectedUserBloc = blocProvider.addBloc(bloc: SelectedUserBloc(_user));
        blocProvider.addBloc(bloc: UserTicketsBloc(selectedUserBloc)).loadUserTickets();
        visitsBloc = blocProvider.addBloc(bloc: UserVisitsBloc(selectedUserBloc))
          ..loadUserVisits();
      },
      child: UiScreen(body: BlocProvider.streamBuilder<CrudEntityUser, SelectedUserBloc>(builder: (user) => Column(children: [
        Row(children: [
          Expanded(child: UiUser(user)),
          GestureDetector(
            child: Icon(Icons.more_vert),
            onTap: () async {
              CrudEntityUser? user = await UiSelectorUser().selectUserDialog(context, 'Пользователи');
              if (user != null) selectedUserBloc.state = user;
            },
          )
        ],),
        UiDivider('Абонементы'),
        BlocProvider.streamBuilder<List<CrudEntityTicket>, UserTicketsBloc>(builder: (tickets) {
          if (tickets.isEmpty) {
            return Text('Нет абонементов');
          } else {
            return Column(children: [
              for (var ticket in tickets)
                GestureDetector(
                  child: UiTicket(ticket,
                    leading: Radio<CrudEntityTicket?>(
                      onChanged: null,
                      value: ticket,
                      groupValue: visitsBloc.selectedTicket,
                    ),),
                  onTap: () => visitsBloc.selectedTicket = ticket != visitsBloc.selectedTicket ? ticket : null,
                ),
            ],);
          }
        }),
        UiDivider('Посещения'),
        Flexible(child: BlocProvider.streamBuilder<List<CrudEntityVisit>, UserVisitsBloc>(builder: (visits) {
          if (visits.isEmpty) {
            return Text('Нет посещений');
          } else {
            return ListView(children: [
              for (var visit in visits) UiVisit(visit, forTrainer: true,),
            ],);
          }
        }),),
      ]),),
        floatingActionButton: FloatingActionButton(
          onPressed: () => _onAddTraining(context, selectedUserBloc, visitsBloc),
          tooltip: 'Add',
          child: Icon(Icons.add),
        ),
      ),);
  }

  Future<void> _onAddTraining(BuildContext context, SelectedUserBloc selectedUserBloc, UserVisitsBloc visitsBloc) async {
    Session session = Injector().get<Session>();
    DateTime now = DateTime.now();
    var training = await UiSelectorTraining('Отметить тренировку').selectTraining(context,
        dateRange: DateTimeRange(
            start: now.subtract(Duration(days: ScreenMasterUser.backlogDays)),
            end: now),
        trainingFilter: (training) => training.trainer == session.user
    );
    log.finer("Select training dialog result: $training");
    if (training != null) {
      CrudEntityVisit visit = CrudEntityVisit(
          user: selectedUserBloc.state,
          training: training,
          trainingId: training.id,
          markMaster: CrudEntityVisitMark.on);
      visitsBloc.markMaster(visit, CrudEntityVisitMark.on);
    }
  }
}

class SelectedUserBloc extends BlocBaseState<CrudEntityUser> {
  SelectedUserBloc(CrudEntityUser user): super(user);
}

class UserTicketsBloc extends BlocBaseList<CrudEntityTicket> {
  SelectedUserBloc selectedUserBloc;

  UserTicketsBloc(this.selectedUserBloc) {
    selectedUserBloc.stateOut.forEach((user) => loadUserTickets);
  }

  void loadUserTickets() async {
    state = [];
    state = (await backend.requestJson('GET', '/api/tickets/byUser/${selectedUserBloc.state.userId}') as List)
        .map((item) => CrudEntityTicket.fromJson(item)..user = selectedUserBloc.state).toList();
  }
}

class UserVisitsBloc extends CrudVisitBloc {
  SelectedUserBloc selectedUserBloc;
  CrudEntityTicket? _selectedTicket;

  UserVisitsBloc(this.selectedUserBloc) {
    selectedUserBloc.stateOut.forEach((user) => loadUserVisits);
  }

  void loadUserVisits() async {
    state = [];
    state = (await backend.requestJson('GET', _selectedTicket == null
        ? '/api/visit/byUser/${selectedUserBloc.state.userId}'
        : '/api/visit/byTicket/${_selectedTicket!.id}',
        params: {'from': dateTimeFormat.format(DateTime.now().subtract(const Duration(days: ScreenMasterUser.backlogDays)))}) as List)
        .map((item) => CrudEntityVisit.fromJson(item)..user = selectedUserBloc.state).toList();
  }

  CrudEntityTicket? get selectedTicket => _selectedTicket;
  set selectedTicket(CrudEntityTicket? selectedTicket) {
    _selectedTicket = selectedTicket;
    loadUserVisits();
  }
}
