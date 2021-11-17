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
  static final Logger log = Logger('BlocProvider');

  final Widget child;
  final void Function(BlocProvider) init;
  final Map<Type, BlocBaseList> blocs = HashMap();
  bool stateInit = false;

  BlocProvider({Key? key, required this.init, required this.child}): super(key: key);

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

  /// Is provider owner widgets during init
  T blocListCreate<A, T extends BlocBaseList<A>>() {
    return (createBlocList<A>() as dynamic) as T;
  }

  /// Can be called by widgets to get appropriate bloc
  static T blocListGet<A, T extends BlocBaseList<A>>(BuildContext context) {
    return (of(context).getBlocList<A>() as dynamic) as T;
  }

  Widget _streamBuilderList<T>(Widget Function(List<T> data) builder) {
    BlocBaseList<T> bloc = getBlocList<T>();
    return StreamBuilder<List<T>>(
      stream: bloc.stateOut,
      initialData: bloc.state,
      builder: (BuildContext context, AsyncSnapshot<List<T>> snapshot) => builder(snapshot.requireData),
    );
  }

  BlocBaseList<T> getBlocList<T>() {
    Type dataType = typeOf<T>();
    if (dataType == dynamic) {
      throw Exception('Internal error. Dynamic data type specified for bloc');
    }
    BlocBaseList? blocList = blocs[dataType];
    if (blocList == null) {
      throw Exception('Internal error. Bloc <$dataType> not found}');
    }
    return blocList as dynamic;
  }

  BlocBaseList<T> createBlocList<T>() {
    Type dataType = typeOf<T>();
    if (dataType == dynamic) {
      throw Exception('Internal error. Dynamic data type specified for bloc');
    }
    if (!stateInit) {
      throw Exception('Internal error. Attempt to create bloc out of init() method');
    }
    BlocBaseList? blocList = blocs[dataType];
    if (blocList == null) {
      log.fine('Bloc $dataType not found. Create new');
      switch (dataType) {
        case CrudEntityTrainingType: blocList = CrudTrainingTypeBloc(this); break;
        case CrudEntityTraining: blocList = CrudTrainingBloc(); break;
        case CrudEntityVisit: blocList = CrudVisitBloc(); break;
        case CrudEntityTicket: blocList = CrudTicketBloc(); break;
        default: throw Exception('Internal error. Unknown list type $dataType');
      }
      blocs[dataType] = blocList;
    } else {
      blocList.log.warning('Attempt to create already present bloc');
      throw Exception('Internal error. Bloc <$dataType> was already present}');
    }
    return blocList as dynamic;
  }

  static BlocProvider of(BuildContext context) {
    BlocProvider? provider = context.findAncestorWidgetOfExactType<BlocProvider>();
    if (provider == null) {
      throw Exception('Internal error. BlocProvider not found in tree');
    }
    return provider;
  }

}

class _BlocProviderState extends State<BlocProvider> {
  static final Logger log = Logger('_BlocProviderState');

  @override
  void initState() {
    log.finer('Init state...');
    super.initState();
    widget.stateInit = true;
    widget.init(widget);
    widget.stateInit = false;
    log.finer('Init complete');
  }

  @override
  void dispose() {
    log.finer('Dispose');
    super.dispose();
    widget.dispose();
  }

  @override
  Widget build(BuildContext context) {
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
    log = Logger('${runtimeType.toString()}<${typeOf<T>()}>');
    log.fine('Init');
    Injector injector = Injector();
    backend = injector.get<CrudApi>();
    session = injector.get<Session>();
    stateOut = _streamController.stream/*.asBroadcastStream()*/;
    stateIn = _streamController.sink;
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
    log.fine('Update list state [${state.length}]: $state');
  }

  void clear() {
    state = [];
  }
}
