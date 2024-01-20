import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';

import 'ecosed_kit_platform.dart';

class MethodChannelEcosedKit extends EcosedKitPlatform {
  @visibleForTesting
  final methodChannel = const MethodChannel('ecosed_kit');

  @override
  Future<String?> getPlatformVersion() async {
    Map<String, dynamic> map = {"channel": "ecosed_client"};
    final version =
    await methodChannel.invokeMethod<String>('getPlatformVersion', map);
    return version;
  }

  @override
  Future<List?> getPluginList() async {
    Map<String, dynamic> map = {"channel": "ecosed_engine"};
    final list = await methodChannel.invokeMethod<List>('getPlugins', map);
    return list;
  }
}