import 'package:flutter/material.dart';
import 'dart:math' as math;

// https://flutter.dev/docs/cookbook/effects/expandable-fab
@immutable
class FabMulti extends StatefulWidget {
  const FabMulti({
    Key? key,
    this.initialOpen,
    required this.distance,
    required this.children,
  }) : super(key: key);

  final bool? initialOpen;
  final double distance;
  final List<Widget> children;

  @override
  _FabMultiState createState() => _FabMultiState();
}

@immutable
class RoundedButton extends StatelessWidget {
  const RoundedButton({
    Key? key,
    this.onPressed,
    required this.text,
  }) : super(key: key);

  final VoidCallback? onPressed;
  final String text;

  @override
  Widget build(BuildContext context) {
    return ElevatedButton(
      onPressed: onPressed,
      child: Text('Button'),
      style: ElevatedButton.styleFrom(shape: StadiumBorder()),
    );
  }
}

class _FabMultiState extends State<FabMulti> {
  bool _open = false;

  @override
  void initState() {
    super.initState();
    _open = widget.initialOpen ?? false;
  }

  void _toggle() {
    setState(() {
      _open = !_open;
    });
  }

  @override
  Widget build(BuildContext context) {
    return SizedBox.expand(
      child: Stack(
        alignment: Alignment.bottomRight,
        clipBehavior: Clip.none,
        children: [
          _buildTap(),
          ..._buildActionButtons(),
        ],
      ),
    );
  }

  Widget _buildTap() {
    return FloatingActionButton(
      onPressed: _toggle,
      child: Icon(_open ? Icons.close : Icons.add),
    );
  }

  List<Widget> _buildActionButtons() {
    final children = <Widget>[];
    if (!_open) return children;
    final count = widget.children.length;
    final step = 90.0 / (count - 1);
    for (var i = 0, angleInDegrees = 0.0; i < count; i++, angleInDegrees += step) {
      final offset = Offset.fromDirection(
        angleInDegrees * (math.pi / 180.0),
        widget.distance,
      );

      children.add(
        Positioned(
          right: 4.0 + offset.dx,
          bottom: 4.0 + offset.dy,
          child: widget.children[i],
        ),
      );
    }
    return children;
  }
}
