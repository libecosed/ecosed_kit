package io.ecosed.kit

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.Service
import android.app.UiModeManager
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.PermissionUtils
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.lifecycle.FlutterLifecycleAdapter
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import kotlin.system.exitProcess

class EcosedKitPlugin : Service(), FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware{
    //Shizuku.OnBinderReceivedListener, Shizuku.OnBinderDeadListener, Shizuku.OnRequestPermissionResultListener {




    private lateinit var mService: EcosedKitPlugin
    private lateinit var poem: ArrayList<String>

    override fun onCreate() {
        super.onCreate()
        mService = this@EcosedKitPlugin

        poem = arrayListOf()
        poem.add("不向焦虑与抑郁投降，这个世界终会有我们存在的地方。")
        poem.add("把喜欢的一切留在身边，这便是努力的意义。")
        poem.add("治愈、温暖，这就是我们最终幸福的结局。")
        poem.add("我有一个梦，也许有一天，灿烂的阳光能照进黑暗森林。")
        poem.add("如果必须要失去，那么不如一开始就不曾拥有。")
        poem.add("我们的终点就是与幸福同在。")
        poem.add("孤独的人不会伤害别人，只会不断地伤害自己罢了。")
        poem.add("如果你能记住我的名字，如果你们都能记住我的名字，也许我或者我们，终有一天能自由地生存着。")
        poem.add("对于所有生命来说，不会死亡的绝望，是最可怕的审判。")
        poem.add("我不曾活着，又何必害怕死亡。")


        val notification = buildNotification()
        //startForeground(notificationId, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onBind(intent: Intent): IBinder {
        return object : EcosedKit.Stub() {
            override fun getFrameworkVersion(): String = frameworkVersion()
            override fun getShizukuVersion(): String = shizukuVersion()
            override fun getChineseCale(): String = chineseCale()
            override fun getOnePoem(): String = onePoem()
            override fun isWatch(): Boolean = watch()
            override fun isUseDynamicColors(): Boolean = dynamicColors()
            override fun openDesktopSettings() = taskbarSettings()
            override fun openEcosedSettings() = ecosedSettings()
        }
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)

    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

    }

