import 'package:flutter/material.dart';

class Plugin extends StatelessWidget {
  const Plugin({super.key});

  @override
  Widget build(BuildContext context) {
    return Card(
      color: Theme.of(context).colorScheme.surface,
      child: Padding(
        padding: EdgeInsets.fromLTRB(24, 8, 24, 8),
        child: Column(
          children: [
            Row(
              children: [],
            )
          ],
        ),
      ),
    );
  }
}
