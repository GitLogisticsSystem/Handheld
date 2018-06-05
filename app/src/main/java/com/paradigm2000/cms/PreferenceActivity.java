package com.paradigm2000.cms;

import android.preference.Preference;

import com.paradigm2000.cms.app.MyPref_;
import com.paradigm2000.cms.event.UrlResetEvent;
import com.paradigm2000.cms.retrofit.ApiCaller;
import com.paradigm2000.core.Common;
import com.paradigm2000.core.bean.EventBus;
import com.paradigm2000.core.dialog.Dialog;
import com.paradigm2000.core.dialog.Dialog2;

import org.androidannotations.annotations.AfterPreferences;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.PreferenceByKey;
import org.androidannotations.annotations.PreferenceClick;
import org.androidannotations.annotations.PreferenceScreen;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EActivity @PreferenceScreen(R.xml.activity_settings)
public class PreferenceActivity extends com.paradigm2000.core.PreferenceActivity
{
    @PreferenceByKey(R.string.version_key)
    Preference _version;
    @PreferenceByKey(R.string.app_key)
    Preference _appPath;
    @PreferenceByKey(R.string.timeout_key)
    Preference _timeout;
    @PreferenceByKey(R.string.reset_key)
    Preference _reset;
    @Bean
    ApiCaller api;
    @Bean
    EventBus bus;
    @Pref
    MyPref_ pref;
    @Extra("Preference_logined") @InstanceState
    boolean logined;

    @AfterPreferences
    void afterPreferences()
    {
        _version.setSummary(Common.get().getVersion(this));
        _appPath.setEnabled(!logined);
        _reset.setEnabled(!logined);
        _reset.setSummary(getString(R.string.reset_text, pref.ServerURL().get()+"/"+pref.AppPath().get()));
    }

    /****************************************/
    // TODO UI events
    /****************************************/

    @PreferenceClick(R.string.timeout_key)
    void askTimeout()
    {
        new Dialog2(this, Dialog2.NUMBER_TYPE)
                .setTitleRes(R.string.timeout_title)
                .setMinValue(5)
                .setMaxValue(60)
                .setNumber(pref.Timeout().getOr(20))
                .setConfirmListener(new Dialog2.OnClickListener()
                {
                    @Override
                    public void onClick(Dialog2 dialog)
                    {
                        int value = dialog.getNumber();
                        pref.Timeout().put(value);
                        api.timeout(value);
                    }
                })
                .show();
    }

    @PreferenceClick(R.string.reset_key)
    void askDeactivate()
    {
        new Dialog(this, Dialog.PROMPT_TYPE)
                .setTitleRes(R.string.reset)
                .setContentRes(R.string.confirm, getString(R.string.reset).toLowerCase())
                .setConfirm(R.string.reset)
                .setConfirmListener(new Dialog.OnClickListener()
                {
                    @Override
                    public void onClick(Dialog dialog)
                    {
                        pref.edit()
                                .ServerURL().remove()
                                .username().remove()
                                .apply();
                        bus.post(new UrlResetEvent());
                        finish();
                    }
                })
                .show();
    }
}
