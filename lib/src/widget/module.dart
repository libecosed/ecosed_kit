import 'dart:convert';

import 'package:ecosed_kit/src/json.dart';
import 'package:flutter/material.dart';

class Plugin extends StatefulWidget {
  const Plugin({super.key, required this.json});

  final String json;

  @override
  State<Plugin> createState() => _PluginState();
}

class _PluginState extends State<Plugin> {
  void a() {}

  @override
  Widget build(BuildContext context) {
    return Padding(
        padding: const EdgeInsets.symmetric(vertical: 6, horizontal: 12),
        child: Card(
          color: Theme.of(context).colorScheme.surface,
          child: Padding(
            padding: const EdgeInsets.fromLTRB(24, 8, 24, 8),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                            PluginPerson.fromJson(jsonDecode(widget.json))
                                .title,
                            textAlign: TextAlign.start),
                        Text(
                            '版本:${PluginPerson.fromJson(jsonDecode(widget.json)).version}',
                            textAlign: TextAlign.start),
                        Text(
                            '作者:${PluginPerson.fromJson(jsonDecode(widget.json)).author}',
                            textAlign: TextAlign.start),
                      ],
                    ),
                    const Spacer(flex: 1),
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.end,
                      children: [Switch(value: true, onChanged: (value) {})],
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                Text(PluginPerson.fromJson(jsonDecode(widget.json)).description,
                    textAlign: TextAlign.start),
                const SizedBox(height: 16),
                const Divider(),
                Row(
                  children: [
                    const Text('内置模块'),
                    const Spacer(flex: 1),
                    TextButton(onPressed: a, child: const Text('设置')),
                    TextButton(onPressed: a, child: const Text('卸载'))
                  ],
                )
              ],
            ),
          ),
        ));
  }
}
