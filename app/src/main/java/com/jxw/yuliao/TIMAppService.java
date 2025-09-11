package com.jxw.yuliao;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tencent.qcloud.tuicore.ServiceInitializer;
import com.tencent.qcloud.tuicore.TUIConstants;
import com.tencent.qcloud.tuicore.TUICore;
import com.tencent.qcloud.tuicore.interfaces.ITUINotification;
import com.tencent.qcloud.tuicore.interfaces.ITUIService;

import java.util.Map;

public class TIMAppService extends ServiceInitializer implements ITUIService, ITUINotification {
    public static final String TAG = TIMAppService.class.getSimpleName();
    private static TIMAppService instance;

    public static TIMAppService getInstance() {
        return instance;
    }

    public Context mContext;



    @Override
    public void init(Context context) {
        instance = this;
        mContext = context;

        initService();
    }

    private void initService() {
        TUICore.registerService(TUIConstants.TIMAppKit.SERVICE_NAME, this);

        TUICore.registerEvent(TUIConstants.TUILogin.EVENT_LOGIN_STATE_CHANGED,
                TUIConstants.TUILogin.EVENT_SUB_KEY_USER_KICKED_OFFLINE, this);
        TUICore.registerEvent(TUIConstants.TUILogin.EVENT_LOGIN_STATE_CHANGED,
                TUIConstants.TUILogin.EVENT_SUB_KEY_USER_SIG_EXPIRED, this);
        TUICore.registerEvent(TUIConstants.TUILogin.EVENT_LOGIN_STATE_CHANGED,
                TUIConstants.TUILogin.EVENT_SUB_KEY_USER_LOGIN_SUCCESS, this);
        TUICore.registerEvent(TUIConstants.TUILogin.EVENT_LOGIN_STATE_CHANGED,
                TUIConstants.TUILogin.EVENT_SUB_KEY_USER_LOGOUT_SUCCESS, this);
        TUICore.registerEvent(TUIConstants.TIMAppKit.NOTIFY_RTCUBE_EVENT_KEY,
                TUIConstants.TIMAppKit.NOFITY_IMLOGIN_SUCCESS_SUB_KEY, this);
      /*  TUICore.registerEvent(TUIConstants.TUIConversation.METHOD_UPDATE_TOTAL_UNREAD_COUNT,
                TUIConstants.TIMAppKit.NOFITY_IMLOGIN_SUCCESS_SUB_KEY, this);*/
    }

    @Override
    public void onNotifyEvent(String key, String subKey, Map<String, Object> param) {
        Log.d(TAG, "onNotifyEvent key = " + key + "subKey = " + subKey);
        if (TUIConstants.TUILogin.EVENT_LOGIN_STATE_CHANGED.equals(key)) {
            if (TUIConstants.TUILogin.EVENT_SUB_KEY_USER_KICKED_OFFLINE.equals(subKey) || TUIConstants.TUILogin.EVENT_SUB_KEY_USER_SIG_EXPIRED.equals(subKey)
                || TUIConstants.TUILogin.EVENT_SUB_KEY_USER_LOGOUT_SUCCESS.equals(subKey)) {
                Log.d(TAG, "logout im");
            } else if (TUIConstants.TUILogin.EVENT_SUB_KEY_USER_LOGIN_SUCCESS.equals(subKey)) {
                Log.d(TAG, "login im ");
            }
        } else if (TUIConstants.TIMAppKit.NOTIFY_RTCUBE_EVENT_KEY.equals(key) && TUIConstants.TIMAppKit.NOFITY_IMLOGIN_SUCCESS_SUB_KEY.equals(subKey)) {
            if (param != null) {
                Intent intent = (Intent) param.get(TUIConstants.TIMAppKit.OFFLINE_PUSH_INTENT_DATA);
//                TUIUtils.handleOfflinePush(intent, null);
            }
        }
    }
}
