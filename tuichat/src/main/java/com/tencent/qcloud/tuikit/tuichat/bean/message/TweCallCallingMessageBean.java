package com.tencent.qcloud.tuikit.tuichat.bean.message;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.qcloud.tuikit.timcommon.bean.TUIMessageBean;
import com.tencent.qcloud.tuikit.tuichat.R;
import com.tencent.qcloud.tuikit.tuichat.TUIChatService;
import com.tencent.qcloud.tuikit.tuichat.util.TUIChatLog;

import java.util.Objects;

public class TweCallCallingMessageBean extends TUIMessageBean {
    private String text = "";
    private int callDuration;
    private int callResultType;
    private String callType = ""; // 1：GROUP_CHAT_AUDIO; 2:GROUP_CHAT_VIDEO
    private String fromAccount; //
    private String toAccount; //
    public static final String ACTION_ID_AUDIO_CALL = "GROUP_CHAT_AUDIO";
    public static final String ACTION_ID_VIDEO_CALL = "GROUP_CHAT_VIDEO";
    public static final int ACTION_ID_CALL_RESULT_TYPE_DEVICE_HANGUP = 1; //拨打成功  主动挂断（设备或小程序）
    public static final int ACTION_ID_CALL_RESULT_TYPE_REFUSE = 2;// 对方拒接
    public static final int ACTION_ID_CALL_RESULT_TYPE_H5_HANGUP = 3;//取消通话
    private TweCallCallingMessageBean customTweCallMessage;

    @Override
    public String onGetDisplayString() {
        return getExtra();
    }

    @Override
    public void onProcessMessage(V2TIMMessage v2TIMMessage) {
        String data = new String(v2TIMMessage.getCustomElem().getData());
        TUIChatLog.d("customTweCallMessage", "data = " + data);
        if (!TextUtils.isEmpty(data)) {
            try {
                customTweCallMessage = new Gson().fromJson(data, TweCallCallingMessageBean.class);
            } catch (Exception e) {
                TUIChatLog.e("customTweCallMessage", "exception e = " + e);
            }
        }
        if (customTweCallMessage != null) {
            if (Objects.equals(customTweCallMessage.callType, TweCallCallingMessageBean.ACTION_ID_AUDIO_CALL)) {
                text = "[语音通话]";
            } else if (Objects.equals(customTweCallMessage.callType, TweCallCallingMessageBean.ACTION_ID_VIDEO_CALL)) {
                text = "[视频通话]";
            }
            setCallResultType(customTweCallMessage.callResultType);
            setCallType(customTweCallMessage.callType);
            setCallDuration(customTweCallMessage.callDuration);
            setExtra(text);
        } else {
            String text = TUIChatService.getAppContext().getString(R.string.no_support_msg);
            setExtra(text);
        }
    }

    public String getCallType() {
        return callType;
    }

    public void setCallType(String callType) {
        this.callType = callType;
    }

    public int getCallResultType() {
        return callResultType;
    }

    public void setCallResultType(int callResultType) {
        this.callResultType = callResultType;
    }

    public int getCallDuration() {
        return callDuration;
    }

    public void setCallDuration(int callDuration) {
        this.callDuration = callDuration;
    }

}
