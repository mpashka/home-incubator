import 'dart:async';
import 'dart:developer' as developer;

import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:logging/logging.dart';

import '../misc/configuration.dart';
import 'init_helper.dart' if (dart.library.js) 'init_helper_js.dart' as init_helper;


class Initializer {
  Logger log = Logger('Initializer');

  Configuration _configuration;
  Session _session;
  Completer? completer;
  bool isInitialized = false;

  Initializer(Injector injector):
        _configuration = injector.get<Configuration>(),
        _session = injector.get<Session>();

  void initLogger() {
    Logger.root.level = Level.ALL;
    Logger.root.onRecord.listen((record) {
      developer.log(record.message, time: record.time, sequenceNumber: record.sequenceNumber, level: record.level.value,
          name: '${record.time} ${record.loggerName}', zone: record.zone, error: record.error, stackTrace: record.stackTrace);
      if (_configuration.isWeb && record.error != null) {
        print(record.error);
      }
      if (_configuration.isWeb && record.stackTrace != null) {
        print(record.stackTrace);
      }
    });
    log.info('Logger configured.');
  }

  Future<void> initFuture() {
    if (isInitialized) {
      return Future.value(null);
    }

    Completer? completer = this.completer;
    if (completer == null) {
      completer = Completer<void>();
      init(completer);
    }
    return completer.future;
  }

  void init(Completer completer) async {
    log.info('Application initializing...');
    this.completer = completer;

    init_helper.initPlatformSpecific();
    
    try {
      await _configuration.load();
      // todo configure logging
      log.info('Configuration loaded. SessionId: ${_configuration.sessionId}');
      if (_configuration.sessionId.isNotEmpty) {
        log.info('Loading user');
        await _session.loadUser();
      }
      completer.complete();
      isInitialized = true;
      log.info('Application initialized');
    } catch (e, s) {
      log.severe('Error loading configuration', e, s);
      _configuration.sessionId = '';
      completer.completeError(e, s);
    } finally {
      this.completer = null;
    }
  }
}
