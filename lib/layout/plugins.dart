import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../platform/ecosed_plugin.dart';

class PluginPage extends StatefulWidget {
  const PluginPage({super.key});

  @override
  State<PluginPage> createState() => _PluginPageState();
}

class _PluginPageState extends State<PluginPage> {

  List _platformVersion = ['Unknown'];
  final _untitled3Plugin = EcosedPlugin();

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    List platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion =
          await _untitled3Plugin.getPluginList() ?? ['Unknown platform version'];
    } on PlatformException {
      platformVersion = ['Failed to get platform version.'];
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  List<Widget> getPlugin() {
    var p = _platformVersion.map((e) => ListTile(title: Text(e)));
    return p.toList();
  }

  @override
  Widget build(BuildContext context) {
    return ListView(children: getPlugin());
  }
}
