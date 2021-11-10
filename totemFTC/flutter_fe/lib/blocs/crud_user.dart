import 'dart:async';
import 'dart:core';

import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:json_annotation/json_annotation.dart';
import 'package:logging/logging.dart';

import 'crud_api.dart';
import '../misc/utils.dart';

part 'crud_user.g.dart';


class CrudUser {
  static final Logger log = Logger('CrudUser');

  final CrudApi _api;

  CrudUser(Injector injector): _api = injector.get<CrudApi>();

  void clear() {
  }
}

class CrudUserBloc {

}

CrudEntityUser emptyUser = CrudEntityUser(userId: -1, type: CrudEntityUserType.guest);

@JsonSerializable(explicitToJson: true)
class CrudEntityUser implements Comparable<CrudEntityUser> {

  int userId;
  String? firstName;
  String? lastName;
  String? nickName;
  CrudEntityUserImage? primaryImage;
  CrudEntityUserType type;
  List<String>? trainingTypes;
  List<CrudEntityUserSocialNetwork>? socialNetworks;
  List<CrudEntityUserPhone>? phones;
  List<CrudEntityUserEmail>? emails;
  List<CrudEntityUserImage>? images;

  CrudEntityUser({
      required this.userId,
      this.firstName,
      this.lastName,
      this.nickName,
      this.primaryImage,
      required this.type,
      this.trainingTypes,
      this.socialNetworks,
      this.phones,
      this.emails,
      this.images});

  factory CrudEntityUser.fromJson(Map<String, dynamic> json) => _$CrudEntityUserFromJson(json);
  Map<String, dynamic> toJson() => _$CrudEntityUserToJson(this);

  @override
  int compareTo(CrudEntityUser other) {
    if (userId == other.userId) return 0;
    int result = compare(0, lastName, other.lastName);
    result = compare(result, firstName, other.firstName);
    result = compare(result, nickName, other.nickName);
    return compareId(result, userId, other.userId);
  }

}

@JsonSerializable()
class CrudEntityUserSocialNetwork {
  String networkName;
  String id;
  String link;

  CrudEntityUserSocialNetwork({required this.networkName, required this.id, required this.link});
  factory CrudEntityUserSocialNetwork.fromJson(Map<String, dynamic> json) => _$CrudEntityUserSocialNetworkFromJson(json);
  Map<String, dynamic> toJson() => _$CrudEntityUserSocialNetworkToJson(this);
}

@JsonSerializable()
class CrudEntityUserPhone {
  String phone;
  bool confirmed;

  CrudEntityUserPhone({required this.phone, required this.confirmed});
  factory CrudEntityUserPhone.fromJson(Map<String, dynamic> json) => _$CrudEntityUserPhoneFromJson(json);
  Map<String, dynamic> toJson() => _$CrudEntityUserPhoneToJson(this);
}

@JsonSerializable()
class CrudEntityUserEmail {
  String email;
  bool confirmed;

  CrudEntityUserEmail({required this.email, required this.confirmed});
  factory CrudEntityUserEmail.fromJson(Map<String, dynamic> json) => _$CrudEntityUserEmailFromJson(json);
  Map<String, dynamic> toJson() => _$CrudEntityUserEmailToJson(this);
}

@JsonSerializable()
class CrudEntityUserImage {
  int id;
  String contentType;

  CrudEntityUserImage({required this.id, required this.contentType});
  factory CrudEntityUserImage.fromJson(Map<String, dynamic> json) => _$CrudEntityUserImageFromJson(json);
  Map<String, dynamic> toJson() => _$CrudEntityUserImageToJson(this);
}

enum CrudEntityUserType {
  guest, user, trainer, admin
}

