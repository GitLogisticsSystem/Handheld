package com.paradigm2000.cms;

import android.Manifest;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.facebook.common.util.UriUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.stetho.Stetho;
import com.paradigm2000.cms.app.CheckDialog;
import com.paradigm2000.cms.app.CheckDialog_;
import com.paradigm2000.cms.app.MyPref_;
import com.paradigm2000.cms.event.UrlResetEvent;
import com.paradigm2000.cms.gson.User;
import com.paradigm2000.cms.retrofit.ApiCaller;
import com.paradigm2000.core.Activity;
import com.paradigm2000.core.Common;
import com.paradigm2000.core.bean.EventBus;
import com.paradigm2000.core.retrofit.RetrofitUtil;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import io.fabric.sdk.android.Fabric;

@EActivity(R.layout.activity_login) @OptionsMenu(R.menu.menu_login)
public class LoginActivity extends Activity
{
    @OptionsMenuItem(R.id.preference)
    MenuItem _pref;
    @ViewById(R.id.version)
    TextView _version;
    @ViewById(R.id.header)
    SimpleDraweeView _header;
    @ViewById(R.id.username)
    EditText _username;
    @ViewById(R.id.password)
    EditText _password;
    @ViewById(R.id.login)
    Button _login;
    @Bean
    EventBus bus;
    @Bean
    ApiCaller api;
    @Bean
    RetrofitUtil util;
    @Pref
    MyPref_ pref;
    @SystemService
    Vibrator vibrator;

    CheckDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        if (BuildConfig.debug) Stetho.initializeWithDefaults(this);
        bus.register(this);
        Common.get().requestPermission(this, 0, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @AfterViews
    void afterViews()
    {
        _version.setText(Common.get().getVersion(this));
        if (pref.username().exists())
        {
            _username.setText(pref.username().get());
            _password.requestFocus();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (!pref.ServerURL().exists())
        {
            if (dialog != null) dialog.dismiss();
            dialog = CheckDialog_.getInstance_(this);
            dialog.show();
        }
        else
        {
            // TODO retrieve from preference
            Uri uri = new Uri.Builder().scheme(UriUtil.LOCAL_RESOURCE_SCHEME).path(String.valueOf(/* R.drawable.engkong */ R.drawable.header)).build();
            _header.setImageURI(uri);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        _pref.setEnabled(pref.ServerURL().exists());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        bus.unregister(this);
    }

    /****************************************/
    // TODO UI events
    /****************************************/

    @OptionsItem(R.id.preference)
    void openPreference()
    {
        PreferenceActivity_.intent(this).start();
    }

    @EditorAction(R.id.password) @Click(R.id.login)
    void login()
    {
        String username = _username.getText().toString();
        String password = _password.getText().toString();
        if (username.length() == 0) _username.requestFocus();
        else if (password.length() == 0) _password.requestFocus();
        else
        {
            _username.setEnabled(false);
            _password.setEnabled(false);
            _login.setEnabled(false);
            _pref.setVisible(false);
            doLogin(username, password);
        }
    }

    /****************************************/
    // TODO POST login
    /****************************************/

    @Background
    void doLogin(String username, String password)
    {
        IOException error = null;
        User result = null;
        try
        {
            result = api.login(username, password).execute().body();
            if (result == null) error = util.createError();
        }
        catch (UnknownHostException e)
        {
            if (!isDestroyed()) util.noNetwork(this);
        }
        catch (SocketTimeoutException e)
        {
            if (!isDestroyed()) util.timeout(this);
        }
        catch (IOException e)
        {
            error = e;
        }
        if (!isDestroyed()) afterLogin(error, result);
    }

    @UiThread
    void afterLogin(IOException error, User result)
    {
        if (error != null)
        {
            util.error(this, R.string.login, "Login_Login", error);
        }
        else if (result != null)
        {
            pref.edit()
                    .username().put(result.usrname)
                    .surveyer().put(result.isSurveyer())
                    .apply();
            MainActivity_.intent(this).start();
            _password.setText(null);
        }
        _username.setEnabled(true);
        _password.setEnabled(true);
        _login.setEnabled(true);
        _pref.setVisible(true);
    }

    /****************************************/
    // TODO Event subscription
    /****************************************/

    @Subscribe
    public void onReset(UrlResetEvent event)
    {
        _pref.setVisible(pref.ServerURL().exists());
        _pref.setEnabled(pref.ServerURL().exists());
        // TODO retrieve from preference
        int resId = /* pref.ServerURL().exists()? R.drawable.engkong: */ R.drawable.header;
        Uri uri = new Uri.Builder().scheme(UriUtil.LOCAL_RESOURCE_SCHEME).path(String.valueOf(resId)).build();
        _header.setImageURI(uri);
    }
}
