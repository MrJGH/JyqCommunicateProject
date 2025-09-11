package com.jxw.yuliao

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.jxw.basemodel.bean.UserListModel
import com.jxw.basemodel.constant.ConstantDataStore
import com.jxw.basemodel.network.ApiHelper
import com.jxw.basemodel.network.DataRepository
import com.jxw.basemodel.network.safeApiCall
import com.jxw.basemodel.utils.DataStoreUtils
import com.jxw.basemodel.utils.LauncherDataUtil
import com.jxw.twecall.TweCallManager
import com.jxw.validate.utils.LauncherDataUtils
import com.jxw.yuliao.MainActivity.TabAdapter.TabViewHolder
import com.jxw.yuliao.activity.PersonalActivity
import com.jxw.yuliao.ext.gone
import com.jxw.yuliao.ext.singClick
import com.jxw.yuliao.ext.startActivity
import com.jxw.yuliao.ext.visible
import com.jxw.yuliao.utils.DemoLog
import com.jxw.yuliao.utils.TUIKitConstants
import com.jxw.yuliao.utils.getStringFromResource
import com.jxw.yuliao.widget.QrcodeView
import com.jxw.yuliao.widget.TitleBarView
import com.tencent.imsdk.BaseConstants
import com.tencent.imsdk.v2.V2TIMCallback
import com.tencent.imsdk.v2.V2TIMConversation
import com.tencent.imsdk.v2.V2TIMConversationListFilter
import com.tencent.imsdk.v2.V2TIMConversationOperationResult
import com.tencent.imsdk.v2.V2TIMConversationResult
import com.tencent.imsdk.v2.V2TIMManager
import com.tencent.imsdk.v2.V2TIMValueCallback
import com.tencent.qcloud.tuicore.TUIConfig
import com.tencent.qcloud.tuicore.TUIConstants
import com.tencent.qcloud.tuicore.TUICore
import com.tencent.qcloud.tuicore.TUIThemeManager
import com.tencent.qcloud.tuicore.util.ErrorMessageConverter
import com.tencent.qcloud.tuicore.util.ToastUtil
import com.tencent.qcloud.tuikit.timcommon.component.UnreadCountTextView
import com.tencent.qcloud.tuikit.timcommon.component.activities.BaseLightActivity
import com.tencent.qcloud.tuikit.tuiconversation.classicui.page.TUIConversationFragmentContainer
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.util.Locale

class MainActivity : BaseLightActivity() {
    private lateinit var tabList: TabRecyclerView
    private lateinit var tabAdapter: TabAdapter
    private var onTabEventListener: OnTabEventListener? = null
    private var conversationBean: TabBean? = null

    /*  private TabBean communityBean;
      private TabBean contactsBean;
      private TabBean profileBean;*/
    private var recentCallsBean: TabBean? = null

    private lateinit var mainTitleBar2: TitleBarView
    private lateinit var mainViewPager: ViewPager2
    private lateinit var mQrcodeIv: QrcodeView
    private var mRefreshIv: AppCompatImageView? = null
    private var mRefreshTv: AppCompatTextView? = null
    private lateinit var mQrcodeCl: ConstraintLayout

    private var fragments: MutableList<Fragment?> = mutableListOf()
    private var tabBeanList: MutableList<TabBean> = mutableListOf()
    private var isClickRefresh = false  //是否是点击的刷新
    private val count = 0
    private val lastClickTime: Long = 0
    private val markUnreadMap = HashMap<String, V2TIMConversation>()
    private var selectedItem: TabBean? = null
    private var preSelectedItem: TabBean? = null
    private lateinit var unreadCountReceiver: BroadcastReceiver
    private lateinit var recentCallsReceiver: BroadcastReceiver
    private var fragmentAdapter: FragmentAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        DemoLog.i(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        LauncherDataUtils.validateActivate(this)
        instance = WeakReference(this)
        initView()
        initUnreadCountReceiver()
        initRecentCallsReceiver()

    }

