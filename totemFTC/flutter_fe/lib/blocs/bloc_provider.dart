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
  final void Function(BlocProviderState) init;

  BlocProvider({Key? key, required this.init, required this.child}): super(key: key);

  @override
  BlocProviderState createState() => BlocProviderState();

  static Widget streamBuilder<T, B extends BlocBaseState<T>>({required Widget Function(T data) builder, String? name, B? bloc}) {
    Widget streamBuilder(B bloc) => StreamBuilder<T>(
      stream: bloc._stateOut,
      initialData: bloc.state,
      builder: (BuildContext context, AsyncSnapshot<T> snapshot) => builder(snapshot.requireData),
    );

    return bloc != null ? streamBuilder(bloc) : Builder(builder: (ctx) => streamBuilder(of(ctx).getBloc<B>(name)));
  }

  static BlocProviderState of(BuildContext context) {
    BlocProviderState? provider = context.findAncestorStateOfType<BlocProviderState>();
    if (provider == null) {
      throw Exception('Internal error. BlocProvider not found in widget tree');
    }
    return provider;
  }

  /// Can be called by widgets to get appropriate bloc
  static T getBloc<T extends BlocBase>(BuildContext context) {
    return (of(context).getBloc<T>() as dynamic) as T;
  }
}

class BlocProviderState extends State<BlocProvider> {
  static final Logger log = Logger('BlocProviderState');

  final Map<String, BlocBase> blocs = HashMap();
  bool stateInit = false;

  @override
  void initState() {
    log.finer('Init state...');
    super.initState();
    stateInit = true;
    widget.init(this);
    stateInit = false;
    log.finer('Init complete');
  }

  @override
  void dispose() {
    log.finer('Dispose');
    super.dispose();
    blocs.values.forEach((b) => b.dispose());
  }

  @override
  Widget build(BuildContext context) {
    return widget.child;
  }

  T getBloc<T extends BlocBase>([String? name]) {
    name ??= typeOf<T>().toString();
    BlocBase? bloc = blocs[name];
    if (bloc == null) {
      throw Exception('Internal error. Bloc <$name> not found}');
    }
    return (bloc as dynamic) as T;
  }

  T addBloc<T extends BlocBase>({required T bloc, String? name}) {
    if (!stateInit) {
      throw Exception('Internal error. Attempt to create bloc out of init() method');
    }
    name ??= bloc.runtimeType.toString();
    if (blocs[name] != null) {
      throw Exception('Internal error. Bloc $name was already present');
    } else {
      blocs[name] = bloc;
    }
    return bloc;
  }

}

abstract class BlocBase {
  void dispose();
}

abstract class BlocBaseState<T> extends BlocBase {
  late final Logger log;

  late final CrudApi backend;
  late final Session session;

  final StreamController<T> _streamController = StreamController<T>();
  late final Sink<T> stateIn;
  late final Stream<T> _stateOut;
  T _state;

  BlocBaseState(this._state) {
    log = Logger('${runtimeType.toString()}<${typeOf<T>()}>');
    log.fine('Init');
    Injector injector = Injector();
    backend = injector.get<CrudApi>();
    session = injector.get<Session>();
    _stateOut = _streamController.stream.asBroadcastStream();
    stateIn = _streamController.sink;
  }

  @override
  void dispose() {
    stateIn.close();
    _streamController.close();
  }

  T get state => _state;

  Stream<T> get stateOut {
    log.fine("Get stream");
    return _stateOut;
  }

  set state(T state) {
    _state = state;
    stateIn.add(state);
    log.fine('Update state: $state');
  }
}

abstract class BlocBaseList<T> extends BlocBaseState<List<T>> {
  BlocBaseList(): super([]);
}
