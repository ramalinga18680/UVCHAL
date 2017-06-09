/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.droidlogic;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.UEventObserver;
import android.os.IBinder;
import android.util.Log;
import java.io.File;

import com.droidlogic.app.HdmiCecManager;

/**
 * Stub implementation of (@link android.media.tv.TvInputService}.
 */
public class CecService extends Service {
    private static final boolean DEBUG = true;
    private static final String TAG = "HdmiCecOutput";

    //For uevent
    private static final String SWITCH_STATE = "SWITCH_STATE";
    private static final String CEC_CONFIG_PATCH = "DEVPATH=/devices/virtual/switch/lang_config";

    private boolean startObServing = false;
    private HdmiCecManager mHdmiCecManager;

    public CecService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        startListenCecDev(action);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        initCecFun();
        super.onCreate();
    }

    public void startListenCecDev(String action) {
        boolean exist = new File(HdmiCecManager.CEC_DEVICE_FILE).exists();
        Log.d(TAG, "startListenCecDev(), action:" + action);
        if (action.equals("CEC_LANGUAGE_AUTO_SWITCH")) {
            if (exist) {
                // update current language
                updateCECLanguage(mHdmiCecManager.getCurLanguage());
            }
        }
        if (exist && !startObServing) {
            mCedObserver.startObserving(CEC_CONFIG_PATCH);
            startObServing = true;
        }
    }

    public UEventObserver mCedObserver = new UEventObserver() {
        @Override
        public void onUEvent(UEventObserver.UEvent event) {
            if (DEBUG) Log.d(TAG, "onUEvent()");

            String mNewLanguage = event.get(SWITCH_STATE);
            updateCECLanguage(mNewLanguage);
        }
    };

    private void updateCECLanguage(String lan) {
        if (DEBUG) Log.d(TAG, "get the language code is : " + lan);

        if (lan == null)
            return;

        if (!mHdmiCecManager.isChangeLanguageOpen()) {
            if (DEBUG) Log.d(TAG, "cec language not open");
            return;
        }

        String[] cec_language_list = getResources().getStringArray(R.array.cec_language);
        String[] language_list = getResources().getStringArray(R.array.language);
        String[] country_list = getResources().getStringArray(R.array.country);
        mHdmiCecManager.setLanguageList(cec_language_list, language_list, country_list);
        mHdmiCecManager.doUpdateCECLanguage(lan);
    }

    public void initCecFun() {
        mHdmiCecManager = new HdmiCecManager(this);
        mHdmiCecManager.initCec();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");
        if (startObServing) {
            mCedObserver.stopObserving();
        }
        super.onDestroy();
    }
}
