package com.tencent.qcloud.tuikit.tuichat.classicui.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.jxw.basemodel.constant.ConstantDataStore;
import com.jxw.basemodel.network.ApiHelper;
import com.jxw.basemodel.utils.DataStoreUtils;
import com.jxw.twecall.TweCallManager;
import com.tencent.qcloud.tuicore.TUIConstants;
import com.tencent.qcloud.tuicore.TUICore;
import com.tencent.qcloud.tuicore.interfaces.TUIExtensionEventListener;
import com.tencent.qcloud.tuicore.interfaces.TUIExtensionInfo;
import com.tencent.qcloud.tuicore.util.ToastUtil;
import com.tencent.qcloud.tuikit.tuichat.R;
import com.tencent.qcloud.tuikit.tuichat.TUIChatConstants;
import com.tencent.qcloud.tuikit.tuichat.bean.ChatInfo;
import com.tencent.qcloud.tuikit.tuichat.classicui.widget.input.InputView;
import com.tencent.qcloud.tuikit.tuichat.presenter.C2CChatPresenter;
import com.tencent.qcloud.tuikit.tuichat.util.TUIChatLog;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TUIC2CChatFragment extends TUIBaseChatFragment {
    private static final String TAG = TUIC2CChatFragment.class.getSimpleName();

    private ChatInfo chatInfo;
    private C2CChatPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        TUIChatLog.i(TAG, "oncreate view " + this);

        baseView = super.onCreateView(inflater, container, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            return baseView;
        }
        chatInfo = (ChatInfo) bundle.getSerializable(TUIChatConstants.CHAT_INFO);
        if (chatInfo == null) {
            return baseView;
        }
        getCurrentOpenId();
        initView();

        return baseView;
    }

    @Override
    protected void initView() {
        super.initView();

        setTitleBarExtension();

        chatView.setPresenter(presenter);
        presenter.setChatInfo(chatInfo);
        presenter.setTypingListener(chatView.mTypingListener);
        chatView.setChatInfo(chatInfo);
        chatView.setInputMoreActionListener(new InputView.InputMoreActionClickListener() {
            @Override
            public void onInputMoreActionClick(int type) {
                if (!DataStoreUtils.INSTANCE.getData(ConstantDataStore.TWE_CALL_DEVICE_IS_VIP_STATUS, false)) {
                    mTweCallVipHintView.setVisibility(View.VISIBLE);
                    return;
                } else {
                    mTweCallVipHintView.setVisibility(View.GONE);
                }
                if (chatInfo.getOpenId() != null) {
                    TweCallManager.INSTANCE.startAudioAndVideoCall(type, chatInfo.getOpenId(), chatInfo.getFaceUrl(), chatInfo.getChatName(), DataStoreUtils.INSTANCE.getData(ConstantDataStore.IM_ACCOUNT_INFO, ""), chatInfo.getId());
                } else {
                    ToastUtil.toastShortMessageCenter(getString(R.string.network_error));
                }
            }
        });
    }

    private void setTitleBarExtension() {
        Map<String, Object> param = new HashMap<>();
        param.put(TUIConstants.TUIChat.Extension.ChatNavigationMoreItem.USER_ID, chatInfo.getId());
        List<TUIExtensionInfo> extensionInfoList = TUICore.getExtensionList(TUIConstants.TUIChat.Extension.ChatNavigationMoreItem.CLASSIC_EXTENSION_ID, param);
        if (!extensionInfoList.isEmpty()) {
            Collections.sort(extensionInfoList, new Comparator<TUIExtensionInfo>() {
                @Override
                public int compare(TUIExtensionInfo o1, TUIExtensionInfo o2) {
                    return o2.getWeight() - o1.getWeight();
                }
            });

            Collections.singletonList(extensionInfoList);
            TUIExtensionInfo extensionInfo = extensionInfoList.get(0);
            titleBar.setRightIcon((Integer) extensionInfo.getIcon());
            titleBar.setOnRightClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TUIExtensionEventListener eventListener = extensionInfo.getExtensionListener();
                    if (eventListener != null) {
                        Map<String, Object> map = new HashMap<>();
                        map.put(TUIConstants.TUIChat.Extension.ChatNavigationMoreItem.USER_ID, chatInfo.getId());
                        map.put(TUIConstants.TUIChat.CHAT_BACKGROUND_URI, mChatBackgroundThumbnailUrl);
                        eventListener.onClicked(map);
                    }
                }
            });
        }
    }

    public void setPresenter(C2CChatPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public C2CChatPresenter getPresenter() {
        return presenter;
    }

    @Override
    public ChatInfo getChatInfo() {
        return chatInfo;
    }

    /**
     * 获取openId
     */
    private void getCurrentOpenId() {
        chatInfo.setOpenId(null);
        ApiHelper.INSTANCE.getCurrentUserOpenId(chatInfo.getId()).observe(getViewLifecycleOwner(), openId -> {
            chatInfo.setOpenId(openId);
        });
    }
}
