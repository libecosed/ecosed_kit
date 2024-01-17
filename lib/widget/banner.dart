import 'package:flutter/material.dart';

class EcosedBanner extends StatelessWidget {
  const EcosedBanner({super.key, required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return Banner(
        message: 'EcosedKit',
        textDirection: TextDirection.ltr,
        location: BannerLocation.topEnd,
        color: Colors.pinkAccent,
        child: child);
  }
}
