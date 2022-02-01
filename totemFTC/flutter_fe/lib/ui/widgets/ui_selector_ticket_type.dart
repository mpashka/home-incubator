import 'package:flutter/material.dart';
import 'package:flutter_fe/blocs/bloc_provider.dart';
import 'package:flutter_fe/blocs/crud_ticket.dart';
import 'package:flutter_fe/blocs/crud_training.dart';
import 'package:flutter_fe/misc/utils.dart';

import 'ui_wheel_list_selector.dart';

class UiSelectorTicketTypeDialog extends StatefulWidget {

  final String title;
  final List<CrudEntityTicketType>? types;
  final TicketTypeFilter? ticketTypeFilter;

  UiSelectorTicketTypeDialog({required this.title, this.types, this.ticketTypeFilter});

  @override
  State createState() => UiSelectorTicketTypeDialogState();

  Future<CrudEntityTicketType?> selectTicketType(BuildContext context) async {
    return await showDialog(context: context, builder: (c) => this);
  }
}

class UiSelectorTicketTypeDialogState extends BlocProvider<UiSelectorTicketTypeDialog> {

  late final CrudTicketTypeFilteredBloc ticketTypeFilteredBloc;
  late final SelectedTicketTypeBloc selectedTicketTypeBloc;

  @override
  void initState() {
    super.initState();
    ticketTypeFilteredBloc = CrudTicketTypeFilteredBloc(provider: this)
      ..loadTicketTypes();
    selectedTicketTypeBloc = SelectedTicketTypeBloc(provider: this);
  }

  @override
  Widget build(BuildContext context) {
    return SimpleDialog(
        title: Text(widget.title),
        elevation: 5,
        children: [
          SizedBox(
              height: 150,
              child: UiWheelListSelector<CrudEntityTicketType, CrudTicketTypeFilteredBloc>(
                childBuilder: (context, index, ticketType) => Center(child: Text(ticketString(ticketType)),),
                onSelectedItemChanged: (ctx, i, data) {
                  selectedTicketTypeBloc.state = data;
                },)
          ),
          Row(children: [
            BlocProvider.streamBuilder<CrudEntityTicketType?, SelectedTicketTypeBloc>(builder: (ctx, selectedTicketType) => TextButton(
              child: Text('Ok'),
              onPressed: selectedTicketType != null ? () => Navigator.pop(context, selectedTicketType) : null,
            ),),
            TextButton(
              child: const Text('Cancel'),
              onPressed: () => Navigator.pop(context),
            ),
          ],),
        ]);
  }
  
  String ticketString(CrudEntityTicketType ticketType) {
    // String trainingTypes = ticketType.trainingTypes?.map((t) => t.trainingName).join(', ') ?? '';
    // return '${ticketType.name}, ${ticketType.days} дней, ${ticketType.cost} рублей, $trainingTypes';
    return ticketType.name;
  }
}
