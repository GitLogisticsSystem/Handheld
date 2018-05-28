package com.paradigm2000.cms.app;

import android.net.Uri;

import com.paradigm2000.core.gallery.GalleryItem;

public class Image extends GalleryItem
{
    Object target = new Object();

    public Image(Uri uri)
    {
        super(uri);
    }

    public void setUri(Uri uri)
    {
        this.uri = uri;
    }

    @Override
    public void setFailed()
    {
        super.setFailed();
        setLoaded();
    }

    public boolean isLoaded()
    {
        return target == null;
    }

    public void setLoaded()
    {
        target = null;
    }
}
