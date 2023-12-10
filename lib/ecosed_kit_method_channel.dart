part of 'ecosed_kit.dart';

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