//    override fun onBinderReceived() {
//
//    }
//
//    override fun onBinderDead() {
//
//    }
//
//    override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
//
//    }

    private val mUserServiceArgs = Shizuku.UserServiceArgs(
        ComponentName(AppUtils.getAppPackageName(), UserService().javaClass.name)
    )
        .daemon(false)
        .processNameSuffix("service")
        .debuggable(BuildConfig.DEBUG)
        .version(AppUtils.getAppVersionCode())

    private fun check() {
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(0)
            }
        } catch (e: Exception) {
            if (e.javaClass == IllegalStateException().javaClass) {
                //PopTip.show("Shizuku未激活")
            }
        }
    }

    private fun frameworkVersion(): String {
        return AppUtils.getAppVersionName()
    }

    private fun shizukuVersion(): String {
        return try {
            "Shizuku ${Shizuku.getVersion()}"
        } catch (e: Exception) {
            Log.getStackTraceString(e)
        }
    }

    private fun chineseCale(): String {
        return ""
    }

    private fun onePoem(): String {
        return poem[(poem.indices).random()]
    }

    private fun watch(): Boolean {
        return getSystemService(
            UiModeManager::class.java
        ).currentModeType == Configuration.UI_MODE_TYPE_WATCH
    }

    private fun dynamicColors(): Boolean {
        return true
    }

    private fun taskbarSettings() {
        CoroutineScope(
            context = Dispatchers.Main
        ).launch {

        }
    }
    private fun ecosedSettings() {
        CoroutineScope(
            context = Dispatchers.Main
        ).launch {

        }
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionUtils.permission(Manifest.permission.POST_NOTIFICATIONS)
        }

        val notification = NotificationCompat.Builder(
            this@EcosedKitPlugin,
            ""
        )
            .setContentTitle(AppUtils.getAppName())
            .setContentText("服务正在运行")
        //    .setSmallIcon(R.drawable.baseline_keyboard_command_key_24)
            .build()

        notification.flags = Notification.FLAG_ONGOING_EVENT

        return notification
    }




    private lateinit var mMethodChannel: MethodChannel

    // 插件附加到引擎
    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        mMethodChannel = MethodChannel(flutterPluginBinding.binaryMessenger, channelName)
        mMethodChannel.setMethodCallHandler(this@EcosedKitPlugin)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        mMethodChannel.setMethodCallHandler(null)
    }

    // 调用方法
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) = engineUnit {
        onMethodCall(
            call = object : MethodCallProxy {

                override val methodProxy: String?
                    get() = call.method

                override val bundleProxy: Bundle?
                    get() {

                        return null
                    }
            },
            result = object : ResultProxy {
                override fun success(
                    resultProxy: Any?,
                ) = result.success(
                    resultProxy
                )

                override fun error(
                    errorCodeProxy: String,
                    errorMessageProxy: String?,
                    errorDetailsProxy: Any?,
                ) = result.error(
                    errorCodeProxy,
                    errorMessageProxy,
                    errorDetailsProxy
                )

                override fun notImplemented() = result.notImplemented()
            }
        )
    }


    override fun onAttachedToActivity(
        binding: ActivityPluginBinding,
    ) = engineUnit {
        getActivity(activity = binding.activity)
        getLifecycle(lifecycle = FlutterLifecycleAdapter.getActivityLifecycle(binding))
        attach()
    }

    override fun onDetachedFromActivityForConfigChanges() = Unit

    override fun onReattachedToActivityForConfigChanges(
        binding: ActivityPluginBinding,
    ) = engineUnit {
        getActivity(activity = binding.activity)
        getLifecycle(lifecycle = FlutterLifecycleAdapter.getActivityLifecycle(binding))
    }

    override fun onDetachedFromActivity() = Unit

    private interface FlutterPluginProxy {
        fun getActivity(activity: Activity)
        fun getLifecycle(lifecycle: Lifecycle)
        fun attach()
        fun onMethodCall(call: MethodCallProxy, result: ResultProxy)
    }

    private interface MethodCallProxy {
        val methodProxy: String?
        val bundleProxy: Bundle?
    }

    private interface ResultProxy {
        fun success(resultProxy: Any?)
        fun error(errorCodeProxy: String, errorMessageProxy: String?, errorDetailsProxy: Any?)
        fun notImplemented()
    }

    private val mFramework = object : FlutterPluginProxy {

        private val mEngine = object : EcosedEngine() {

            override val hybridPlugin: EcosedPlugin
                get() = mPlugin
        }

        private val mPlugin = object : EcosedPlugin() {

            override fun onEcosedMethodCall(call: EcosedMethodCall, result: EcosedResult) {
                super.onEcosedMethodCall(call, result)
                when(call.method) {
                    "" -> result.success("")
                    else -> result.notImplemented()
                }
            }

            override val channel: String
                get() = "framework"
        }

        override fun getActivity(activity: Activity) {
            mEngine.getActivity(activity = activity)
        }

        override fun getLifecycle(lifecycle: Lifecycle) {
            mEngine.getLifecycle(lifecycle = lifecycle)
        }

        override fun onMethodCall(call: MethodCallProxy, result: ResultProxy) {
            mEngine.onMethodCall(call = call, result = result)
        }

        override fun attach() {
            mEngine.attach()
        }
    }

    private fun engineUnit(
        content: FlutterPluginProxy.() -> Unit,
    ) {
        content.invoke(mFramework)
    }

    /**
     * 用于调用方法的接口.
     */
    private interface EcosedMethodCall {

        /**
         * 要调用的方法名.
         */
        val method: String?

        /**
         * 要传入的参数.
         */
        val bundle: Bundle?
    }


    /**
     * 作者: wyq0918dev
     * 仓库: https://github.com/ecosed/EcosedDroid
     * 时间: 2023/10/01
     * 描述: 基本插件
     * 文档: https://github.com/ecosed/EcosedDroid/blob/master/README.md
     */
    private abstract class EcosedPlugin : ContextWrapper(null) {

        /** 插件通道 */
        private lateinit var mPluginChannel: PluginChannel

        /** 是否调试模式 */
        private var mDebug: Boolean = false

        /**
         * 附加基本上下文
         */
        override fun attachBaseContext(base: Context?) {
            super.attachBaseContext(base)
        }

        /**
         * 插件添加时执行
         */
        open fun onEcosedAdded(binding: PluginBinding) {
            // 初始化插件通道
            mPluginChannel = PluginChannel(
                binding = binding, channel = channel
            )
            // 插件附加基本上下文
            attachBaseContext(
                base = mPluginChannel.getContext()
            )
            // 获取是否调试模式
            mDebug = mPluginChannel.isDebug()
            // 设置调用
            mPluginChannel.setMethodCallHandler(
                handler = this@EcosedPlugin
            )
        }

        /** 获取插件通道 */
        val getPluginChannel: PluginChannel
            get() = mPluginChannel

        /** 需要子类重写的通道名称 */
        abstract val channel: String

        /** 供子类使用的判断调试模式的接口 */
        protected val isDebug: Boolean = mDebug

        /**
         * 插件调用方法
         */
        open fun onEcosedMethodCall(call: EcosedMethodCall, result: EcosedResult) = Unit
    }


    /**
     * 方法调用结果回调.
     */
    private interface EcosedResult {

        /**
         * 处理成功结果.
         * @param result 处理成功结果,注意可能为空.
         */
        fun success(result: Any?)

        /**
         * 处理错误结果.
         * @param errorCode 错误代码.
         * @param errorMessage 错误消息,注意可能为空.
         * @param errorDetails 详细信息,注意可能为空.
         */
        fun error(
            errorCode: String,
            errorMessage: String?,
            errorDetails: Any?
        ): Nothing

        /**
         * 处理对未实现方法的调用.
         */
        fun notImplemented()
    }

    /**
     * 作者: wyq0918dev
     * 仓库: https://github.com/ecosed/plugin
     * 时间: 2023/09/02
     * 描述: 插件绑定器
     * 文档: https://github.com/ecosed/plugin/blob/master/README.md
     */
    private class PluginBinding(
        context: Context,
        debug: Boolean,
    ) {

        /** 应用程序全局上下文. */
        private val mContext: Context = context

        /** 是否调试模式. */
        private val mDebug: Boolean = debug

        /**
         * 获取上下文.
         * @return Context.
         */
        fun getContext(): Context {
            return mContext
        }

        /**
         * 是否调试模式.
         * @return Boolean.
         */
        fun isDebug(): Boolean {
            return mDebug
        }
    }

    /**
     * 作者: wyq0918dev
     * 仓库: https://github.com/ecosed/plugin
     * 时间: 2023/09/02
     * 描述: 插件通信通道
     * 文档: https://github.com/ecosed/plugin/blob/master/README.md
     */
    private class PluginChannel(binding: PluginBinding, channel: String) {

        /** 插件绑定器. */
        private var mBinding: PluginBinding = binding

        /** 插件通道. */
        private var mChannel: String = channel

        /** 方法调用处理接口. */
        private var mPlugin: EcosedPlugin? = null

        /** 方法名. */
        private var mMethod: String? = null

        /** 参数Bundle. */
        private var mBundle: Bundle? = null

        /** 返回结果. */
        private var mResult: Any? = null

        /**
         * 设置方法调用.
         * @param handler 执行方法时调用EcosedMethodCallHandler.
         */
        fun setMethodCallHandler(handler: EcosedPlugin) {
            mPlugin = handler
        }

        /**
         * 获取上下文.
         * @return Context.
         */
        fun getContext(): Context {
            return mBinding.getContext()
        }

        /**
         * 是否调试模式.
         * @return Boolean.
         */
        fun isDebug(): Boolean {
            return mBinding.isDebug()
        }

        /**
         * 获取通道.
         * @return 通道名称.
         */
        fun getChannel(): String {
            return mChannel
        }

        /**
         * 执行方法回调.
         * @param name 通道名称.
         * @param method 方法名称.
         * @return 方法执行后的返回值.
         */
        @Suppress("UNCHECKED_CAST")
        fun <T> execMethodCall(name: String, method: String?, bundle: Bundle?): T? {
            mMethod = method
            mBundle = bundle
            if (name == mChannel) {
                mPlugin?.onEcosedMethodCall(
                    call = call,
                    result = result
                )
            }
            return mResult as T?
        }

        /** 用于调用方法的接口. */
        private val call: EcosedMethodCall = object : EcosedMethodCall {

            /**
             * 要调用的方法名.
             */
            override val method: String?
                get() = mMethod

            /**
             * 要传入的参数.
             */
            override val bundle: Bundle?
                get() = mBundle
        }

        /** 方法调用结果回调. */
        private val result: EcosedResult = object : EcosedResult {

            /**
             * 处理成功结果.
             */
            override fun success(result: Any?) {
                mResult = result
            }

            /**
             * 处理错误结果.
             */
            override fun error(
                errorCode: String,
                errorMessage: String?,
                errorDetails: Any?
            ): Nothing = error(
                message = "错误代码:$errorCode\n" +
                        "错误消息:$errorMessage\n" +
                        "详细信息:$errorDetails"
            )

            /**
             * 处理对未实现方法的调用.
             */
            override fun notImplemented() {
                mResult = null
            }
        }

    }

    private abstract class EcosedEngine : EcosedPlugin(), FlutterPluginProxy, LifecycleOwner, DefaultLifecycleObserver {

        override val channel: String
            get() = channelName


        /** 插件绑定器. */
        private var mBinding: PluginBinding? = null

        /** 插件列表. */
        private var mPluginList: ArrayList<EcosedPlugin>? = null

        private val isBaseDebug: Boolean = AppUtils.isAppDebug()



        private var execResult: Any? = null

        private lateinit var mActivity: Activity
        private lateinit var mLifecycle: Lifecycle

        override fun getActivity(activity: Activity) {
            mActivity = activity
        }

        override fun getLifecycle(lifecycle: Lifecycle) {
            mLifecycle = lifecycle
        }

        override fun getLifecycle(): Lifecycle {
            return mLifecycle
        }

        open val hybridPlugin: EcosedPlugin? = null

        override fun onEcosedMethodCall(call: EcosedMethodCall, result: EcosedResult) {
            super.onEcosedMethodCall(call, result)
            when (call.method) {
                "" -> result.success("")
                else -> result.notImplemented()
            }
        }

        override fun onMethodCall(call: MethodCallProxy, result: ResultProxy) {
//        try {
//            val bundle = Bundle()
//            bundle.putString(
//                call.argument<String>("key"),
//                call.argument<String>("value")
//            )
//            execResult = execMethodCall<Any>(
//                channel = EcosedClient.mChannelName,
//                method = call.method,
//                bundle = bundle
//            )
//            if (execResult != null) {
//                result.success(execResult)
//            } else {
//                result.notImplemented()
//            }
//        } catch (e: Exception) {
//            result.error(tag, "", e)
//        }
        }

        /**
         * 将引擎附加到应用.
         */
        override fun attach() {

            when {
                (mPluginList == null) or (mBinding == null) -> apply {
                    // 引擎附加基本上下文
                    attachBaseContext(base = mActivity.baseContext)
                    lifecycle.addObserver(this@EcosedEngine)

                    // 初始化插件绑定器.
                    mBinding = PluginBinding(
                        context = this@EcosedEngine, debug = isBaseDebug
                    )
                    // 初始化插件列表.
                    mPluginList = arrayListOf()
                    // 添加所有插件.
                    arrayListOf(
                        this@EcosedEngine,
                        //TermPluxClient.build(),
                        hybridPlugin?: error("")
                    ).let { plugins ->
                        mBinding?.let { binding ->
                            plugins.forEach { plugin ->
                                plugin.apply {
                                    try {
                                        onEcosedAdded(binding = binding)
                                        if (isBaseDebug) {
                                            Log.d(tag, "插件${plugin.javaClass.name}已加载")
                                        }
                                    } catch (e: Exception) {
                                        if (isBaseDebug) {
                                            Log.e(tag, "插件添加失败!", e)
                                        }
                                    }
                                }
                            }
                        }.run {
                            plugins.forEach { plugin ->
                                mPluginList?.add(element = plugin)
                                if (isBaseDebug) {
                                    Log.d(tag, "插件${plugin.javaClass.name}已添加到插件列表")
                                }
                            }
                        }
                    }
                }

                else -> if (isBaseDebug) {
                    Log.e(tag, "请勿重复执行attach!")
                }
            }
        }

        // Flutter插件Activity生命周期
        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)


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

        /**
         * 调用插件代码的方法.
         * @param channel 要调用的插件的通道.
         * @param method 要调用的插件中的方法.
         * @param bundle 通过Bundle传递参数.
         * @return 返回方法执行后的返回值,类型为Any?.
         */
        fun <T> execMethodCall(
            channel: String,
            method: String,
            bundle: Bundle?,
        ): T? {
            var result: T? = null
            try {
                mPluginList?.forEach { plugin ->
                    plugin.getPluginChannel.let { pluginChannel ->
                        when (pluginChannel.getChannel()) {
                            channel -> {
                                result = pluginChannel.execMethodCall<T>(
                                    name = channel, method = method, bundle = bundle
                                )
                                if (isBaseDebug) {
                                    Log.d(
                                        tag,
                                        "插件代码调用成功!\n通道名称:${channel}.\n方法名称:${method}.\n返回结果:${result}."
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                if (isBaseDebug) {
                    Log.e(tag, "插件代码调用失败!", e)
                }
            }
            return result
        }

        companion object {

            /** 用于打印日志的标签. */
            private const val tag: String = "PluginEngine"

            private const val channelName: String = "termplux_engine"

        }
    }


    internal interface EcosedCallBack {

        /** 在服务绑定成功时回调 */
        fun onEcosedConnected()

        /** 在服务解绑或意外断开链接时回调 */
        fun onEcosedDisconnected()

        /** 在服务端服务未启动时绑定服务时回调 */
        fun onEcosedDead()

        /** 在未绑定服务状态下调用API时回调 */
        fun onEcosedUnbind()
    }

    private class TermPluxClient private constructor() : EcosedPlugin(), ServiceConnection, EcosedCallBack {

        override val channel: String
            get() = mChannelName

        private lateinit var mIntent: Intent

        /** 服务AIDL接口 */
        private var mFramework: EcosedKit? = null

        /** 服务绑定状态 */
        private var mIsBind: Boolean = false

        override fun onEcosedAdded(binding: PluginBinding) {
            super.onEcosedAdded(binding)
            mIntent = Intent(this@TermPluxClient, EcosedKitPlugin().javaClass)
            mIntent.action = action

            startService(mIntent)
            bindEcosed()

            Toast.makeText(this, "client", Toast.LENGTH_SHORT).show()
        }

        override fun onEcosedMethodCall(call: EcosedMethodCall, result: EcosedResult) {
            super.onEcosedMethodCall(call, result)
            when (call.method) {
                mMethodDebug -> result.success(isDebug)
                mMethodIsBinding -> result.success(mIsBind)
                mMethodStartService -> startService(mIntent)
                mMethodBindService -> bindEcosed()
                mMethodUnbindService -> unbindEcosed()

                "" -> result.success({

                })

                mMethodShizukuVersion -> result.success(getShizukuVersion())
                else -> result.notImplemented()
            }
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mFramework = EcosedKit.Stub.asInterface(service)
            when {
                mFramework != null -> {
                    mIsBind = true
                    callBack {
                        onEcosedConnected()
                    }
                }

                else -> if (isDebug) Log.e(
                    tag, "AIDL接口获取失败 - onServiceConnected"
                )
            }
            when {
                isDebug -> Log.i(
                    tag, "服务已连接 - onServiceConnected"
                )
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mIsBind = false
            mFramework = null
            unbindService(this)
            callBack {
                onEcosedDisconnected()
            }
            if (isDebug) {
                Log.i(tag, "服务意外断开连接 - onServiceDisconnected")
            }
        }

        override fun onBindingDied(name: ComponentName?) {
            super.onBindingDied(name)
        }

        override fun onNullBinding(name: ComponentName?) {
            super.onNullBinding(name)
            if (isDebug) {
                Log.e(tag, "Binder为空 - onNullBinding")
            }
        }

        override fun onEcosedConnected() {
            Toast.makeText(this@TermPluxClient, "onEcosedConnected", Toast.LENGTH_SHORT).show()
        }

        override fun onEcosedDisconnected() {
            Toast.makeText(this@TermPluxClient, "onEcosedDisconnected", Toast.LENGTH_SHORT).show()
        }

        override fun onEcosedDead() {
            Toast.makeText(this@TermPluxClient, "onEcosedDead", Toast.LENGTH_SHORT).show()
        }

        override fun onEcosedUnbind() {
            Toast.makeText(this@TermPluxClient, "onEcosedUnbind", Toast.LENGTH_SHORT).show()
        }

        /**
         * 绑定服务
         */
        private fun bindEcosed() {
            try {
                if (!mIsBind) {
                    bindService(
                        mIntent,
                        this@TermPluxClient,
                        Context.BIND_AUTO_CREATE
                    ).let {
                        if (!it) callBack {
                            onEcosedDead()
                        }
                    }
                }
            } catch (e: Exception) {
                if (isDebug) {
                    Log.e(tag, "bindEcosed", e)
                }
            }
        }

        /**
         * 解绑服务
         */
        private fun unbindEcosed() {
            try {
                if (mIsBind) {
                    unbindService(
                        this@TermPluxClient
                    ).run {
                        mIsBind = false
                        mFramework = null
                        callBack {
                            onEcosedDisconnected()
                        }
                        if (isDebug) {
                            Log.i(tag, "服务已断开连接 - onServiceDisconnected")
                        }
                    }
                }
            } catch (e: Exception) {
                if (isDebug) {
                    Log.e(tag, "unbindEcosed", e)
                }
            }
        }

        private fun getShizukuVersion(): String? {
            return try {
                if (mIsBind) {
                    if (mFramework != null) {
                        mFramework!!.shizukuVersion
                    } else {
                        callBack {
                            onEcosedUnbind()
                        }
                        null
                    }
                } else {
                    callBack {
                        onEcosedUnbind()
                    }
                    null
                }
            } catch (e: Exception) {
                if (isDebug) {
                    Log.e(tag, "getShizukuVersion", e)
                }
                null
            }
        }

        private fun callBack(
            function: EcosedCallBack.() -> Unit,
        ) {
            this@TermPluxClient.function()
        }

        companion object {
            internal const val mChannelName: String = "ecosed_droid"
            internal const val mMethodDebug: String = "is_binding"
            internal const val mMethodIsBinding: String = "is_debug"
            internal const val mMethodStartService: String = "start_service"
            internal const val mMethodBindService: String = "bind_service"
            internal const val mMethodUnbindService: String = "unbind_service"

            internal const val mMethodShizukuVersion: String = "shizuku_version"

            internal const val tag: String = "EcosedClient"
            internal const val action: String = "io.ecosed.droid.action"
            fun build(): TermPluxClient = TermPluxClient()
        }
    }

    private class UserService : IUserService.Stub() {

        override fun destroy() {
            exitProcess(0)
        }

        override fun exit() {
            exitProcess(0)
        }
    }

    private companion object {
        const val channelName = "ecosed_kit"
    }
}
