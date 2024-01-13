library ecosed_kit;

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class EcosedKit {
  Future<String?> _getPlatformVersion() {
    return EcosedKitPlatform.instance.getPlatformVersion();
  }

  Widget manager() {
    return EcosedManager();
  }
}

abstract class EcosedKitPlatform extends PlatformInterface {
  EcosedKitPlatform() : super(token: _token);

  static final Object _token = Object();

  static EcosedKitPlatform _instance = MethodChannelEcosedKit();

  static EcosedKitPlatform get instance => _instance;

  static set instance(EcosedKitPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}

class MethodChannelEcosedKit extends EcosedKitPlatform {
  @visibleForTesting
  final methodChannel = const MethodChannel('ecosed_kit');

  @override
  Future<String?> getPlatformVersion() async {
    final version =
        await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}

class EcosedBanner extends StatelessWidget {
  const EcosedBanner({super.key, required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Banner(
        message: 'EcosedApp',
        textDirection: TextDirection.ltr,
        location: BannerLocation.topEnd,
        color: Colors.pinkAccent,
        child: child);
  }
}

class EcosedManager extends StatefulWidget {
  const EcosedManager({super.key});

  @override
  State<EcosedManager> createState() => _EcosedManagerState();
}

class _EcosedManagerState extends State<EcosedManager> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: EcosedBanner(
        child: Scaffold(
          appBar: AppBar(
              title: const Text("EcosedKit")
          ),
          body: Center(
            child: Text("Manager"),
          ),
          bottomNavigationBar: NavigationBar(
            destinations: [
              NavigationDestination(icon: Icon(Icons.info), label: "概览"),
              NavigationDestination(icon: Icon(Icons.manage_accounts), label: "管理")
            ],
          ),
        ),
      ),
      debugShowCheckedModeBanner: false,
    );
  }
}
