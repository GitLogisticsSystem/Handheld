package com.paradigm2000.cms.gson;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.paradigm2000.cms.app.PhotoGroup;
import com.paradigm2000.core.Common;
import com.paradigm2000.core.io.Folder;

import org.parceler.Parcel;

import java.util.Locale;

@Parcel @SuppressWarnings("unused")
public class Detail implements Comparable<Detail>, PhotoGroup
{
    public int sud = -1;
    public String estm;
    public String macc;
    public String acc;
    public String cpn;
    public String rpc;
    public String description;
    public int s1;
    public int s2;
    public int qty;
    public String loca;
    public String dama;
    public double hrs;
    public double hrate;
    public double mat;
    public String tpc;
    public String mas_acc;
    public String mas_macc;

    public String cont;
    public int position;
    public boolean enquiry = false;
    boolean selected = false;

    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean flag)
    {
        selected = flag;
    }

    public boolean isNew()
    {
        return sud == -1;
    }

    @Override @SuppressWarnings("CloneDoesntCallSuperClone")
    public Detail clone()
    {
        Detail detail = new Detail();
        detail.enquiry = enquiry;
        detail.sud = sud;
        detail.mas_acc = mas_acc;
        detail.mas_macc = mas_macc;
        detail.cont = cont;
        return detail;
    }

    public void merge(Detail detail)
    {
        estm = detail.estm;
        macc = detail.macc;
        acc = detail.acc;
        cpn = detail.cpn;
        rpc = detail.rpc;
        description = detail.description;
        s1 = detail.s1;
        s2 = detail.s2;
        qty = detail.qty;
        loca = detail.loca;
        dama = detail.dama;
        hrs = detail.hrs;
        mat = detail.mat;
        tpc = detail.tpc;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Detail)
        {
            Detail detail = (Detail) obj;
            return compare(macc, detail.macc) && compare(acc, detail.acc) &&
                    compare(cpn, detail.cpn) && compare(rpc, detail.rpc) &&
                    compare(description, detail.description) && compare(s1, detail.s1) &&
                    compare(s2, detail.s2) && compare(qty, detail.qty) &&
                    compare(loca, detail.loca) && compare(dama, detail.dama) &&
                    compare(hrs, detail.hrs) && compare(mat, detail.mat) &&
                    compare(tpc, detail.tpc) && compare(sud, detail.sud);
        }
        return false;
    }

    public String[] mas_acc()
    {
        return TextUtils.isEmpty(mas_acc)? Common.EMPTY_STR_ARRAY: mas_acc.split("\\|");
    }

    public String[] mas_macc()
    {
        return TextUtils.isEmpty(mas_macc)? new String[] { "M", "" }: mas_macc.split("\\|");
    }

    /****************************************/
    // TODO Compare
    /****************************************/

    boolean compare(String value1, String value2)
    {
        if (value1 == null) value1 = "";
        if (value2 == null) value2 = "";
        return value1.equals(value2);
    }

    boolean compare(double value1, double value2)
    {
        return value1 == value2;
    }

    /****************************************/
    // TODO Implement interfaces
    /****************************************/

    @Override
    public int compareTo(@NonNull Detail detail)
    {
        return sud - detail.sud;
    }

    @Override
    public String getTitle()
    {
        return String.format(Locale.getDefault(), "%s-%02d", cont, position + 1);
    }

    @Override
    public String getName(int position, String timestamp)
    {
        return String.format(Locale.getDefault(), "%s-%s-%s%02d", sud, getTitle(), timestamp, position + 1);
    }

    @Override
    public Folder getFolder(Context context)
    {
        return Folder.data(context, true).folder(cont).folder((enquiry? "e": "") + "detail-" + sud);
    }
}
