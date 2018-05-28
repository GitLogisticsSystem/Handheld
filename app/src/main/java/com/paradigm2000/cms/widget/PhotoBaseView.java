package com.paradigm2000.cms.widget;

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.paradigm2000.cms.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.item_photobase)
public class PhotoBaseView extends FrameLayout
{
    @ViewById(R.id.imageview)
    ImageView _imageview;

    public PhotoBaseView(Context context)
    {
        super(context);
    }

    @Override
    public void setOnClickListener(OnClickListener l)
    {
        _imageview.setOnClickListener(l);
    }
}
