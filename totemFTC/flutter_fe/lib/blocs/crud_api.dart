import 'dart:async';
import 'dart:convert';

import 'package:http/http.dart' as http;

import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:intl/intl.dart';
import 'package:logging/logging.dart';

import '../misc/configuration.dart';

class CrudApi {
  static final Logger log = Logger('Backend');

  final Configuration _configuration;

  CrudApi(Injector injector): _configuration = injector.get<Configuration>();

  /// This can be either Map or List
  Future<dynamic> get(String apiUri) async {
    try {
      Uri uri = Uri.parse('${_configuration.backendUrl()}$apiUri');
      http.Response userResponse = await http.get(uri, headers: {'Authorization': 'Bearer ${_configuration.sessionId}'});
      if (userResponse.statusCode != 200) {
        log.severe('Backend error get $uri ${userResponse.statusCode}\n${userResponse.body}');
        throw ApiException('Server error', userResponse.body);
      }
      log.fine('Response received $uri ${userResponse.statusCode}\n${userResponse.body}');
      var decoded = jsonDecode(userResponse.body);
      return decoded;
    } catch (e,s) {
      log.severe('Internal error get $apiUri', e, s);
      throw ApiException('Internal error', e.toString());
    }
  }
}

class ApiException implements Exception {
  final String title;
  final String message;

  const ApiException(this.title, this.message);
}

final _dateTimeFormatter = DateFormat('yyyy-MM-dd HH:mm');
final _dateFormatter = DateFormat('yyyy-MM-dd');
DateTime dateTimeFromJson(String date) => _dateTimeFormatter.parse(date);
String dateTimeToJson(DateTime date) => _dateTimeFormatter.format(date);
DateTime? dateTimeFromJson_(String? date) => date != null ? _dateTimeFormatter.parse(date) : null;
String? dateTimeToJson_(DateTime? date) => date != null ? _dateTimeFormatter.format(date) : null;
DateTime dateFromJson(String date) => _dateFormatter.parse(date);
String dateToJson(DateTime date) => _dateFormatter.format(date);

// @JsonKey(name: 'registration_date_millis')
// @JsonKey(defaultValue: false)
// @JsonKey(required: true)
// @JsonKey(ignore: true)

// https://flutter.dev/docs/development/data-and-backend/json
// flutter pub run build_runner build
// flutter pub run build_runner watch