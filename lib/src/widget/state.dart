import 'package:flutter/material.dart';

class StateCard extends StatelessWidget {
  const StateCard({super.key});

  @override
  Widget build(BuildContext context) {
    return Card(
      color: Theme.of(context).colorScheme.secondaryContainer,
      child: Padding(
          padding: EdgeInsets.all(24),
          child: Row(
            children: [
              Icon(Icons.keyboard_command_key),
              Padding(
                padding: EdgeInsets.only(left: 24),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      "EcosedKit",
                      textAlign: TextAlign.left,
                    ),
                    Text(
                      "版本: 1.0",
                      textAlign: TextAlign.left,
                    )
                  ],
                ),
              )
            ],
          )),
    );
  }
}
