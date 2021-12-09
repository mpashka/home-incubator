import 'dart:async';
import 'dart:collection';

import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:logging/logging.dart';

import 'crud_api.dart';
import 'data_storage.dart';

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
      throw Exception('Internal error. Bloc <$name> is not of type ${typeOf<T>()} but ${bloc.runtimeType}');
    }
    return bloc;
  }

  //
  // Static helpers
  //

  // todo remove B, make name required, put bloc names into static constants
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

  BlocBaseState<Combined2<T1, T2>> combine2<T1, T2>(String name, b1d, b2d, {bool listen1=true, bool listen2=true}) {
    BlocBaseState<T1> b1 = b1d;
    BlocBaseState<T2> b2 = b2d;
    var bloc = BlocBaseState(state: Combined2(b1.state, b2.state), provider: this, name: name);
    if (listen1) {
      StreamSubscription<T1> subscription1 = b1.stateOut.listen((v1) {
        bloc.state = Combined2(v1, b2.state);
      });
      bloc.addDisposable(() => subscription1.cancel());
    }
    if (listen2) {
      StreamSubscription<T2> subscription2 = b2.stateOut.listen((v2) {
        bloc.state = Combined2(b1.state, v2);
      });
      bloc.addDisposable(() => subscription2.cancel());
    }
    return bloc;
  }
  
  BlocBaseState<Combined3<T1, T2, T3>> combine3<T1, T2, T3>(String name, b1d, b2d, b3d, {bool listen1=true, bool listen2=true, bool listen3=true}) {
    BlocBaseState<T1> b1 = b1d;
    BlocBaseState<T2> b2 = b2d;
    BlocBaseState<T3> b3 = b3d;
    var bloc = BlocBaseState(state: Combined3(b1.state, b2.state, b3.state), provider: this, name: name);
    if (listen1) {
      StreamSubscription<T1> subscription1 = b1.stateOut.listen((v1) {
        bloc.state = Combined3(v1, b2.state, b3.state);
      });
      bloc.addDisposable(() => subscription1.cancel());
    }
    if (listen2) {
      StreamSubscription<T2> subscription2 = b2.stateOut.listen((v2) {
        bloc.state = Combined3(b1.state, v2, b3.state);
      });
      bloc.addDisposable(() => subscription2.cancel());
    }
    if (listen3) {
      StreamSubscription<T3> subscription3 = b3.stateOut.listen((v3) {
        bloc.state = Combined3(b1.state, b2.state, v3);
      });
      bloc.addDisposable(() => subscription3.cancel());
    }
    return bloc;
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
  late final DataStorage dataStorage;

  final StreamController<T> _streamController = StreamController<T>();
  late final Sink<T> stateIn;
  late final Stream<T> _stateOut;
  late final List<void Function()> _disposables = [];
  T _state;

  BlocBaseState({required T state, required BlocProvider provider, String? name}): _state = state, super(provider: provider, name: name) {
    log = Logger('${runtimeType.toString()}<${typeOf<T>()}@$hashCode>');
    log.fine('Init');
    Injector injector = Injector();
    backend = injector.get<CrudApi>();
    session = injector.get<Session>();
    dataStorage = injector.get<DataStorage>();
    _stateOut = _streamController.stream.asBroadcastStream();
    stateIn = _streamController.sink;
  }

  @override
  void dispose() {
    stateIn.close();
    _streamController.close();
    _disposables.forEach((d) => d());
  }

  void addDisposable(void Function() dispose) {
    _disposables.add(dispose);
  }

  void addDisposableSubscription(StreamSubscription subscription) {
    addDisposable(() => subscription.cancel());
  }

  Stream<T> get stateOut {
    log.fine("Get stream");
    return _stateOut;
  }

  T get state => _state;

  set state(T state) {
    if (_state != state) {
      _state = state;
      stateIn.add(state);
      log.fine('Update state: $state');
    } else {
      log.fine('State remains the same: $_state');
    }
  }

  void listen(void Function(T event)? onData) {
    if (onData != null) {
      StreamSubscription<T> subscription = stateOut.listen(
          onData, onError: (e, s) => log.warning('listen().Error', e, s),
          onDone: () => log.fine('listen().Done'),
          cancelOnError: false);
      _disposables.add(() => subscription.cancel());
    }
  }
}

abstract class BlocBaseList<T> extends BlocBaseState<List<T>> {
  BlocBaseList({List<T> state = const [], required BlocProvider provider, String? name}): super(state: state, provider: provider, name: name);

  Future<List<T>> cache(String name, LoadFunction<T> load) async {
    var cacheKey = '${typeOf<T>()}_$name';
    if (state.isEmpty) {
      List? cached = dataStorage.caches[cacheKey];
      if (cached != null) {
        log.fine('Cached data found for $cacheKey $cached');
        _state = cached as List<T>;
      } else {
        log.fine('Cached data not found for $cacheKey');
      }
    }
    List<T> data = await load();
    dataStorage.caches[cacheKey] = data;
    return data;
  }
}

typedef LoadFunction<T> = Future<List<T>> Function();

class Combined2<T1, T2> {
  T1 state1;
  T2 state2;

  Combined2(this.state1, this.state2);
}

class Combined3<T1, T2, T3> {
  T1 state1;
  T2 state2;
  T3 state3;

  Combined3(this.state1, this.state2, this.state3);
}