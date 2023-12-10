import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:ecosed_kit/ecosed_kit.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  final _ecosedKitPlugin = EcosedKit();

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    String platformVersion;
    try {
      platformVersion = await _ecosedKitPlugin.getPlatformVersion() ??
          'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }
    if (!mounted) return;
    setState(() {
      _platformVersion = platformVersion;
    });
  }

  Widget ecosedApp(Widget child) {
    return _ecosedKitPlugin.ecosedApp(child);
  }


  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        home: ecosedApp(Scaffold(
      appBar: AppBar(
        title: const Text('EcosedKit Example'),
      ),
      body: Center(
        child: Column(
          children: [
            MaterialButton(onPressed: _ecosedKitPlugin.openMenu, child: const Text('打开菜单')),
            MaterialButton(onPressed: _ecosedKitPlugin.closeMenu, child: const Text('关闭菜单'))
          ],
        )
      ),
    )));
  }
}
