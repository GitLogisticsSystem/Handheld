package com.paradigm2000.cms.widget;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.paradigm2000.cms.R;
import com.paradigm2000.cms.gson.Repair;
import com.paradigm2000.core.Common;
import com.paradigm2000.core.io.Folder;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

import java.io.File;

@EViewGroup(R.layout.item_repair)
public class RepairView extends FrameLayout
{
    @ViewById(android.R.id.text1)
    TextView _text;
    @ViewById(R.id.photos)
    SimpleDraweeView _photos;
    @ViewById(R.id.submit)
    ImageView _submit;
    @ColorRes(R.color.colorAccentLight)
    int selectedColor;

    public RepairView(Context context)
    {
        super(context);
        setBackgroundColor(Color.WHITE);
    }

    @Override
    public void setSelected(boolean selected)
    {
        super.setSelected(selected);
        setBackgroundColor(selected? selectedColor: Color.WHITE);
        _photos.setVisibility(selected? View.VISIBLE: View.GONE);
        _submit.setVisibility(selected? View.VISIBLE: View.GONE);
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        _photos.setEnabled(enabled);
        _submit.setEnabled(enabled);
    }

    @Override
    public void setOnClickListener(OnClickListener l)
    {
        getChildAt(0).setOnClickListener(l);
        _photos.setOnClickListener(l);
        _submit.setOnClickListener(l);
    }

    public void bind(Repair repair)
    {
        setSelected(repair.selected);
        _text.setText(String.format("%s (%s) %s", repair.cont, repair.oper, repair.sys));
        if (Common.get().isExternalStorageAvailable())
        {
            Folder folder = repair.getFolder(getContext());
            File[] files = folder.listFiles();
            if (files == null || files.length == 0)
            {
                _photos.setImageURI((Uri) null);
            }
            else
            {
                _photos.setImageURI(Uri.fromFile(files[0]));
            }
        }
        else
        {
            _photos.setImageURI((Uri) null);
        }
    }
}
