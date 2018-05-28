package com.paradigm2000.cms.widget;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.paradigm2000.cms.R;
import com.paradigm2000.cms.gson.Detail;
import com.paradigm2000.core.Common;
import com.paradigm2000.core.io.Folder;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;

import java.io.File;

@EViewGroup(R.layout.item_detail)
public class DetailView extends FrameLayout
{
    @ViewById(R.id.position)
    TextView _position;
    @ViewById(R.id.estm)
    TextView _estm;
    @ViewById(R.id.ac)
    TextView _ac;
    @ViewById(R.id.mac)
    TextView _mac;
    @ViewById(R.id.s1)
    TextView _s1;
    @ViewById(R.id.s2)
    TextView _s2;
    @ViewById(R.id.qty)
    TextView _qty;
    @ViewById(R.id.cpn)
    TextView _cpn;
    @ViewById(R.id.rp)
    TextView _rp;
    @ViewById(R.id.loca)
    TextView _loca;
    @ViewById(R.id.dmcode)
    TextView _dmcode;
    @ViewById(R.id.tpc)
    TextView _tpc;
    @ViewById(R.id.hr)
    TextView _hr;
    @ViewById(R.id.mat)
    TextView _mat;
    @ViewById(R.id.description)
    TextView _description;
    @ViewById(R.id.photo_section)
    View _photo_section;
    @ViewById(R.id.photo)
    SimpleDraweeView _photo;
    @ColorRes(R.color.colorPrimaryLight)
    int normalBg;
    @ColorRes(R.color.colorAccentLight)
    int selectedBg;

    public DetailView(Context context)
    {
        super(context);
    }

    public void bind(Detail detail, boolean enabled, boolean completed)
    {
        CardView cardview = (CardView) getChildAt(0);
        cardview.setCardBackgroundColor(detail.isSelected()? selectedBg: normalBg);
        _position.setText(String.valueOf(detail.position + 1));
        _estm.setText(detail.estm);
        _ac.setText(detail.acc);
        _mac.setText(detail.macc);
        _s1.setText(String.valueOf(detail.s1));
        _s2.setText(String.valueOf(detail.s2));
        _qty.setText(String.valueOf(detail.qty));
        _cpn.setText(detail.cpn);
        _rp.setText(detail.rpc);
        _loca.setText(detail.loca);
        _dmcode.setText(detail.dama);
        _tpc.setText(detail.tpc);
        _hr.setText(String.valueOf(detail.hrs));
        _mat.setText(String.valueOf(detail.mat));
        _description.setText(detail.description);
        _photo_section.setVisibility(enabled? View.VISIBLE: View.GONE);
        if (!enabled) return;
        if (Common.get().isExternalStorageAvailable())
        {
            Folder folder = detail.getFolder(getContext());
            File[] files = folder.listFiles();
            if (files == null || files.length == 0)
            {
                if (completed)
                {
                    _photo.setVisibility(View.GONE);
                }
                else
                {
                    _photo.setVisibility(View.VISIBLE);
                    _photo.setImageURI((Uri) null);
                }
            }
            else
            {
                _photo.setVisibility(View.VISIBLE);
                // _photo.setImageURI(Uri.fromFile(files[0]));
                _photo.setImageResource(R.drawable.ic_add_a_photo_light_24dp);
            }
        }
        else
        {
            _photo.setVisibility(View.GONE);
            _photo.setImageURI((Uri) null);
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l)
    {
        getChildAt(0).setOnClickListener(l);
        _photo.setOnClickListener(l);
    }
}
