package io.ecosed.kit

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.AppUtils
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.lifecycle.FlutterLifecycleAdapter
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

class EcosedKitPlugin : ContextWrapper(null), FlutterPlugin, MethodChannel.MethodCallHandler,
    ActivityAware {

    private lateinit var mMethodChannel: MethodChannel

    // 附加基本上下文
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

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
        attachBaseContext(base = binding.activity.baseContext)
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
        internal fun setMethodCallHandler(handler: EcosedPlugin) {
            mPlugin = handler
        }

        /**
         * 获取上下文.
         * @return Context.
         */
        internal fun getContext(): Context {
            return mBinding.getContext()
        }

        /**
         * 是否调试模式.
         * @return Boolean.
         */
        internal fun isDebug(): Boolean {
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
        internal fun <T> execMethodCall(
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

    private companion object {
        const val channelName = "ecosed_kit"
    }
}
