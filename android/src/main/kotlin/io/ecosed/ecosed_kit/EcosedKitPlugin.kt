package io.ecosed.ecosed_kit

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.lifecycle.FlutterLifecycleAdapter
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class EcosedKitPlugin : ContextWrapper(null), FlutterPlugin, MethodChannel.MethodCallHandler,
    ActivityAware, LifecycleOwner, DefaultLifecycleObserver {

    private lateinit var mChannel: MethodChannel
    private var mActivity: Activity? = null
    private var mLifecycle: Lifecycle? = null

    // 附加基本上下文
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    // 插件附加到引擎
    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        mChannel = MethodChannel(flutterPluginBinding.binaryMessenger, channelName)
        mChannel.setMethodCallHandler(this@EcosedKitPlugin)
    }

    // 调用方法
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
            "openMenu" -> {}
            "closeMenu" -> {}
            else -> result.notImplemented()
        }
    }

    // 插件与引擎分离
    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        mChannel.setMethodCallHandler(null)
    }

    // 首次绑定到activity
    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        mActivity = binding.activity
        mLifecycle = FlutterLifecycleAdapter.getActivityLifecycle(binding)
        lifecycle.addObserver(this@EcosedKitPlugin)
    }

    // 由于某些原因导致暂时解绑
    override fun onDetachedFromActivityForConfigChanges() {
        mActivity = null
        mLifecycle = null
        lifecycle.removeObserver(this@EcosedKitPlugin)
    }

    // 恢复绑定
    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        mActivity = binding.activity
        mLifecycle = FlutterLifecycleAdapter.getActivityLifecycle(binding)
        lifecycle.addObserver(this@EcosedKitPlugin)
    }

    // 解绑
    override fun onDetachedFromActivity() {
        mActivity = null
        mLifecycle = null
        lifecycle.removeObserver(this@EcosedKitPlugin)
    }

    override fun getLifecycle(): Lifecycle {
        return mLifecycle ?: error("error: lifecycle is null!")
    }

    override fun onCreate(owner: LifecycleOwner) {
        attachBaseContext(base = mActivity?.baseContext)
        super.onCreate(owner)
        Toast.makeText(
            this@EcosedKitPlugin,
            "test",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
    }

    companion object {
        const val channelName = "ecosed_kit"
    }
}
