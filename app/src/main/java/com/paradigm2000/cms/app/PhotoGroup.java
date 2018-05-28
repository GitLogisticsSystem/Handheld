package com.paradigm2000.cms.app;

import android.content.Context;

import com.paradigm2000.core.io.Folder;

public interface PhotoGroup
{
    String getTitle();
    String getName(int position, String timestamp);
    Folder getFolder(Context context);
}
