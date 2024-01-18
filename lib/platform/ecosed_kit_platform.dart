import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'method_channel_ecosed_kit.dart';

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

  Future<List?> getPluginList() {
    throw UnimplementedError('getPluginList() has not been implemented.');
  }
}
