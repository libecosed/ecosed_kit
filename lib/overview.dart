import 'package:flutter/material.dart';

class OverviewPage extends StatefulWidget {
  const OverviewPage({super.key});

  @override
  State<OverviewPage> createState() => _OverviewPageState();
}

class _OverviewPageState extends State<OverviewPage> {
  @override
  Widget build(BuildContext context) {
    return const Column(
      children: [
        Padding(
          padding:
          EdgeInsets.only(top: 12, bottom: 6, left: 12, right: 12),
          child: Card(
            child: Padding(
              padding: EdgeInsets.all(24),
              child: Row(
                children: [
                  Icon(Icons.terminal_outlined),
                  Padding(
                      padding: EdgeInsets.only(left: 20),
                      child: Expanded(child: Column(
                        children: [
                          Text("正在运行"),
                          Spacer(flex: 4,), Text("版本")
                        ],
                      ))),
                  Icon(Icons.keyboard_arrow_right)
                ],
              ),
            ),
          ),
        )
      ],
    );
  }
}
