import 'dart:async';
import 'dart:developer' as developer;
import 'dart:io';

import 'package:flutter_fe/blocs/session.dart';
import 'package:logging/logging.dart';
import 'package:flutter_web_plugins/flutter_web_plugins.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';

import '../misc/configuration.dart';


class Initializer {
  Logger log = Logger('Initializer');

  Configuration _configuration;
  Session _session;
  final completer = Completer<void>();
  late final Future<void> future = completer.future;

  Initializer(Injector injector):
        _configuration = injector.get<Configuration>(),
        _session = injector.get<Session>();

  void init() async {
    Logger.root.level = Level.ALL;
    Logger.root.onRecord.listen((record) {
      developer.log(record.message, time: record.time, sequenceNumber: record.sequenceNumber, level: record.level.value,
          name: '${record.time} ${record.loggerName}', zone: record.zone, error: record.error, stackTrace: record.stackTrace);
      if (record.error != null) {
        developer.log('Error: ${record.error}', time: record.time, sequenceNumber: record.sequenceNumber, level: record.level.value,
          name: record.loggerName, zone: record.zone);
      }
      if (record.stackTrace != null) {
        developer.log(record.stackTrace.toString(), time: record.time, sequenceNumber: record.sequenceNumber, level: record.level.value,
          name: record.loggerName, zone: record.zone);
      }
    });
    log.info('Logger configured. Application initializing...');

    if (_configuration.isWeb()) {
      setUrlStrategy(PathUrlStrategy()); //  HashUrlStrategy
    }

    try {
      await _configuration.load();
      // todo configure logging
      log.info('Configuration loaded. SessionId: ${_configuration.sessionId}');
      if (_configuration.sessionId.isNotEmpty) {
        log.info('Loading user');
        await _session.loadUser();
      }
      completer.complete();
    } catch (e, s) {
      log.severe('Error loading configuration', e, s);
      _configuration.sessionId = '';
      completer.completeError(e, s);
    }
  }

  bool isInitialized() {
    return completer.isCompleted;
  }
}
