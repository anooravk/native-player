package com.example.flios

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine


class MainActivity : FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        flutterEngine
            .platformViewsController
            .registry
            .registerViewFactory(
                "NativeUI",
                NativeViewFactory(flutterEngine.dartExecutor.binaryMessenger, this)
            )

    }

    override fun onStop() {
        super.onStop()

    }
}

