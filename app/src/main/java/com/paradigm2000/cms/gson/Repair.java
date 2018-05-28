package com.paradigm2000.cms.gson;

import android.content.Context;

import com.paradigm2000.core.io.Folder;

import org.parceler.Parcel;

import java.util.Locale;

@Parcel
public class Repair extends Container
{
    public String estm;
    public int eth;
    transient public boolean selected;

    @Override
    public String getName(int position, String timestamp)
    {
        return String.format(Locale.getDefault(), "%s-R%s%02d", cont, timestamp, position + 1);
    }

    @Override @SuppressWarnings("ConstantConditions")
    public Folder getFolder(Context context)
    {
        return Folder.data(context, true).folder(cont).folder("repair");
    }
}
