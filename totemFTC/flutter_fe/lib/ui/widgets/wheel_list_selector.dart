import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:logging/logging.dart';

class WheelListSelector<E, B extends BlocBaseState<List<E>>> extends StatelessWidget {
  final Logger log;
  final WidgetBuilder<E> childBuilder;
  final WidgetBuilder? transformedChildBuilder;
  final WidgetValueChanged<E>? onSelectedItemChanged;
  final WidgetValueChanged? onSelectedTransformedItemChanged;
  final List Function(List<E>)? dataTransformer;
  final dynamic selectedItem;

  WheelListSelector({Key? key, required this.childBuilder, this.onSelectedItemChanged, this.onSelectedTransformedItemChanged,
    this.transformedChildBuilder, this.dataTransformer, this.selectedItem}) :
        log = Logger('WheelListSelector<${typeOf<E>()}, ${typeOf<B>()}>'),
        super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Stack(
      children: [
        Positioned.fill(
            child: BlocProvider.streamBuilder<List<E>, B>(builder: (ctx, data) {
              List workingData = dataTransformer != null ? dataTransformer!(data) : data;
              return ListWheelScrollView.useDelegate(
                  onSelectedItemChanged: (itemIndex) {
                    // log.finest("Item selected: $itemIndex");
                    HapticFeedback.selectionClick();
                    var item = workingData[itemIndex];
                    if (item is E) {
                      if (onSelectedItemChanged != null) {
                        onSelectedItemChanged!(context, itemIndex, item);
                      }
                    } else if (onSelectedTransformedItemChanged != null) {
                      onSelectedTransformedItemChanged!(context, itemIndex, item);
                    }
                  },
                  controller: FixedExtentScrollController(initialItem: selectedItem == null ? 0 : workingData.indexOf(selectedItem)),
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
                  childDelegate: ListWheelChildBuilderDelegate(
                    childCount: workingData.length,
                    builder: (context, index) =>
                        (workingData[index] is E ? childBuilder : transformedChildBuilder)!(context, index, workingData[index]),
                  )
              );
            })),
        IgnorePointer(
          child: Center(
            child: Container(
              constraints: const BoxConstraints.expand(height: 30,),
              margin: const EdgeInsets.only(
                left: 3,
                right: 3,
                // top: 3,
                bottom: 10,
              ),
              color: theme.colorScheme.secondary.withAlpha(50),
            ),
          ),),
      ],
    );
  }
}

typedef WidgetBuilder<E> = Widget? Function(BuildContext context, int index, E data);
typedef WidgetValueChanged<E> = Function(BuildContext context, int index, E data);
