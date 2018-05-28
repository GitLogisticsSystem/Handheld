package com.paradigm2000.cms.gson;

import org.parceler.Parcel;

@Parcel @SuppressWarnings("unused")
public class User
{
    public String usrname;
    public String utype;

    public boolean isSurveyer() { return "SURVEYER".equals(utype); }
}
