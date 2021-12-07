import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'package:flutter/services.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/misc/utils.dart';
import 'package:logging/logging.dart';

class UiWheelListSelector<E, B extends BlocBaseState<List<E>>> extends StatelessWidget {
  static const double itemHeight = 30;

  final Logger log;
  final WidgetBuilder<E> childBuilder;
  final WidgetBuilder? transformedChildBuilder;
  final WidgetValueChanged<E>? onSelectedItemChanged;
  final WidgetValueChanged? onSelectedTransformedItemChanged;
  final List Function(List<E>)? dataTransformer;
  final dynamic selectedItem;

  UiWheelListSelector({Key? key, required this.childBuilder, this.onSelectedItemChanged, this.onSelectedTransformedItemChanged,
    this.transformedChildBuilder, this.dataTransformer, this.selectedItem}) :
        log = Logger('WheelListSelector<${typeOf<E>()}, ${typeOf<B>()}>'),
        super(key: key);

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return SizedBox(height: itemHeight * 5, child: Stack(children: [
        Positioned.fill(
            child: BlocProvider.streamBuilder<List<E>, B>(builder: (ctx, data) {
              List workingData = dataTransformer != null ? dataTransformer!(data) : data;
              var i = selectedItem == null ? 0 : workingData.indexOf(selectedItem);
              log.fine('Items: ${data.length}. Selected item: $i - $selectedItem');
              // var controller = FixedExtentScrollController(initialItem: selectedItem == null ? 0 : (itemHeight * workingData.indexOf(selectedItem)).round());
              var controller = FixedExtentScrollController(initialItem: i);
              var scrollView = ListWheelScrollView.useDelegate(
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
                  controller: controller,
                  itemExtent: itemHeight,
                  physics: const FixedExtentScrollPhysics(),
                  squeeze: 1.4,
                  diameterRatio: 3,
                  perspective: 0.005,
                  magnification: 1.1,
                  overAndUnderCenterOpacity: 0.447,
                  // diameterRatio: 1.5,
                  // perspective: 0.01,
                  // scrollBehavior: ,
                  // useMagnifier: true,
                  // magnification: 1.2,
                  childDelegate: ListWheelChildBuilderDelegate(
                    childCount: workingData.length,
                    builder: (context, index) {
                      var item = workingData[index];
                      return item is E
                          ? childBuilder(context, index, workingData[index])
                          : transformedChildBuilder!(context, index, workingData[index]);
                    },
                  )
              );
/*
              if (i > 0) {
                WidgetsBinding.instance!.addPostFrameCallback((duration) {
                  log.fine('Post frame callback() - jump to $i');
                  controller.jumpToItem(i);
                  // controller.animateToItem(i, duration: Duration(milliseconds: 100), curve: Curves.bounceInOut);
                });
              }
*/
              return scrollView;
            })),
        IgnorePointer(child: Center(child: Container(
          // alignment: Alignment.center,
              constraints: const BoxConstraints.expand(height: itemHeight,),
              // margin: const EdgeInsets.only(left: 3, right: 3,),
              color: theme.colorScheme.primaryVariant.withAlpha(80),
          ),),),
      ],
    ),);
  }
}

typedef WidgetBuilder<E> = Widget? Function(BuildContext context, int index, E data);
typedef WidgetValueChanged<E> = Function(BuildContext context, int index, E data);
