import 'package:ecosed_kit/src/widget/module.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../platform/ecosed_plugin.dart';

class PluginPage extends StatefulWidget {
  const PluginPage({super.key});

  @override
  State<PluginPage> createState() => _PluginPageState();
}

class _PluginPageState extends State<PluginPage> {
  List _pluginList = ['{"unknown":"unknown"}'];
  final _ecosedNative = EcosedNative();

  @override
  void initState() {
    super.initState();
    initPluginsState();
  }

  Future<void> initPluginsState() async {
    List pluginList;
    try {
      pluginList = await _ecosedNative.getPluginList() ?? ['Unknown plugins'];
    } on PlatformException {
      pluginList = ['Failed to get plugin list.'];
    }
    if (!mounted) return;
    setState(() {
      _pluginList = pluginList;
    });
  }

  @override
  Widget build(BuildContext context) {
    return ListView(children: _pluginList.map((e) => Plugin(json: e)).toList());
  }
}
