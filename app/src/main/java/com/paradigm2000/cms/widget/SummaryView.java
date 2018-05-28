package com.paradigm2000.cms.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.paradigm2000.cms.R;
import com.paradigm2000.cms.gson.Summary;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.item_summary)
public class SummaryView extends FrameLayout
{
    @ViewById(R.id.estm)
    TextView _estm;
    @ViewById(R.id.mac)
    TextView _mac;
    @ViewById(R.id.hr)
    TextView _hr;
    @ViewById(R.id.hrate)
    TextView _hrate;
    @ViewById(R.id.lab)
    TextView _lab;
    @ViewById(R.id.mat)
    TextView _mat;
    @ViewById(R.id.total)
    TextView _total;

    public SummaryView(@NonNull Context context)
    {
        super(context);
    }

    public void bind(Summary summary)
    {
        _estm.setText(summary.estm);
        _mac.setText(summary.macc);
        _hr.setText(String.valueOf(summary.hrs));
        _hrate.setText(String.valueOf(summary.hrate));
        _lab.setText(String.valueOf(summary.lab));
        _mat.setText(String.valueOf(summary.mat));
        _total.setText(String.valueOf(summary.lab + summary.mat));
    }
}
