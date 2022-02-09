// ignore: avoid_web_libraries_in_flutter
import 'dart:html';

import 'package:flutter_fe/misc/configuration.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:flutter_web_plugins/flutter_web_plugins.dart';

void initPlatformSpecific() {
  setUrlStrategy(PathUrlStrategy()); //  HashUrlStrategy
  if (window.location.href.startsWith('https://totemftc.ga/')) {
    Configuration configuration = Injector().get<Configuration>();
    configuration.prodStr = 'production';
  }
}
