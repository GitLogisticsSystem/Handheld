package com.paradigm2000.cms.app;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;


import com.paradigm2000.cms.BuildConfig;
import com.paradigm2000.cms.R;
import com.paradigm2000.cms.event.UrlResetEvent;
import com.paradigm2000.cms.retrofit.ApiCaller;
import com.paradigm2000.core.Common;
import com.paradigm2000.core.bean.EventBus;
import com.paradigm2000.core.dialog.Dialog2;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.net.HttpURLConnection;
import java.net.URL;

@EBean
public class CheckDialog extends Dialog2
{
    static final boolean DEBUG = BuildConfig.debug;
    static final String TAG = "CheckDialog";

    @RootContext
    Activity activity;
    @Bean
    ApiCaller api;
    @Bean
    EventBus bus;
    @Pref
    MyPref_ pref;
    @SystemService
    Vibrator vibrator;

    boolean mError = false;

    public CheckDialog(Context context)
    {
        super(context, Dialog2.CUSTOM_TYPE);
        restore();
    }

    private void restore()
    {
        setTitleRes(R.string.app_name);
        setConfirm(R.string.submit);
        setCancel(R.string.leave);
        setCloseOnClick(false);
        showCancel(true);
        setView(R.layout.dialog_baseurl, new Dialog2.CustomListener()
        {
            EditText baseurl;
            EditText app;
            TextView error;

            @Override
            public void onLayout(final Dialog2 dialog, FrameLayout parent)
            {
                error = (TextView) parent.findViewById(R.id.error);
                error.setVisibility(mError? View.VISIBLE: View.GONE);
                baseurl = (EditText) parent.findViewById(R.id.baseurl);
                baseurl.setOnEditorActionListener(new TextView.OnEditorActionListener()
                {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
                    {
                        return doCheck(dialog);
                    }
                });
                app = (EditText) parent.findViewById(R.id.app);
                app.setOnEditorActionListener(new TextView.OnEditorActionListener()
                {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent)
                    {
                        return doCheck(dialog);
                    }
                });
                baseurl.requestFocus();
            }

            @Override
            public void onConfirm(Dialog2 dialog, FrameLayout parent)
            {
                app.onEditorAction(EditorInfo.IME_ACTION_DONE);
            }

            @Override
            public void onCancel(Dialog2 dialog, FrameLayout parent)
            {
                dismissWithAnimation();
            }

            private boolean doCheck(Dialog2 dialog)
            {
                if (baseurl.length() == 0) return baseurl.requestFocus();
                dialog.changeType(Dialog2.PROGRESS_TYPE)
                        .showCancel(false)
                        .setCloseOnClick(false)
                        .setCancelOnBack(false)
                        .setContentRes(R.string.LOADING);
                String url = baseurl.getText().toString().trim();
                String path = app.getText().toString().trim();
                doCheckUrl(url, path.length() == 0? "sml": path, dialog);
                return true;
            }
        });
    }

    @Override
    public void cancel()
    {
        dismissWithAnimation();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        if (!pref.ServerURL().exists()) activity.onBackPressed();
    }

    /****************************************/
    // TODO Check androidbc.asmx exists
    /****************************************/

    @Background
    void doCheckUrl(String serverurl, String app, Dialog2 dialog)
    {
        boolean result = false;
        try
        {
            String url = (serverurl.startsWith("://") ? "" : "http://") + serverurl + "/" + app + "_app/androidbc.asmx";
            if (DEBUG) Log.i(TAG, "url: " + url);
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            int response = con.getResponseCode();
            if (DEBUG) Log.i(TAG, "response: " + response);
            result = response == HttpURLConnection.HTTP_OK;
        }
        catch (Exception e)
        {
            if (DEBUG) Log.w(TAG, "doCheckUrl", e);
        }
        afterCheckUrl(result, serverurl, app, dialog);
    }

    @UiThread
    void afterCheckUrl(boolean result, String serverurl, String app, Dialog2 dialog)
    {
        if (result)
        {
            pref.ServerURL().put(serverurl);
            pref.AppPath().put(app);
            dialog.dismissWithAnimation();
            api.updateServerUrl();
            bus.post(new UrlResetEvent());
        }
        else
        {
            vibrator.vibrate(Common.PATTERN, -1);
            changeType(Dialog2.CUSTOM_TYPE);
            mError = true;
            restore();
        }
    }
}
