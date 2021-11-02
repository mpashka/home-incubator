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
    injector.map((i) => CrudUser(injector), isSingleton: true);
    injector.map((i) => CrudTraining(injector), isSingleton: true);
    injector.map((i) => CrudTicket(injector), isSingleton: true);
    injector.map((i) => CrudVisit(injector), isSingleton: true);
    return injector;
  }
}
