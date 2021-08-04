import 'dart:developer' as developer;
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class WheelListSelector extends StatelessWidget {

  List<String> items;

  WheelListSelector({required this.items});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Stack(
      children: [
        Positioned.fill(
            child: ListWheelScrollView(
                onSelectedItemChanged: (item) {
                  developer.log("Item selected: $item");
                  HapticFeedback.selectionClick();
                },
                itemExtent: 30,
                physics: const FixedExtentScrollPhysics(),
                squeeze: 1.45,
                diameterRatio: 1.07,
                perspective: 0.003,
                overAndUnderCenterOpacity: 0.447,
                // diameterRatio: 1.5,
                // perspective: 0.01,
                // scrollBehavior: ,
                // useMagnifier: true,
                // magnification: 1.2,
                children: [
                  for (var item in items) Text(item),
                ])
        ),
        IgnorePointer(
          child: Center(
            child: ConstrainedBox(
              constraints: BoxConstraints.expand(
                height: 30,
              ),
              child: Container(
                margin: EdgeInsets.only(
                  left: 3,
                  right: 3,
                  // top: 3,
                  bottom: 10,
                ),
                color: theme.accentColor.withOpacity(0.2),
              ),
            ),
          ),
        )
      ],
    );
  }
}