import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

part 'ecosed_kit_method_channel.dart';

part 'ecosed_kit_platform_interface.dart';

part 'ecosed_banner.dart';

class EcosedKit {
  Future<String?> getPlatformVersion() {
    return EcosedKitPlatform.instance.getPlatformVersion();
  }

  Widget _banner() {
    if (kDebugMode) {
      return const EcosedBanner();
    } else {
      return Container();
    }
  }

  Widget ecosedApp(Widget child) {
    return Stack(children: [child, _banner()]);
  }

  Widget manager() {
    return Container();
  }
}
