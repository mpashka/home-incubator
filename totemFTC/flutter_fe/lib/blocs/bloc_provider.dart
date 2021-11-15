import 'dart:async';
import 'dart:collection';

import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/crud_ticket.dart';
import 'package:flutter_fe/blocs/crud_visit.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:logging/logging.dart';

import 'crud_api.dart';

class BlocProvider extends StatefulWidget {

  final Widget child;
  final void Function(BlocProvider) init;
  late final Session session;
  final Map<Type, BlocBaseList> blocs = HashMap();

  BlocProvider({Key? key, required this.init, required this.child}): super(key: key) {
    final injector = Injector();
    session = injector.get<Session>();
  }

  @override
  _BlocProviderState createState() => _BlocProviderState();

  void dispose() {
    blocs.values.forEach((b) => b.dispose());
  }

  static Widget streamBuilderList<T>(Widget Function(List<T> data) builder) {
    return Builder(
      builder: (ctx) => of(ctx)._streamBuilderList(builder),
    );
  }
  
  T dynBlocList<A, T extends BlocBaseList<A>>() {
    return (getOrCreateBlocList<A>() as dynamic) as T;
  }

  static T blocList<A, T extends BlocBaseList<A>>(BuildContext context) {
    return (of(context).getOrCreateBlocList<A>() as dynamic) as T;
  }

  Widget _streamBuilderList<T>(Widget Function(List<T> data) builder) {
    BlocBaseList<T> bloc = getOrCreateBlocList<T>();
    return StreamBuilder<List<T>>(
      stream: bloc.stateOut,
      initialData: bloc.state,
      builder: (BuildContext context, AsyncSnapshot<List<T>> ticketsSnapshot) => builder(ticketsSnapshot.requireData),
    );
  }

  BlocBaseList<T> getOrCreateBlocList<T>() {
    Type dataType = typeOf<T>();
    BlocBaseList? blocList = blocs[dataType];
    if (blocList == null) {
      switch (dataType) {
        case CrudEntityTraining: blocList = CrudTrainingBloc(); break;
        case CrudEntityVisit: blocList = CrudVisitBloc(); break;
        case CrudEntityTicket: blocList = CrudTicketBloc(); break;
        default: throw Exception('Internal error. Unknown list type $dataType');
      }
    }
    return blocList as dynamic;
  }

  static BlocProvider of(BuildContext context) {
    BlocProvider? provider = context.findAncestorWidgetOfExactType<BlocProvider>();
    return provider!;
  }

}

class _BlocProviderState extends State<BlocProvider> {
  static final Logger log = Logger('_BlocProviderState');

  @override
  void initState() {
    log.finer('Init state');
    super.initState();
    widget.init(widget);
  }

  @override
  void dispose(){
    log.finer('Dispose');
    super.dispose();
    widget.dispose();
  }

  @override
  Widget build(BuildContext context){
    return widget.child;
  }
}

abstract class BlocBase {
  void dispose();
}

abstract class BlocBaseList<T> extends BlocBase {
  late final Logger log;

  late final CrudApi backend;
  late final Session session;

  final StreamController<List<T>> _streamController = StreamController<List<T>>();
  late final Sink<List<T>> stateIn;
  late final Stream<List<T>> stateOut;
  List<T> _state = [];

  BlocBaseList() {
    Injector injector = Injector();
    backend = injector.get<CrudApi>();
    session = injector.get<Session>();
    stateOut = _streamController.stream.asBroadcastStream();
    stateIn = _streamController.sink;
    log = Logger('BlocBaseList<${typeOf<T>()}>${runtimeType.toString()}');
  }

  @override
  void dispose() {
    stateIn.close();
    _streamController.close();
  }

  List<T> get state => _state;

  set state(List<T> state) {
    _state = state;
    stateIn.add(state);
    log.fine('Update state: $state');
  }

  void clear() {
    state = [];
  }
}
