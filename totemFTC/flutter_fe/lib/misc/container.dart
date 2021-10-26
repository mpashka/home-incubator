import 'package:flutter_fe/blocs/session.dart';
import 'package:flutter_fe/misc/configuration.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';

import 'initializer.dart';

class ModuleContainer {
  Injector initialise(Injector injector) {
    injector.map((i) => Configuration(), isSingleton: true);
    injector.map((i) => Initializer(injector), isSingleton: true);
    injector.map((i) => Session(injector), isSingleton: true);
    return injector;
  }
}
