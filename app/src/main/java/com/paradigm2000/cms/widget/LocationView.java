package com.paradigm2000.cms.widget;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.paradigm2000.cms.R;

import org.androidannotations.annotations.EViewGroup;

@EViewGroup(R.layout.item_location)
public class LocationView extends FrameLayout
{
    public LocationView(@NonNull Context context)
    {
        super(context);
    }
    public LocationView(@NonNull Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
    }
    public LocationView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }
}
