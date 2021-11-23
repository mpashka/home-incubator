import 'package:flutter/material.dart';
import 'package:flutter_fe/misc/configuration.dart';
import 'package:flutter_fe/ui/screen_base.dart';
import 'package:flutter_simple_dependency_injection/injector.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:url_launcher/url_launcher.dart';

import 'drawer.dart';
import 'widgets/wheel_list_selector.dart';
import 'widgets/ui_visit.dart';

class ScreenAbout extends StatelessWidget {

  static const routeName = '/about';

  const ScreenAbout({Key? key}) : super(key: key);

  /**
   * https://t.me/Rino77s -> tg://resolve?domain=Rino77s
   */
  @override
  Widget build(BuildContext context) {
    var config = Injector().get<Configuration>();

    return UiScreen(body: Column(children: [
      Image.asset('images/logo.png', fit: BoxFit.contain),
      Row(children: [
        GestureDetector(child: Icon(Icons.public),
          onTap: () => launch('https://totemftc.ru/'),),
        GestureDetector(child: Icon(Icons.facebook),
          onTap: () => launch('https://www.facebook.com/hashtag/totemftc'),),
        GestureDetector(child: Icon(MdiIcons.instagram),
          onTap: () => launch('https://www.instagram.com/totemftc/'),),
        GestureDetector(child: Icon(Icons.phone),
          onTap: () => launch('tel://+${config.masterPhone()}'),),
        GestureDetector(child: FaIcon(FontAwesomeIcons.telegram),
          onTap: () => launch('https://t.me/${config.masterTelegram()}'),),
        GestureDetector(child: Icon(MdiIcons.whatsapp),
          onTap: () => launch('https://wa.me/${config.masterPhone()}'),),
        GestureDetector(child: Icon(Icons.alternate_email),
          onTap: () => launch('mailto:${config.masterEmail()}'),),
      ],),

      GestureDetector(child: Row(
        children: [Icon(Icons.phone), Text(config.masterPhoneUi()),],),
        onTap: () => launch('tel://+${config.masterPhone()}'),),
      GestureDetector(child: Row(
        children: [Icon(Icons.email), Text(config.masterEmail()),],),
        onTap: () => launch('mailto:${config.masterEmail()}'),),
      GestureDetector(child: Column(children: [
        Row(children: const [Icon(Icons.maps_home_work), Text('141401, Московская Область, г. Химки, ул. Академика Грушина, д. 8'),],),
      ]),
        onTap: () async {
          for (var mapUrl in ['https://www.google.com/maps/search/?api=1&query=55.910944,37.454814',
            'yandexmaps://maps.yandex.ru/?z=12&ll=55.910944,37.454814',
            'http://maps.apple.com/?ll=55.910944,37.454814']) {
            if (await canLaunch(mapUrl)) {
              await launch(mapUrl);
              break;
            }
          }
        },),

    Row(children: [Text('Обратная связь с разработчиками'),
    GestureDetector(child: Icon(MdiIcons.trello),
    onTap: () => launch('https://trello.com/b/dQ9YlBoq/todo')),
    GestureDetector(child: Icon(MdiIcons.github),
    onTap: () => launch('https://github.com/mpashka/home-incubator/issues')),
    GestureDetector(child: FaIcon(FontAwesomeIcons.telegram),
    onTap: () => launch('https://t.me/${config.devTelegram
    }()}'),),
        GestureDetector(child: Icon(MdiIcons.whatsapp),
          onTap: () => launch('https://wa.me/${config.devPhone()}'),),
      ],)
    ],),);
  }
}
