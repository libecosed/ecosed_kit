import 'ecosed_kit_platform.dart';

class EcosedPlugin {
  Future<String?> _getPlatformVersion() {
    return EcosedKitPlatform.instance.getPlatformVersion();
  }
}