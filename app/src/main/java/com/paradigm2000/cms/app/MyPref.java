package com.paradigm2000.cms.app;

import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(SharedPref.Scope.APPLICATION_DEFAULT) @SuppressWarnings("unused")
public interface MyPref
{
    // private
    String username();
    boolean surveyer();

    // public
    String ServerURL();
    String AppPath();
    String PhotoQuality();
    int Timeout();
}
