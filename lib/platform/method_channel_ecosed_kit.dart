import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';

import 'ecosed_kit_platform.dart';

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