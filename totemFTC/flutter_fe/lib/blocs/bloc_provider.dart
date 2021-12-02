import 'dart:async';
import 'dart:collection';

import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:logging/logging.dart';

import 'crud_api.dart';

abstract class BlocProvider<TWidget extends StatefulWidget> extends State<TWidget> {
  late final Logger log /*= Logger('BlocProviderState')*/;

  final Map<String, BlocBase> blocs = HashMap();


  @override
  void initState() {
    log = Logger('$runtimeType[$hashCode of ${widget.hashCode}]');
    log.finer('Init state...');
    super.initState();
  }

  @override
  void dispose() {
    log.finer('Dispose...');
    super.dispose();
    blocs.values.forEach((b) => b.dispose());
  }

  T addBloc<T extends BlocBase>({required T bloc, String? name}) {
    name ??= bloc.runtimeType.toString();
    if (blocs[name] != null) {
      throw Exception('Internal error. Bloc $name was already present');
    } else {
      blocs[name] = bloc;
    }
    return bloc;
  }

  T getBloc<T extends BlocBase>([String? name]) {
    name ??= typeOf<T>().toString();
    BlocBase? bloc = blocs[name];
    if (bloc == null) {
      throw Exception('Internal error. Bloc <$name> not found}');
    }
    if (bloc is! T) {
      throw Exception('Internal error. Bloc <$name> is not of type $name');
    }
    return bloc;
  }

  //
  // Static helpers
  //

  static Widget streamBuilder<T, B extends BlocBaseState<T>>({required Widget Function(BuildContext context, T data) builder, String? blocName}) {
    return Builder(builder: (ctx) {
      var bloc = of(ctx).getBloc<B>(blocName);
      return StreamBuilder<T>(
        stream: bloc._stateOut,
        initialData: bloc.state,
        // snapshot.requireData can't report null
        builder: (BuildContext context, AsyncSnapshot<T> snapshot) => builder(context, bloc.state),
      );
    });
  }

  static BlocProvider of(BuildContext context) {
    BlocProvider? provider = context.findAncestorStateOfType<BlocProvider>();
    if (provider == null) {
      throw Exception('Internal error. BlocProvider not found in widget tree');
    }
    return provider;
  }

  /// Can be called by widgets to get appropriate bloc
  static T getProviderBloc<T extends BlocBase>(BuildContext context, {String? name}) {
    return of(context).getBloc<T>(name);
  }
}

abstract class BlocBase {

  BlocBase({required BlocProvider provider, String? name}) {
    provider.addBloc(bloc: this, name: name);
  }

  void dispose();
}

class BlocBaseState<T> extends BlocBase {
  late final Logger log;

  late final CrudApi backend;
  late final Session session;

  final StreamController<T> _streamController = StreamController<T>();
  late final Sink<T> stateIn;
  late final Stream<T> _stateOut;
  T _state;

  BlocBaseState({required T state, required BlocProvider provider, String? name}): _state = state, super(provider: provider, name: name) {
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

  Stream<T> get stateOut {
    log.fine("Get stream");
    return _stateOut;
  }

  T get state => _state;

  set state(T state) {
    _state = state;
    stateIn.add(state);
    log.fine('Update state: $state');
  }
}

abstract class BlocBaseList<T> extends BlocBaseState<List<T>> {
  BlocBaseList({List<T> state = const [], required BlocProvider provider, String? name}): super(state: state, provider: provider, name: name);
}
