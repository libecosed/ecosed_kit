import 'dart:ffi';

import 'package:dynamic_color/dynamic_color.dart';
import 'package:ecosed_kit/overview.dart';
import 'package:flutter/material.dart';

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';

import 'ecosed_banner.dart';

class EcosedManager extends StatefulWidget {
  const EcosedManager({super.key});

  @override
  State<EcosedManager> createState() => _EcosedManagerState();
}

class _EcosedManagerState extends State<EcosedManager> {
  @override
  Widget build(BuildContext context) {
    return DynamicColorBuilder(
        builder: (ColorScheme? lightDynamic, ColorScheme? darkDynamic) {
      return MaterialApp(
        home: EcosedHome(),
        theme: ThemeData(colorScheme: lightDynamic, useMaterial3: true),
        darkTheme: ThemeData(colorScheme: darkDynamic, useMaterial3: true),
        themeMode: ThemeMode.system,
        debugShowCheckedModeBanner: false,
      );
    });
  }
}

class EcosedHome extends StatefulWidget {
  const EcosedHome({super.key});

  @override
  State<EcosedHome> createState() => _EcosedHomeState();
}

class _EcosedHomeState extends State<EcosedHome> {

  ValueNotifier<int> pageIndex = ValueNotifier(0);

  @override
  Widget build(BuildContext context) {
    return EcosedBanner(
      child: Scaffold(
        appBar: AppBar(title: const Text("EcosedKit")),
        body: ValueListenableBuilder(valueListenable: pageIndex, builder: (context, value, child) {
          return IndexedStack(
            index: value,
            children: [
              EcosedView(),
              Text("Manager"),
            ],
          );
        }),
        bottomNavigationBar: ValueListenableBuilder(valueListenable: pageIndex, builder:(context, value, child){
          return NavigationBar(
            selectedIndex: pageIndex.value,
            destinations: const [
              NavigationDestination(
                icon: Icon(Icons.info_outline),
                selectedIcon: Icon(Icons.info),
                label: "概览",
                tooltip: "查看插件工作状态",
                enabled: true,
              ),
              NavigationDestination(
                icon: Icon(Icons.manage_accounts_outlined),
                selectedIcon: Icon(Icons.manage_accounts),
                label: "管理",
                tooltip: "管理插件",
                enabled: true,
              )
            ],
            onDestinationSelected: (index) {
              pageIndex.value = index;
            },
          );
        })

      ),
    );
  }
}





class EcosedView extends StatefulWidget {
  const EcosedView({super.key});

  @override
  State<EcosedView> createState() => _EcosedViewState();
}

class _EcosedViewState extends State<EcosedView> {
  @override
  Widget build(BuildContext context) {
    // This is used in the platform side to register the view.
    const String viewType = 'ecosed_view';
    // Pass parameters to the platform side.
    const Map<String, dynamic> creationParams = <String, dynamic>{};

    return PlatformViewLink(
      viewType: viewType,
      surfaceFactory: (context, controller) {
        return AndroidViewSurface(
          controller: controller as AndroidViewController,
          gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{},
          hitTestBehavior: PlatformViewHitTestBehavior.opaque,
        );
      },
      onCreatePlatformView: (params) {
        return PlatformViewsService.initSurfaceAndroidView(
          id: params.id,
          viewType: viewType,
          layoutDirection: TextDirection.ltr,
          creationParams: creationParams,
          creationParamsCodec: const StandardMessageCodec(),
          onFocus: () {
            params.onFocusChanged(true);
          },
        )
          ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
          ..create();
      },
    );
  }
}


