package com.paradigm2000.cms.gson;

import android.content.Context;
import android.text.TextUtils;

import com.paradigm2000.core.io.Folder;

import org.parceler.Parcel;

import java.util.Locale;

@Parcel @SuppressWarnings("unused")
public class Container extends Header
{
    public String mkey;
    public float esttot;
    public int printjob;
    public String irmk;
    public String hrem;
    public String rmk;
    public String sys;
    public Enquiry[] enquiries;

    @Override
    public String[] std_list()
    {
        return TextUtils.isEmpty(std_list)? new String[] { "" }: ("|" + std_list).split("\\|");
    }

    @Override
    public Detail newDetail()
    {
        Detail detail = super.newDetail();
        detail.enquiry = true;
        return detail;
    }

    @Override @SuppressWarnings("ConstantConditions")
    public Folder getFolder(Context context)
    {
        return Folder.data(context, true).folder(cont).folder("enquiry");
    }

    @Override
    public String getName(int position, String timestamp)
    {
        return String.format(Locale.getDefault(), "CTN-%s-%s%02d", mkey, timestamp, position);
    }
}
