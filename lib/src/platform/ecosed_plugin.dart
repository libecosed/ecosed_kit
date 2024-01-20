import 'ecosed_kit_platform.dart';

class EcosedNative {
  Future<String?> getPlatformVersion() {
    return EcosedKitPlatform.instance.getPlatformVersion();
  }

  Future<List?> getPluginList() {
    return EcosedKitPlatform.instance.getPluginList();
  }
}