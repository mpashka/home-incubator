import 'dart:async';
import 'dart:convert';

import 'package:http/http.dart' as http;

import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:logging/logging.dart';

import '../misc/utils.dart';
import '../misc/configuration.dart';

class CrudApi {
  static final Logger log = Logger('Backend');

  final Configuration _configuration;
  final http.Client _httpClient = http.Client();

  CrudApi(Injector injector): _configuration = injector.get<Configuration>();

  void close() {
    _httpClient.close();
  }

  Future<http.Response> request(String method, String apiUri, {Map<String, dynamic>? params, Object? body, bool auth=true}) async {
    try {
      String uriStr = '${_configuration.backendUrl()}$apiUri';
      if (params != null) {
        var delimiter = apiUri.contains('?') ? '&' : '?';
        params.forEach((key, value) {
          uriStr += '$delimiter$key=${Uri.encodeComponent(value.toString())}';
          delimiter = '&';
        });
      }
      // log.finest('Url: $uriStr, Params: $params');
      http.Request request = http.Request(method, Uri.parse(uriStr));
      request.encoding = utf8;
      if (body != null) {
        request.headers['Content-Type'] = 'application/json;charset=utf-8';
        request.bodyBytes = utf8.encode(jsonEncode(body));
      }
      if (auth) {
        request.headers['Authorization'] = 'Bearer ${_configuration.sessionId}';
      }
      // Accept: application/json
      var responseStream = await _httpClient.send(request);
      log.fine('Received streamed response: ${responseStream.statusCode}');
      http.Response response = await http.Response.fromStream(responseStream);
      if (response.statusCode == 403) {
        // Forbidden. Clear session
        log.info('Access denied. Clear session "${_configuration.sessionId}" $method $apiUri ${response.statusCode}');
        _configuration.sessionId = '';
        throw ApiException('Access denied', 'Please relogin');
      } if (response.statusCode != 200 && response.statusCode != 204) {
        log.info('Backend error $method $apiUri ${response.statusCode}');
        log.finer(response.body);
        throw ApiException('Backend error ${response.statusCode}', response.body);
      }
      log.fine('Response received $apiUri ${response.statusCode}');
      log.finer(response.body);
      return response;
    } on ApiException {
      rethrow;
    } catch (e,s) {
      log.severe('Internal error ${_configuration.backendUrl()} $method $apiUri', e, s);
      throw ApiException('Internal error', e.toString());
    }
  }

  Future<dynamic> requestJson(String method, String apiUri, {Map<String, dynamic>? params, Object? body, bool auth=true}) async {
    try {
      var responseBytes = (await request(method, apiUri, body: body, params: params, auth: auth)).bodyBytes;
      if (responseBytes.isEmpty) {
        log.fine('Response is empty');
        return null;
      }
      var decoded = jsonDecode(utf8.decode(responseBytes));
      log.fine('Response decoded $decoded');
      return decoded;
    } on ApiException {
      rethrow;
    } catch (e,s) {
      log.severe('Internal error getJson $apiUri', e, s);
      throw ApiException('Internal error', e.toString());
    }
  }


}

class ApiException implements Exception {
  final String title;
  final String message;

  const ApiException(this.title, this.message);

  @override
  String toString() {
    return '$title\n$message';
  }
}

DateTime dateTimeFromJson(String date) => dateTimeFormat.parse(date);
String dateTimeToJson(DateTime date) => dateTimeFormat.format(date);
DateTime? dateTimeFromJson_(String? date) => date != null ? dateTimeFormat.parse(date) : null;
String? dateTimeToJson_(DateTime? date) => date != null ? dateTimeFormat.format(date) : null;
DateTime dateFromJson(String date) => dateFormat.parse(date);
String dateToJson(DateTime date) => dateFormat.format(date);
DateTime? dateFromJson_(String? date) => date != null ? dateFormat.parse(date) : null;
String? dateToJson_(DateTime? date) => date != null ? dateFormat.format(date) : null;
