part of 'ecosed_kit.dart';

class EcosedBanner extends StatelessWidget {
  const EcosedBanner({super.key});

  @override
  Widget build(BuildContext context) {
    return const Banner(
        message: 'EcosedApp',
        textDirection: TextDirection.ltr,
        location: BannerLocation.topStart,
        color: Colors.pinkAccent);
  }
}
