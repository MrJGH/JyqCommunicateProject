package com.jxw.yuliao.utils;

import com.tencent.qcloud.tuicore.interfaces.TUILoginConfig;

public class TUIUtils {
    private static final String TAG = TUIUtils.class.getSimpleName();


    public static TUILoginConfig getLoginConfig() {
        TUILoginConfig config = new TUILoginConfig();
        config.setLogLevel(TUILoginConfig.TUI_LOG_DEBUG);
        return config;
    }
}
