package io.ecosed.kit

import android.os.Bundle
import io.flutter.embedding.android.FlutterFragment

class MyActivity : EcosedKitPlugin() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateFlutter(): FlutterFragment {
        return super.onCreateFlutter()
    }

    override fun isExtended(): Boolean {
        super.isExtended()
        return true
    }
}