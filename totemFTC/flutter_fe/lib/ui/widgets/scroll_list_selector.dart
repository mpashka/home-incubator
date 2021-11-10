import 'dart:developer' as developer;

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class WheelListSelector<E> extends StatelessWidget {

  final NullableIndexedWidgetBuilder _childBuilder;
  final ValueChanged<int>? onSelectedItemChanged;
  final List<E> initialData;
  final Stream<List<E>> dataStream;

  const WheelListSelector({Key? key, required this.initialData, required this.dataStream,
    required NullableIndexedWidgetBuilder childBuilder, this.onSelectedItemChanged}) :
        _childBuilder = childBuilder,
        super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Stack(
      children: [
        Positioned.fill(
            child: StreamBuilder(
              initialData: initialData,
              stream: dataStream,
              builder: (BuildContext context, AsyncSnapshot<List<E>> items) {
                return ListWheelScrollView.useDelegate(
                    onSelectedItemChanged: (item) {
                      developer.log("Item selected: $item");
                      HapticFeedback.selectionClick();
                      var onSelectedItemChanged = this.onSelectedItemChanged;
                      if (onSelectedItemChanged != null) {
                        onSelectedItemChanged(item);
                      }
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
/*
                    children: [
                      for (var item in items.requireData) Text(item),
                    ]);
*/
                    childDelegate: ListWheelChildBuilderDelegate(
                      childCount: items.requireData.length,
                      builder: _childBuilder,
                    )
                );
              },
            )
        ),
        IgnorePointer(
          child: Center(
            child: ConstrainedBox(
              constraints: const BoxConstraints.expand(
                height: 30,
              ),
              child: Container(
                margin: const EdgeInsets.only(
                  left: 3,
                  right: 3,
                  // top: 3,
                  bottom: 10,
                ),
                color: theme.colorScheme.secondary.withOpacity(0.2),
              ),
            ),
          ),
        )
      ],
    );
  }
}