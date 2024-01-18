import 'package:ecosed_kit/platform/ecosed_kit_platform.dart';
import 'package:ecosed_kit/platform/method_channel_ecosed_kit.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ecosed_kit/ecosed_kit.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockEcosedKitPlatform
    with MockPlatformInterfaceMixin
    implements EcosedKitPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');

  @override
  Future<List<String>?> getPluginList() {
    return Future.value([""]);
  }
}

void main() {
  final EcosedKitPlatform initialPlatform = EcosedKitPlatform.instance;

  test('$MethodChannelEcosedKit is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelEcosedKit>());
  });

  test('getPlatformVersion', () async {
    EcosedKit ecosedKitPlugin = EcosedKit();
    MockEcosedKitPlatform fakePlatform = MockEcosedKitPlatform();
    EcosedKitPlatform.instance = fakePlatform;

    expect('await ecosedKitPlugin.getPlatformVersion()', '42');
  });
}
