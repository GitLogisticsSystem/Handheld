package com.paradigm2000.cms.widget;

import android.content.Context;
import android.graphics.Color;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.paradigm2000.cms.R;
import com.paradigm2000.cms.gson.Container;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.item_container)
public class ContainerView extends FrameLayout
{
    @ViewById(android.R.id.text1)
    TextView text;

    public ContainerView(Context context)
    {
        super(context);
        setBackgroundColor(Color.WHITE);
    }

    public void bind(Container container)
    {
        text.setText(String.format("%s (%s) %s", container.cont, container.oper, container.sys));
    }

    @Override
    public void setOnClickListener(OnClickListener l)
    {
        getChildAt(0).setOnClickListener(l);
    }
}
