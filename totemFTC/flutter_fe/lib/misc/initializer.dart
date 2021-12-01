import 'dart:developer' as developer;
import 'dart:io';

import 'package:logging/logging.dart';
import 'package:flutter_web_plugins/flutter_web_plugins.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';

import '../misc/configuration.dart';


/**
    todo async initialization must be handled properly - e.g. show splash screen
    https://flutter.dev/docs/development/ui/advanced/splash-screen
    Also https://stefangaller.at/app-development/flutter/initialization-splash/ - FutureBuilder

 */
class Initializer {
  Logger log = Logger('Initializer');

  Configuration _configuration;
  bool _initialized = false;
  late Future<void> future;

  Initializer(Injector injector): _configuration = injector.get<Configuration>();

  void init() {
    Logger.root.level = Level.ALL;
    Logger.root.onRecord.listen((record) {
      // print(record);
      if (record.error != null) {
        print('Error: ${record.error}');
      }
      if (record.stackTrace != null) {
        print(record.stackTrace);
      }
      developer.log(record.message, time: record.time, sequenceNumber: record.sequenceNumber, level: record.level.value,
          name: record.loggerName, zone: record.zone, error: record.error, stackTrace: record.stackTrace);
    });
    log.info('Application initializing...');

    try {
      Future<List> configurationFuture = _configuration.load().whenComplete(() {
        // todo configure logging
        log.info('Configuration loaded');
      });
      future = Future.wait([configurationFuture]).then((value) {
        _initialized = true;
        log.info('Application initialized');
      }, onError: (error, stackTrace) => log.severe('Error loading configuration', error, stackTrace));
    } catch (e) {
      log.severe('Error loading configuration', e);
    }

    if (_configuration.isWeb()) {
      setUrlStrategy(PathUrlStrategy()); //  HashUrlStrategy
    }
  }

  bool isInitialized() {
    return _initialized;
  }
}
