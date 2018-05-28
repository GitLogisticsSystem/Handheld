package com.paradigm2000.cms.event;

import android.content.Context;

import com.paradigm2000.core.gallery.GalleryActivity;
import com.paradigm2000.core.io.Folder;

public class PhotosCheckEvent
{
    boolean hasPhotos;

    public PhotosCheckEvent(Context context)
    {
        hasPhotos = GalleryActivity.hasPhoto(context, Folder.data(context, true));
    }

    public boolean hasPhotos()
    {
        return hasPhotos;
    }
}
