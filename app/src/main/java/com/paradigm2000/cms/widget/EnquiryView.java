package com.paradigm2000.cms.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.paradigm2000.cms.BuildConfig;
import com.paradigm2000.cms.R;
import com.paradigm2000.cms.app.MyPref_;
import com.paradigm2000.cms.gson.Detail;
import com.paradigm2000.cms.gson.Enquiry;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

@EViewGroup(R.layout.item_enquiry)
public class EnquiryView extends FrameLayout
{
    static final SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.ENGLISH);
    static final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
    static final boolean DEBUG = BuildConfig.debug;
    static final String TAG = "EDetailView";

    @ViewById(R.id.est_no)
    TextView _est_no;
    @ViewById(R.id.est_date)
    TextView _est_date;
    @ViewById(R.id.est_tot)
    TextView _est_tot;
    @ViewById(R.id.est_appr)
    TextView _est_appr;
    @ViewById(R.id.approve)
    ImageView _approve;
    @ViewById(R.id.print)
    ImageView _print;
    @ViewById(R.id.details)
    RecyclerView _details;
    @Bean
    DetailAdapter detailAdapter;
    @Pref
    MyPref_ pref;

    public EnquiryView(Context context)
    {
        super(context);
    }

    @AfterViews
    void afterViews()
    {
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        _details.setLayoutManager(manager);
        _details.setAdapter(detailAdapter);
        detailAdapter.setEnabled(false);
    }

    public void bind(Enquiry enquiry)
    {
        _est_no.setText(enquiry.est_no);
        String value = enquiry.est_date;
        try
        {
            value = formatter.format(parser.parse(value));
        }
        catch (ParseException e)
        {
            if (DEBUG) Log.w(TAG, "Fail to format @" + value);
        }
        _est_date.setText(value);
        _est_tot.setText(String.valueOf(enquiry.est_tot));
        value = enquiry.est_appr;
        try
        {
            value = value != null? formatter.format(parser.parse(value)): "---";
        }
        catch (ParseException e)
        {
            if (DEBUG) Log.w(TAG, "Fail to format @" + value);
        }
        _est_appr.setText(value);
        _approve.setVisibility(pref.surveyer().get() && "Y".equals(enquiry.est_CanBeApp)?
                View.VISIBLE: View.GONE);
        _print.setVisibility(enquiry.est_prt > 0? View.VISIBLE: View.GONE);
        detailAdapter.apply(enquiry.details.toArray(new Detail[enquiry.details.size()]));
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        _approve.setEnabled(enabled);
        _print.setEnabled(enabled);
    }

    @Override
    public void setOnClickListener(OnClickListener l)
    {
        _approve.setOnClickListener(l);
        _print.setOnClickListener(l);
    }
}
