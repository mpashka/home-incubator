import 'dart:core';

import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:json_annotation/json_annotation.dart';

import '../misc/utils.dart';
import 'bloc_provider.dart';
import 'crud_ticket.dart';
import 'crud_visit.dart';
import 'session.dart';

part 'crud_user.g.dart';

class CrudUserBloc extends BlocBaseState<CrudEntityUser> {

  CrudUserBloc({required BlocProvider provider, String? name}): super(provider: provider, state: Injector().get<Session>().user, name: name);

  void updateUser(CrudEntityUser user) async {
    await backend.request('PUT', '/api/user', body: user);
    session.user.firstName = user.firstName;
    session.user.lastName = user.lastName;
    session.user.nickName = user.nickName;
    session.user.primaryImage = user.primaryImage;
    session.user.type = user.type;
    session.user.trainingTypes = user.trainingTypes;
    state = session.user;
  }

  void unlink(CrudEntityUserSocialNetwork network) async {
    await backend.request('DELETE', '/api/user/current/network/${network.id}');
    state.socialNetworks!.remove(network);
    state = session.user;
  }

  void link(LoginProvider provider, {SessionBloc? sessionBloc}) async {
    var user = await session.link(provider, sessionBloc: sessionBloc);
    if (user != null) {
      state = user;
    }
  }
}

class SelectedUserBloc extends BlocBaseState<CrudEntityUser> {
  SelectedUserBloc({required CrudEntityUser user, required BlocProvider provider, String? name}): super(state: user, provider: provider, name: name);
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

  @JsonKey(ignore: true)
  List<CrudEntityTicket>? tickets;
  @JsonKey(ignore: true)
  List<CrudEntityVisit>? visits;

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

  String get displayName {
    String result = '';
    String add(String? s) {
      if (s == null) return result;
      if (result.isNotEmpty) {
        result += ' ';
      }
      result += s;
      return result;
    }
    add(firstName);
    add(lastName);
    if (nickName == null) {
      return result;
    }
    return result.isEmpty ? nickName! : add('($nickName)');
  }

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is CrudEntityUser &&
          runtimeType == other.runtimeType &&
          userId == other.userId;

  @override
  int get hashCode => userId.hashCode;

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
  String? link;
  String? displayName;

  CrudEntityUserSocialNetwork({required this.networkName, required this.id, this.link, this.displayName});
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

