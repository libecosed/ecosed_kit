package io.ecosed.kit

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
import android.view.View
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
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import kotlin.system.exitProcess

class EcosedKitPlugin : Service(), FlutterPlugin, MethodChannel.MethodCallHandler, ActivityAware,
    LifecycleOwner, DefaultLifecycleObserver, ServiceConnection {


    /** Flutter插件方法通道 */
    private lateinit var mMethodChannel: MethodChannel

    /** 插件绑定器. */
    private var mBinding: PluginBinding? = null

    /** 插件列表. */
    private var mPluginList: ArrayList<EcosedPlugin>? = null

    /** Activity */
    private lateinit var mActivity: Activity

    /** 生命周期 */
    private lateinit var mLifecycle: Lifecycle

    private val mBaseDebug: Boolean = AppUtils.isAppDebug()

    private var mFullDebug: Boolean = false

    private var mExecResult: Any? = null

    private lateinit var mEcosedServicesIntent: Intent

    /** 服务AIDL接口 */
    private var mAIDL: EcosedKit? = null
    private var mIUserService: IUserService? = null

    /** 服务绑定状态 */
    private var mIsBind: Boolean = false


    private lateinit var poem: ArrayList<String>


    private val mUserServiceArgs = Shizuku.UserServiceArgs(
        ComponentName(AppUtils.getAppPackageName(), UserService().javaClass.name)
    )
        .daemon(false)
        .processNameSuffix("service")
        .debuggable(mFullDebug)
        .version(AppUtils.getAppVersionCode())

    override fun onCreate() {
        super<Service>.onCreate()
        // 添加Shizuku监听
        Shizuku.addBinderReceivedListener(mService)
        Shizuku.addBinderDeadListener(mService)
        Shizuku.addRequestPermissionResultListener(mService)


        check()

        // 绑定Shizuku服务
        //Shizuku.bindUserService(mUserServiceArgs, this@EcosedKitPlugin)

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannel,
                AppUtils.getAppName(),
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionUtils.permission(Manifest.permission.POST_NOTIFICATIONS).request()
        }

        val notification = buildNotification()
        startForeground(notificationId, notification)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        return serviceUnit {
            return@serviceUnit getBinder(
                intent = intent
            )
        }
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super<Service>.onDestroy()
        // 移除Shizuku监听
        Shizuku.removeBinderDeadListener(mService)
        Shizuku.removeBinderReceivedListener(mService)
        Shizuku.removeRequestPermissionResultListener(mService)

        // 解绑Shizuku服务
        Shizuku.unbindUserService(mUserServiceArgs, this@EcosedKitPlugin, true)
    }

    // 插件附加到引擎
    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        mMethodChannel = MethodChannel(flutterPluginBinding.binaryMessenger, flutterChannelName)
        mMethodChannel.setMethodCallHandler(this@EcosedKitPlugin)
        flutterPluginBinding.platformViewRegistry.registerViewFactory(
            viewTypeId, object : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

                override fun create(
                    context: Context?, viewId: Int, args: Any?
                ): PlatformView = object : PlatformView {

                    override fun getView(): View = frameworkUnit {
                        return@frameworkUnit getView(
                            context = context, viewId = viewId, args = args
                        )
                    }

                    override fun dispose() = frameworkUnit {
                        dispose()
                    }
                }
            }
        )
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        mMethodChannel.setMethodCallHandler(null)
    }

    // 调用方法
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) = frameworkUnit {
        onMethodCall(
            call = object : MethodCallProxy {

                override val methodProxy: String
                    get() = call.method

                override val bundleProxy: Bundle
                    get() {
                        val bundle = Bundle()
                        bundle.putString("", "")
                        return bundle
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
    ) = frameworkUnit {
        getActivity(activity = binding.activity)
        getLifecycle(lifecycle = FlutterLifecycleAdapter.getActivityLifecycle(binding))
        attach()
    }

    override fun onDetachedFromActivityForConfigChanges() = Unit

    override fun onReattachedToActivityForConfigChanges(
        binding: ActivityPluginBinding,
    ) = frameworkUnit {
        getActivity(activity = binding.activity)
        getLifecycle(lifecycle = FlutterLifecycleAdapter.getActivityLifecycle(binding))
    }

    override fun onDetachedFromActivity() = Unit

    override fun getLifecycle(): Lifecycle {
        return mLifecycle
    }

    override fun onCreate(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onCreate(owner)
    }

    override fun onStart(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStart(owner)
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
        super<DefaultLifecycleObserver>.onDestroy(owner)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        when (name?.className) {
            UserService().javaClass.name -> {
                if ((service != null) and (service?.pingBinder() == true)) {
                    mIUserService = IUserService.Stub.asInterface(service)
                }
                when {
                    mIUserService != null -> {
                        Toast.makeText(this@EcosedKitPlugin, "mIUserService", Toast.LENGTH_SHORT)
                            .show()
                    }

                    else -> if (mFullDebug) Log.e(
                        tag, "UserService接口获取失败 - onServiceConnected"
                    )
                }
                when {
                    mFullDebug -> Log.i(
                        tag, "服务已连接 - onServiceConnected"
                    )
                }
            }

            this@EcosedKitPlugin.javaClass.name -> {
                if ((service != null) and (service?.pingBinder() == true)) {
                    mAIDL = EcosedKit.Stub.asInterface(service)
                }
                when {
                    mAIDL != null -> {
                        mIsBind = true
                        callBackUnit {
                            onEcosedConnected()
                        }
                    }

                    else -> if (mFullDebug) Log.e(
                        tag, "AIDL接口获取失败 - onServiceConnected"
                    )
                }
                when {
                    mFullDebug -> Log.i(
                        tag, "服务已连接 - onServiceConnected"
                    )
                }
            }

            else -> {

            }
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        when (name?.className) {
            UserService().javaClass.name -> {

            }

            this@EcosedKitPlugin.javaClass.name -> {
                mIsBind = false
                mAIDL = null
                unbindService(this)
                callBackUnit {
                    onEcosedDisconnected()
                }
                if (mFullDebug) {
                    Log.i(tag, "服务意外断开连接 - onServiceDisconnected")
                }
            }

            else -> {

            }
        }

    }

    override fun onBindingDied(name: ComponentName?) {
        super.onBindingDied(name)

        when (name?.className) {
            UserService().javaClass.name -> {

            }

            this@EcosedKitPlugin.javaClass.name -> {

            }

            else -> {

            }
        }
    }

    override fun onNullBinding(name: ComponentName?) {
        super.onNullBinding(name)
        when (name?.className) {
            UserService().javaClass.name -> {

            }

            this@EcosedKitPlugin.javaClass.name -> {
                if (mFullDebug) {
                    Log.e(tag, "Binder为空 - onNullBinding")
                }
            }

            else -> {

            }
        }
    }

    /**
     ***********************************************************************************************
     * 分类: 内部基本接口方法
     ***********************************************************************************************
     */

    private interface FlutterPluginProxy {
        fun getActivity(activity: Activity)
        fun getLifecycle(lifecycle: Lifecycle)
        fun attach()
        fun onMethodCall(call: MethodCallProxy, result: ResultProxy)
        fun getView(context: Context?, viewId: Int, args: Any?): View
        fun dispose()
    }

    private interface MethodCallProxy {
        val methodProxy: String
        val bundleProxy: Bundle
    }

    private interface ResultProxy {
        fun success(resultProxy: Any?)
        fun error(errorCodeProxy: String, errorMessageProxy: String?, errorDetailsProxy: Any?)
        fun notImplemented()
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

    private interface EngineWrapper : FlutterPluginProxy {
        fun <T> execMethodCall(channel: String, method: String, bundle: Bundle?): T?
    }

    private interface EcosedCallBack {

        /** 在服务绑定成功时回调 */
        fun onEcosedConnected()

        /** 在服务解绑或意外断开链接时回调 */
        fun onEcosedDisconnected()

        /** 在服务端服务未启动时绑定服务时回调 */
        fun onEcosedDead()

        /** 在未绑定服务状态下调用API时回调 */
        fun onEcosedUnbind()
    }

    private interface ServiceWrapper {
        fun getBinder(intent: Intent): IBinder
    }

    private interface NativeWrapper {
        fun main()
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

    /**
     ***********************************************************************************************
     * 分类: 关键内部类
     ***********************************************************************************************
     */

    private val mFramework = object : EcosedPlugin(), FlutterPluginProxy {

        override val channel: String
            get() = frameworkChannelName

        override fun onEcosedAdded(binding: PluginBinding) {
            super.onEcosedAdded(binding)
        }

        override fun onEcosedMethodCall(call: EcosedMethodCall, result: EcosedResult) {
            super.onEcosedMethodCall(call, result)
        }

        override fun getActivity(activity: Activity) = engineUnit {
            getActivity(activity = activity)
        }

        override fun getLifecycle(lifecycle: Lifecycle) = engineUnit {
            getLifecycle(lifecycle = lifecycle)
        }

        override fun onMethodCall(call: MethodCallProxy, result: ResultProxy) = engineUnit {
            onMethodCall(call = call, result = result)
        }

        override fun getView(context: Context?, viewId: Int, args: Any?): View = engineUnit {
            return@engineUnit getView(context = context, viewId = viewId, args = args)
        }

        override fun dispose() = engineUnit {
            dispose()
        }

        override fun attach() = engineUnit {
            attach()
        }
    }

    private val mEngine = object : EcosedPlugin(), EngineWrapper {

        override val channel: String
            get() = engineChannelName

        override fun getActivity(activity: Activity) {
            mActivity = activity
        }

        override fun getLifecycle(lifecycle: Lifecycle) {
            mLifecycle = lifecycle
        }

        override fun onEcosedAdded(binding: PluginBinding) {
            super.onEcosedAdded(binding)
            // 设置来自插件的全局调试布尔值
            mFullDebug = isDebug
            // 添加生命周期观察者
            lifecycle.addObserver(this@EcosedKitPlugin)
        }

        override fun onEcosedMethodCall(call: EcosedMethodCall, result: EcosedResult) {
            super.onEcosedMethodCall(call, result)
            when (call.method) {
                "" -> result.success("")
                else -> result.notImplemented()
            }
        }

        override fun onMethodCall(call: MethodCallProxy, result: ResultProxy) {
            try {
                mExecResult = execMethodCall<Any>(
                    channel = clientChannelName,
                    method = call.methodProxy,
                    bundle = call.bundleProxy
                )
                if (mExecResult != null) {
                    result.success(mExecResult)
                } else {
                    result.notImplemented()
                }
            } catch (e: Exception) {
                result.error(tag, "", Log.getStackTraceString(e))
            }
        }

        override fun getView(context: Context?, viewId: Int, args: Any?): View {
            return View(this)
        }

        override fun dispose() {
        }

        /**
         * 将引擎附加到应用.
         */
        override fun attach() {
            when {
                (mPluginList == null) or (mBinding == null) -> pluginUnit { plugin, binding ->
                    // 初始化插件列表.
                    mPluginList = arrayListOf()
                    // 添加所有插件.
                    plugin.forEach { item ->
                        item.apply {
                            try {
                                onEcosedAdded(binding = binding)
                                if (mBaseDebug) {
                                    Log.d(tag, "插件${item.javaClass.name}已加载")
                                }
                            } catch (e: Exception) {
                                if (mBaseDebug) {
                                    Log.e(tag, "插件添加失败!", e)
                                }
                            }
                        }.run {
                            mPluginList?.add(element = item)
                            if (mBaseDebug) {
                                Log.d(tag, "插件${item.javaClass.name}已添加到插件列表")
                            }
                        }
                    }
                }

                else -> if (mBaseDebug) Log.e(tag, "请勿重复执行attach!")
            }
        }

        /**
         * 调用插件代码的方法.
         * @param channel 要调用的插件的通道.
         * @param method 要调用的插件中的方法.
         * @param bundle 通过Bundle传递参数.
         * @return 返回方法执行后的返回值,类型为Any?.
         */
        override fun <T> execMethodCall(
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
                                if (mBaseDebug) {
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
                if (mBaseDebug) {
                    Log.e(tag, "插件代码调用失败!", e)
                }
            }
            return result
        }
    }

    private val mClient = object : EcosedPlugin(), EcosedCallBack {

        override val channel: String
            get() = clientChannelName

        override fun onEcosedAdded(binding: PluginBinding) = run {
            super.onEcosedAdded(binding)
            mEcosedServicesIntent = Intent(this@run, this@EcosedKitPlugin.javaClass)
            mEcosedServicesIntent.action = action


            startService(mEcosedServicesIntent)
            bindEcosed(this@run)

            Toast.makeText(this@run, "client", Toast.LENGTH_SHORT).show()

        }

        override fun onEcosedMethodCall(call: EcosedMethodCall, result: EcosedResult) {
            super.onEcosedMethodCall(call, result)
            when (call.method) {
                mMethodDebug -> result.success(isDebug)
                mMethodIsBinding -> result.success(mIsBind)
                mMethodStartService -> startService(mEcosedServicesIntent)
                mMethodBindService -> bindEcosed(this)
                mMethodUnbindService -> unbindEcosed(this)

                "getPlatformVersion" -> result.success("Android API ${Build.VERSION.SDK_INT}")

                "shizuku" -> mIUserService?.poem()

                mMethodShizukuVersion -> result.success(getShizukuVersion())
                else -> result.notImplemented()
            }
        }

        override fun onEcosedConnected() {
            Toast.makeText(this, "onEcosedConnected", Toast.LENGTH_SHORT).show()
        }

        override fun onEcosedDisconnected() {
            Toast.makeText(this, "onEcosedDisconnected", Toast.LENGTH_SHORT).show()
        }

        override fun onEcosedDead() {
            Toast.makeText(this, "onEcosedDead", Toast.LENGTH_SHORT).show()
        }

        override fun onEcosedUnbind() {
            Toast.makeText(this, "onEcosedUnbind", Toast.LENGTH_SHORT).show()
        }
    }

    private val mService = object : EcosedPlugin(), ServiceWrapper,
        Shizuku.OnBinderReceivedListener,
        Shizuku.OnBinderDeadListener,
        Shizuku.OnRequestPermissionResultListener {

        override val channel: String
            get() = serviceChannelName

        override fun getBinder(intent: Intent): IBinder {
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

        override fun onBinderReceived() {
            Toast.makeText(this@EcosedKitPlugin, "onBinderReceived", Toast.LENGTH_SHORT).show()
        }

        override fun onBinderDead() {
            Toast.makeText(this@EcosedKitPlugin, "onBinderDead", Toast.LENGTH_SHORT).show()
        }

        override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
            Toast.makeText(this@EcosedKitPlugin, "onRequestPermissionResult", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private val mNative = object : EcosedPlugin(), NativeWrapper {

        override val channel: String
            get() = nativeChannelName

        override fun onEcosedMethodCall(call: EcosedMethodCall, result: EcosedResult) {
            super.onEcosedMethodCall(call, result)
            when (call.method) {
                "main" -> main()
                else -> result.notImplemented()
            }
        }

        override fun main() {
            main(arrayOf(""))
        }
    }

    /**
     * Shizuku调用类
     */

    class UserService : IUserService.Stub() {


        override fun destroy() {
            exitProcess(status = 0)
        }

        override fun exit() {
            exitProcess(status = 0)
        }

        override fun poem() {
            Log.d(tag, "Shizuku - poem")
        }
    }

    /**
     ***********************************************************************************************
     * 分类: 调用单元
     ***********************************************************************************************
     */

    /**
     * 框架调用单元
     * Flutter插件调用框架
     * @param content Flutter插件代理单元
     * @return content 返回值
     */
    private fun <R> frameworkUnit(
        content: FlutterPluginProxy.() -> R,
    ): R = content.invoke(mFramework)

    /**
     * 引擎调用单元
     * 框架调用引擎
     * @param content 引擎包装器单元
     * @return content 返回值
     */
    private fun <R> engineUnit(
        content: EngineWrapper.() -> R,
    ): R = content.invoke(mEngine)

    /**
     * 插件调用单元
     * 插件初始化
     * @param content 插件列表单元, 插件绑定器
     * @return content 返回值
     */
    private fun <R> pluginUnit(
        content: (ArrayList<EcosedPlugin>, PluginBinding) -> R
    ): R = content.invoke(
        arrayListOf(mFramework, mEngine, mClient, mService, mNative),
        PluginBinding(context = mActivity, debug = mBaseDebug)
    )

    /**
     * 客户端回调调用单元
     * 绑定解绑调用客户端回调
     * @param content 客户端回调单元
     * @return content 返回值
     */
    private fun <R> callBackUnit(
        content: EcosedCallBack.() -> R,
    ): R = content.invoke(mClient)


    private fun <R> serviceUnit(
        content: ServiceWrapper.() -> R,
    ): R = content.invoke(mService)

    private fun <R> nativeUnit(
        content: NativeWrapper.() -> R,
    ): R = content.invoke(mNative)


    /**
     ***********************************************************************************************
     * 分类: 私有函数
     ***********************************************************************************************
     */

    private external fun main(args: Array<String>)

    /**
     * 绑定服务
     * @param context 上下文
     */
    private fun bindEcosed(context: Context) {
        try {
            if (!mIsBind) {
                context.bindService(
                    mEcosedServicesIntent,
                    this@EcosedKitPlugin,
                    Context.BIND_AUTO_CREATE
                ).let { bind ->
                    callBackUnit {
                        if (!bind) onEcosedDead()
                    }
                }
            }
        } catch (e: Exception) {
            if (mFullDebug) {
                Log.e(tag, "bindEcosed", e)
            }
        }
    }

    /**
     * 解绑服务
     * @param context 上下文
     */
    private fun unbindEcosed(context: Context) {
        try {
            if (mIsBind) {
                context.unbindService(
                    this@EcosedKitPlugin
                ).run {
                    mIsBind = false
                    mAIDL = null
                    callBackUnit {
                        onEcosedDisconnected()
                    }
                    if (mFullDebug) {
                        Log.i(tag, "服务已断开连接 - onServiceDisconnected")
                    }
                }
            }
        } catch (e: Exception) {
            if (mFullDebug) {
                Log.e(tag, "unbindEcosed", e)
            }
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(
            this@EcosedKitPlugin,
            notificationChannel
        ).build().apply {
            flags = Notification.FLAG_ONGOING_EVENT
        }
    }

    private fun check() {
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                // 还没权限

                //申请权限
                Shizuku.requestPermission(0)
            } else {
                // 已经有权限了
            }
        } catch (e: Exception) {
            if (e.javaClass == IllegalStateException().javaClass) {
                //Shizuku还没激活
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



    private fun getShizukuVersion(): String? {
        return try {
            if (mIsBind) {
                if (mAIDL != null) {
                    mAIDL!!.shizukuVersion
                } else {
                    callBackUnit {
                        onEcosedUnbind()
                    }
                    null
                }
            } else {
                callBackUnit {
                    onEcosedUnbind()
                }
                null
            }
        } catch (e: Exception) {
            if (mFullDebug) {
                Log.e(tag, "getShizukuVersion", e)
            }
            null
        }
    }

    private companion object {

        init {
            System.loadLibrary("ecosed")
        }

        // 打印日志的标签
        const val tag: String = "EcosedKitPlugin"

        const val viewTypeId: String = "ecosed_view"

        // Flutter插件通道名称
        const val flutterChannelName = "ecosed_kit"

        // Ecosed插件通道名称
        const val frameworkChannelName: String = "ecosed_framework"
        const val engineChannelName: String = "ecosed_engine"
        const val clientChannelName: String = "ecosed_client"
        const val serviceChannelName: String = "ecosed_service"
        const val nativeChannelName: String = "ecosed_native"

        const val notificationId = 1
        const val notificationChannel: String = "ecosed_notification"

        const val action: String = "io.ecosed.kit.action"


        const val mMethodDebug: String = "is_binding"
        const val mMethodIsBinding: String = "is_debug"
        const val mMethodStartService: String = "start_service"
        const val mMethodBindService: String = "bind_service"
        const val mMethodUnbindService: String = "unbind_service"
        const val mMethodShizukuVersion: String = "shizuku_version"


    }
}