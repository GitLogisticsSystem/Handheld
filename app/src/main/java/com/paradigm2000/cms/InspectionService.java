package com.paradigm2000.cms;

import android.util.Log;

import com.paradigm2000.cms.gson.Header;
import com.paradigm2000.cms.gson.Headers;
import com.paradigm2000.core.io.Folder;

import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.api.support.app.AbstractIntentService;

import java.io.File;

@EIntentService
public class InspectionService extends AbstractIntentService
{
    static final boolean DEBUG = BuildConfig.debug;
    static final String TAG = "InspectionService";

    public InspectionService()
    {
        super(TAG);
    }

    @ServiceAction
    void check(Headers headers)
    {
        Folder folder = Folder.data(this, true);
        if (folder == null) return;
        Header[] list = headers.get();
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) return;
        for (File file: files)
        {
            if (checkExists(list, file)) continue;
            folder = new Folder(file);
            File[] files2 = folder.listFiles();
            if (files2 == null || files2.length == 0) continue;
            for (File file2: files2)
            {
                if (!file2.isDirectory()) continue;
                file2 = new Folder(file2);
                String name = file2.getName();
                if (!"header".equals(name) && !name.startsWith("detail-")) continue;
                if (!file2.delete() && DEBUG) Log.w(TAG, "Fail to delete @" + file2);
            }
        }
    }

    boolean checkExists(Header[] headers, File file)
    {
        String cont = file.getName();
        for (Header header: headers) if (cont.equals(header.cont)) return true;
        return false;
    }
}
