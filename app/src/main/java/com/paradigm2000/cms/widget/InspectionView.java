package com.paradigm2000.cms.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.paradigm2000.cms.BuildConfig;
import com.paradigm2000.cms.R;
import com.paradigm2000.cms.gson.Header;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@EViewGroup(R.layout.item_inspection)
public class InspectionView extends FrameLayout
{
    static final boolean DEBUG = BuildConfig.debug;
    static final String TAG = "InspectionView";

    @ViewById(android.R.id.text1)
    TextView text1;
    @ViewById(android.R.id.text2)
    TextView text2;
    @ViewById(R.id.new_add)
    TextView new_add;

    SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss", Locale.getDefault());
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - kk:mm", Locale.getDefault());

    public InspectionView(Context context)
    {
        super(context);
        setBackgroundColor(Color.WHITE);
    }

    public void bind(Header header)
    {
        Date date = null;
        try
        {
            date = parser.parse(header.time1);
        }
        catch (ParseException e)
        {
            if (DEBUG) Log.w(TAG, "Fail to parse date @" + header.mdte, e);
        }
        String value1 = header.cont + " (" + header.oper + ")";
        if (!TextUtils.isEmpty(header.size)) value1 += " " + header.size;
        if (!TextUtils.isEmpty(header.type)) value1 += " / " + header.type;
        text1.setText(value1);
        String value2 = "@ " + (date == null? "dd/MM - kk:mm": formatter.format(date));
        if (!TextUtils.isEmpty(header.stat)) value2 = header.stat + " " + value2;
        text2.setText(value2);
        new_add.setBackgroundResource(header.isSUH()? R.drawable.button_red: R.drawable.button_green);
        new_add.setText(header.isSUH()? R.string.new_add: R.string.gatelog);
        new_add.setAlpha(header.isCompleted()? .1f: 1);
    }

    @Override
    public void setOnClickListener(OnClickListener l)
    {
        getChildAt(0).setOnClickListener(l);
    }
}
