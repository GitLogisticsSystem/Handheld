package com.paradigm2000.cms.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.paradigm2000.cms.BuildConfig;
import com.paradigm2000.cms.R;
import com.paradigm2000.cms.gson.ContainerOut;
import com.paradigm2000.cms.gson.Header;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@EViewGroup(R.layout.item_containerout)
public class ContainerOutView extends FrameLayout
{
    static final boolean DEBUG = BuildConfig.debug;
    static final String TAG = "ContainerOutView";

    @ViewById(android.R.id.text1)
    TextView text1;
    @ViewById(android.R.id.text2)
    TextView text2;
    @ViewById(R.id.new_add)
    TextView new_add;

    SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss", Locale.getDefault());
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - kk:mm", Locale.getDefault());

    public ContainerOutView(Context context)
    {
        super(context);
        setBackgroundColor(Color.WHITE);
    }

    public void bind(ContainerOut containerout)
    {
        Date date = null;
        try
        {
            date = parser.parse(containerout.time1);
        }
        catch (ParseException e)
        {
            if (DEBUG) Log.w(TAG, "Fail to parse date @" + containerout.mdte, e);
        }
        String value1 = null;
        if (!TextUtils.isEmpty(containerout.cont))value1 = containerout.cont;
        if (!TextUtils.isEmpty(containerout.oper))value1 += " (" + containerout.oper + ")";
        if (!TextUtils.isEmpty(containerout.size)) value1 += " " + containerout.size;
        if (!TextUtils.isEmpty(containerout.type)) value1 += " / " + containerout.type;
        text1.setText(value1);
        String value2 = "@ " + (date == null? "dd/MM - kk:mm": formatter.format(date));
        if (!TextUtils.isEmpty(containerout.stat)) value2 = containerout.stat + " " + value2;
        if (!TextUtils.isEmpty(containerout.trac)) value2 +=  value2+" ("+ containerout.trac+") ";
        text2.setText(value2);
        new_add.setBackgroundResource(containerout.isSUH()? R.drawable.button_red: R.drawable.button_green);
        new_add.setText(containerout.isSUH()? R.string.new_add: R.string.gatelog);
        new_add.setAlpha(containerout.isCompleted()? .1f: 1);
    }

    @Override
    public void setOnClickListener(OnClickListener l)
    {
        getChildAt(0).setOnClickListener(l);
    }
}