    private fun initUnreadCountReceiver() {
        unreadCountReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                conversationBean?.unreadCount =
                    intent.getLongExtra(TUIConstants.UNREAD_COUNT_EXTRA, 0)
                onTabBeanChanged(conversationBean)
            }
        }
        val unreadCountFilter = IntentFilter()
        unreadCountFilter.addAction(TUIConstants.CONVERSATION_UNREAD_COUNT_ACTION)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(unreadCountReceiver, unreadCountFilter)
    }

    private fun initRecentCallsReceiver() {
        recentCallsReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent != null) {
                    val isEnableRecentCalls =
                        intent.getBooleanExtra(TUIKitConstants.RECENT_CALLS_ENABLE, false)
                    onRecentCallsStatusChanged(isEnableRecentCalls)
                }
            }
        }
        val recentCallsFilter = IntentFilter()
        recentCallsFilter.addAction(TUIKitConstants.RECENT_CALLS_ENABLE_ACTION)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(recentCallsReceiver, recentCallsFilter)
    }

    private fun onRecentCallsStatusChanged(isEnable: Boolean) {
        if (isEnable) {
            val param: MutableMap<String, Any> = HashMap()
            param[TUIConstants.TUICalling.ObjectFactory.RecentCalls.UI_STYLE] =
                TUIConstants.TUICalling.ObjectFactory.RecentCalls.UI_STYLE_CLASSIC
            val `object` = TUICore.createObject(
                TUIConstants.TUICalling.ObjectFactory.FACTORY_NAME,
                TUIConstants.TUICalling.ObjectFactory.RecentCalls.OBJECT_NAME,
                param
            )
            if (`object` is Fragment) {
                recentCallsBean = TabBean().apply {
                    weight = 95
                    text = R.string.appkit_recent_calls_tab_name
                    normalIcon = R.attr.demo_main_tab_recent_calls_normal_bg
                    selectedIcon = R.attr.demo_main_tab_recent_calls_selected_bg
                    fragment = `object`
                }
                onNewTabBean(recentCallsBean)
            }
        } else {
            onTabBeanRemoved(recentCallsBean)
            recentCallsBean = null
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        DemoLog.i(TAG, "onNewIntent")
        setIntent(intent)
    }

    private fun initView() {
        setContentView(R.layout.activity_main)
        mainTitleBar2 = findViewById(R.id.main_title_bar_2)
        mQrcodeIv = findViewById(R.id.qrcode_iv)
        mRefreshIv = findViewById(R.id.refresh_iv)
        mRefreshTv = findViewById(R.id.refresh_tv)
        mQrcodeCl = findViewById(R.id.qrcode_cl)

        getUserInfo()
        mRefreshIv?.singClick {
            isClickRefresh = true
            getUserInfo()
        }
        mRefreshTv?.singClick {
            isClickRefresh = true
            getUserInfo()
        }
        setConversationTitleBar()
    }

    private fun getUserInfo() {
        lifecycleScope.launch {
            val data = safeApiCall(
                callback = {
                    onParseError { code, _ ->
                        mQrcodeCl.visible()
                        if (code != 1) {
                            mQrcodeIv.showNoNetWork()
                        } else {
                            if (isClickRefresh) {
                                ToastUtil.toastLongMessageCenter(getStringFromResource(R.string.can_not_bind_success_repeat_bind))
                            }
                            DataStoreUtils.putData(ConstantDataStore.WECHAT_QRCODE_BIND_CHILD_INFORMATION, false)
                            getQrCode()
                            getConversationList(true)
                        }
                    }
                    onError { _, _ -> mQrcodeIv.showNoNetWork() }
                },
                responseFunction = {
                    DataRepository.getContactPersonList()
                })
            data?.let {
                isClickRefresh = false
                if (it.isNotEmpty()) {
                    DataStoreUtils.putData(ConstantDataStore.WECHAT_QRCODE_BIND_CHILD_INFORMATION, true)
                    getConversationList(false, it)
                    initTabs()
                    mQrcodeCl.gone()
                    checkNeedSendAudioAndVideo()
                } else {
                    mQrcodeCl.visible()
                    DataStoreUtils.putData(ConstantDataStore.WECHAT_QRCODE_BIND_CHILD_INFORMATION, false)
                }
            }
        }
    }

    /**
     * @param isDeleteAll true  全部删除 ，false 部分删除
     */
    private fun getConversationList(
        isDeleteAll: Boolean,
        data: MutableList<UserListModel>? = null
    ) {
        V2TIMManager.getConversationManager()
            .getConversationList(0, 100, object : V2TIMValueCallback<V2TIMConversationResult> {
                override fun onSuccess(v2TIMConversationResult: V2TIMConversationResult?) {
                    var conversationIDList = mutableListOf<String>()
                    if (isDeleteAll) {
                        Log.e(TAG, " v2TIMConversationResult?.conversationList === ${v2TIMConversationResult?.conversationList}")
                        v2TIMConversationResult?.conversationList?.forEach {
                            conversationIDList.add(it.conversationID)
                        }
                    } else {
                        val conversationIDList1 = mutableListOf<String>()
                        v2TIMConversationResult?.conversationList?.forEach {
                            conversationIDList1.add(it.conversationID)
                        }
                        val conversationIDList2 = mutableListOf<String>()
                        data?.forEach { conversationIDList2.add(it.getConversationID()) }
                        conversationIDList.addAll(conversationIDList1.minus(conversationIDList2.toSet()))
                        Log.e(TAG, "conversationIDList1 ==========$conversationIDList1")
                        Log.e(TAG, "conversationIDList2 ==========$conversationIDList2")
                        Log.e(TAG, "conversationIDList ==========$conversationIDList")
                    }
                    if (conversationIDList.isNotEmpty()) {
                        V2TIMManager.getConversationManager().deleteConversationList(
                            conversationIDList,
                            true,
                            object : V2TIMValueCallback<List<V2TIMConversationOperationResult>> {
                                override fun onSuccess(p0: List<V2TIMConversationOperationResult>?) {
                                    Log.e(TAG, "会话删除成功")
                                }

                                override fun onError(p0: Int, p1: String?) {
                                    Log.e(TAG, "会话删除失败")
                                }
                            })
                    }

                }

                override fun onError(code: Int, desc: String?) {
                }

            })

    }

    private fun getQrCode() {
        lifecycleScope.launch {
            val data = safeApiCall(
                callback = { onError { i, s -> mQrcodeIv.showNoNetWork() } },
                responseFunction = { DataRepository.getQrCode() })
            data?.let {
                mQrcodeCl.visible()
                mQrcodeIv.setImageView(it.qrcode)
            }
        }
    }

    private fun initTabs() {
        // conversation
        conversationBean = TabBean().apply {
            normalIcon = R.attr.demo_main_tab_conversation_normal_bg
            selectedIcon = R.attr.demo_main_tab_conversation_selected_bg
            text = R.string.tab_conversation_tab_text
            fragment = TUIConversationFragmentContainer()
            showUnread = true
            unreadClearEnable = true
            weight = 100
        }
        conversationBean?.let { tabBeanList.add(it) }

        tabBeanList.sortWith { o1, o2 -> o2.weight - o1.weight }
        tabList = findViewById(R.id.tab_list)
        tabList.disableIntercept()
        tabList.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        //        tabList.addItemDecoration(new TabDecoration());
        tabAdapter = TabAdapter()
        tabList.adapter = tabAdapter
        for (tabBean in tabBeanList) {
            fragments.add(tabBean.fragment)
        }
        mainViewPager = findViewById(R.id.view_pager)
        fragmentAdapter = FragmentAdapter(this)
        fragmentAdapter?.setFragmentList(fragments)
        mainViewPager.isUserInputEnabled = false
        mainViewPager.offscreenPageLimit = 5
        mainViewPager.adapter = fragmentAdapter
        setTabSelected(conversationBean)
        onTabEventListener = object : OnTabEventListener {
            override fun onTabSelected(tabBean: TabBean?) {
                setTabSelected(tabBean)
            }

            override fun onTabUnreadCleared(tabBean: TabBean) {
                if (tabBean === conversationBean) {
                    triggerClearAllUnreadMessage()
                }
            }
        }
    }

    private fun triggerClearAllUnreadMessage() {
        V2TIMManager.getConversationManager()
            .cleanConversationUnreadMessageCount("", 0, 0, object : V2TIMCallback {
                override fun onSuccess() {
                    DemoLog.i(TAG, "cleanConversationUnreadMessageCount success")
                    conversationBean?.unreadCount = 0
                    onTabBeanChanged(conversationBean)
                    ToastUtil.toastShortMessage(this@MainActivity.getString(R.string.mark_all_message_as_read_succ))
                }

                override fun onError(code: Int, desc: String) {
                    DemoLog.i(TAG, "cleanConversationUnreadMessageCount error:$code, desc:" + ErrorMessageConverter.convertIMError(code, desc))
                    ToastUtil.toastShortMessage(
                        String.format(
                            Locale.US,
                            this@MainActivity.getString(R.string.mark_all_message_as_read_err_format),
                            code,
                            ErrorMessageConverter.convertIMError(code, desc)
                        )
                    )
                }
            })
        val filter = V2TIMConversationListFilter()
        filter.markType = V2TIMConversation.V2TIM_CONVERSATION_MARK_TYPE_UNREAD
        getMarkUnreadConversationList(
            filter,
            0,
            100,
            true,
            object : V2TIMValueCallback<HashMap<String, V2TIMConversation>> {
                override fun onSuccess(stringV2TIMConversationHashMap: HashMap<String, V2TIMConversation>?) {
                    if (stringV2TIMConversationHashMap?.size == 0) {
                        return
                    }
                    val unreadConversationIDList: MutableList<String> = ArrayList()
                    val iterator: Iterator<Map.Entry<String, V2TIMConversation>> =
                        markUnreadMap.entries.iterator()
                    while (iterator.hasNext()) {
                        val (key) = iterator.next()
                        unreadConversationIDList.add(key)
                    }
                    V2TIMManager.getConversationManager().markConversation(unreadConversationIDList,
                        V2TIMConversation.V2TIM_CONVERSATION_MARK_TYPE_UNREAD,
                        false,
                        object : V2TIMValueCallback<List<V2TIMConversationOperationResult>> {
                            override fun onSuccess(v2TIMConversationOperationResults: List<V2TIMConversationOperationResult>) {
                                for (result in v2TIMConversationOperationResults) {
                                    if (result.resultCode == BaseConstants.ERR_SUCC) {
                                        val v2TIMConversation = markUnreadMap[result.conversationID]
                                        if (v2TIMConversation?.markList?.contains(V2TIMConversation.V2TIM_CONVERSATION_MARK_TYPE_HIDE) == false) {
                                            markUnreadMap.remove(result.conversationID)
                                        }
                                    }
                                }
                            }

                            override fun onError(code: Int, desc: String) {
                                DemoLog.e(
                                    TAG,
                                    "triggerClearAllUnreadMessage->markConversation error:$code, desc:" + ErrorMessageConverter.convertIMError(
                                        code,
                                        desc
                                    )
                                )
                            }
                        })
                }

                override fun onError(code: Int, desc: String) {
                    DemoLog.e(
                        TAG,
                        "triggerClearAllUnreadMessage->getMarkUnreadConversationList error:$code, desc:" + ErrorMessageConverter.convertIMError(
                            code,
                            desc
                        )
                    )
                }
            })
    }

    private fun getMarkUnreadConversationList(
        filter: V2TIMConversationListFilter,
        nextSeq: Long,
        count: Int,
        fromStart: Boolean,
        callback: V2TIMValueCallback<HashMap<String, V2TIMConversation>>?
    ) {
        if (fromStart) {
            markUnreadMap.clear()
        }
        V2TIMManager.getConversationManager().getConversationListByFilter(
            filter,
            nextSeq,
            count,
            object : V2TIMValueCallback<V2TIMConversationResult> {
                override fun onSuccess(v2TIMConversationResult: V2TIMConversationResult) {
                    val conversationList = v2TIMConversationResult.conversationList
                    for (conversation in conversationList) {
                        markUnreadMap[conversation.conversationID] = conversation
                    }
                    if (!v2TIMConversationResult.isFinished) {
                        getMarkUnreadConversationList(
                            filter,
                            v2TIMConversationResult.nextSeq,
                            count,
                            false,
                            callback
                        )
                    } else {
                        callback?.onSuccess(markUnreadMap)
                    }
                }

                override fun onError(code: Int, desc: String) {
                    DemoLog.e(
                        TAG,
                        "getMarkUnreadConversationList error:$code, desc:" + ErrorMessageConverter.convertIMError(
                            code,
                            desc
                        )
                    )
                }
            })
    }

    private fun setTabSelected(tabBean: TabBean?) {
        if (tabBean == null) {
            return
        }
        val position = tabBeanList.indexOf(tabBean)
        if (position == -1) {
            return
        }
        tabAdapter?.notifyItemChanged(position)
        mainViewPager?.setCurrentItem(position, false)
        resetMenuState()
        if (tabBean === conversationBean) {
            setConversationTitleBar()
        } /*else if (tabBean == communityBean) {
            mainTitleBar.setVisibility(View.GONE);
            setCommunityBackground();
        } else if (tabBean == contactsBean) {
            setContactTitleBar();
        } else if (tabBean == profileBean) {
            setProfileTitleBar();
        } */ else if (tabBean === recentCallsBean) {
            setRecentCallsTitleBar()
        }
        preSelectedItem = selectedItem
        selectedItem = tabBean
        val prePosition = tabBeanList.indexOf(preSelectedItem)
        if (prePosition == -1) {
            return
        }
        tabAdapter.notifyItemChanged(prePosition)
    }

    private fun setConversationTitleBar() {
        mainTitleBar2.setRightImageClickListener {
            startActivity<PersonalActivity>()
        }
    }


    private fun setRecentCallsTitleBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(
                TUIThemeManager.getAttrResId(
                    this,
                    com.tencent.qcloud.tuicore.R.attr.core_header_start_color
                )
            )
        }
    }

    private fun resetMenuState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = resources.getColor(
                TUIThemeManager.getAttrResId(
                    this,
                    com.tencent.qcloud.tuicore.R.attr.core_header_start_color
                )
            )
        }
    }

    override fun onResume() {
        DemoLog.i(TAG, "onResume")
        super.onResume()
        checkNeedSendAudioAndVideo()
        setCurrentItemTab()
    }

    private fun checkNeedSendAudioAndVideo() {
        if (LauncherDataUtil.isNeedSendAudioAndVideo()) {
            TweCallManager.init(this@MainActivity)
            ApiHelper.getActivateInfo().observe(this) {
                DataStoreUtils.putData(ConstantDataStore.TWE_CALL_DEVICE_IS_VIP_STATUS, it.getExpireStatus())
            }
        }
    }

    private fun setCurrentItemTab() {
        if (TUIConfig.getTUIHostType() == TUIConfig.TUI_HOST_TYPE_RTCUBE) {
            val intent = intent
            if (intent != null) {
                var tabName = intent.getStringExtra(TUIConstants.TIMAppKit.IM_DEMO_ITEM_TYPE_KEY)
                if (TextUtils.isEmpty(tabName)) {
                    tabName = TUIConstants.TIMAppKit.IM_DEMO_ITEM_TYPE_CHAT
                }
                if (TextUtils.equals(tabName, TUIConstants.TIMAppKit.IM_DEMO_ITEM_TYPE_CHAT)) {
                    setTabSelected(conversationBean)
                } /* else if (TextUtils.equals(tabName, TUIConstants.TIMAppKit.IM_DEMO_ITEM_TYPE_COMMUNITY)) {
                    setTabSelected(communityBean);
                } else if (TextUtils.equals(tabName, TUIConstants.TIMAppKit.IM_DEMO_ITEM_TYPE_CONTACT)) {
                    setTabSelected(contactsBean);
                } */ else if (TextUtils.equals(
                        tabName,
                        TUIConstants.TIMAppKit.IM_DEMO_ITEM_TYPE_RECENT_CALLS
                    )
                ) {
                    setTabSelected(recentCallsBean)
                } /*else if (TextUtils.equals(tabName, TUIConstants.TIMAppKit.IM_DEMO_ITEM_TYPE_PROFILE)) {
                    setTabSelected(profileBean);
                }*/
            }
        }
    }

    override fun onDestroy() {
        DemoLog.i(TAG, "onDestroy")
        super.onDestroy()
        unreadCountReceiver?.let {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(it)
        }
    }

    private fun onTabBeanChanged(tabBean: TabBean?) {
        val index = tabBeanList.indexOf(tabBean)
        if (index == -1) {
            return
        }
        tabAdapter.notifyItemChanged(index)
    }

    private fun onNewTabBean(tabBean: TabBean?) {
        if (tabBean == null) {
            return
        }
        tabBeanList.add(tabBean)
        tabBeanList.sortWith { o1, o2 -> o2.weight - o1.weight }
        val index = tabBeanList.indexOf(tabBean)
        fragments.add(index, tabBean.fragment)
        tabAdapter.notifyItemInserted(index)
        tabAdapter.notifyItemRangeChanged(0, tabBeanList.size)
        fragmentAdapter?.notifyItemInserted(index)
    }

    private fun onTabBeanRemoved(tabBean: TabBean?) {
        if (tabBean == null) {
            return
        }
        val index = tabBeanList.indexOf(tabBean)
        if (index == -1) {
            return
        }
        tabBeanList.remove(tabBean)
        tabAdapter.notifyItemRemoved(index)
        tabAdapter.notifyItemRangeChanged(0, tabBeanList.size)
        fragments.remove(tabBean.fragment)
        fragmentAdapter?.notifyItemRemoved(index)
    }

    internal class TabBean {
        var weight = 0
        var normalIcon = 0
        var selectedIcon = 0
        var text = 0
        var fragment: Fragment? = null
        var unreadCount: Long = 0
        var showUnread = false
        var unreadClearEnable = false
    }

    internal inner class TabAdapter : RecyclerView.Adapter<TabViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
            val view = LayoutInflater.from(this@MainActivity)
                .inflate(R.layout.main_classic_bottom_tab_item_layout, null)
            return TabViewHolder(view)
        }

        override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        }

        @SuppressLint("ClickableViewAccessibility")
        private fun prepareToClearAllUnreadMessage(
            unreadCountTextView: UnreadCountTextView,
            tabBean: TabBean
        ) {
            unreadCountTextView.setOnTouchListener(object : OnTouchListener {
                private var downX = 0f
                private var downY = 0f
                private var isTriggered = false
                override fun onTouch(view: View, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            downX = unreadCountTextView.x
                            downY = unreadCountTextView.y
                        }

                        MotionEvent.ACTION_MOVE -> {
                            if (isTriggered) {
                                return true
                            }
                            val viewX = view.x
                            val viewY = view.y
                            val eventX = event.x
                            val eventY = event.y
                            val translationX = eventX + viewX - downX
                            val translationY = eventY + viewY - downY
                            view.translationX = translationX
                            view.translationY = translationY
                            // If the moved x and y axis coordinates exceed a certain pixel, it will trigger one-click to clear all unread sessions
                            if (Math.abs(translationX) > 200 || Math.abs(translationY) > 200) {
                                isTriggered = true
                                unreadCountTextView.visibility = View.GONE
                                onTabEventListener?.onTabUnreadCleared(tabBean)
                            }
                        }

                        MotionEvent.ACTION_UP -> {
                            view.translationX = 0f
                            view.translationY = 0f
                            isTriggered = false
                        }

                        MotionEvent.ACTION_CANCEL -> {
                            view.translationX = 0f
                            view.translationY = 0f
                            isTriggered = false
                        }
                    }
                    return true
                }
            })
        }

        override fun getItemCount(): Int {
            return if (tabBeanList == null) {
                0
            } else tabBeanList.size
        }

        internal inner class TabViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var imageView: ImageView? = null
            var textView: TextView? = null
            var unreadCountTextView: UnreadCountTextView? = null
            private fun disableClipOnParents(v: View?) {
                if (v == null) {
                    return
                }
                if (v is ViewGroup) {
                    v.clipChildren = false
                    v.clipToPadding = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        v.clipToOutline = false
                    }
                }
                if (v.parent is View) {
                    disableClipOnParents(v.parent as View)
                }
            }
        }
    }

    internal interface OnTabEventListener {
        fun onTabSelected(tabBean: TabBean?)
        fun onTabUnreadCleared(tabBean: TabBean)
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private var instance: WeakReference<MainActivity?>? = null
        fun finishMainActivity() {
            if (instance != null && instance?.get() != null) {
                instance?.get()?.finish()
            }
        }
    }
}