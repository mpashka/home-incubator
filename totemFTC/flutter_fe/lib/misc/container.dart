import 'package:flutter_fe/blocs/data_storage.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';

import '../blocs/crud_api.dart';
import '../blocs/session.dart';
import '../blocs/crud_user.dart';
import '../blocs/crud_training.dart';
import '../blocs/crud_ticket.dart';
import '../blocs/crud_visit.dart';
import 'configuration.dart';

import 'initializer.dart';

class ModuleContainer {
  Injector initialise(Injector injector) {
    injector.map((i) => Configuration(), isSingleton: true);
    injector.map((i) => Initializer(injector), isSingleton: true);
    injector.map((i) => Session(injector), isSingleton: true);
    injector.map((i) => CrudApi(injector), isSingleton: true);
    injector.map((i) => DataStorage(), isSingleton: true);
    return injector;
  }
}
