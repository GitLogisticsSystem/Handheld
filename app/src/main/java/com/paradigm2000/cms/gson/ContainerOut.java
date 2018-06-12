package com.paradigm2000.cms.gson;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.paradigm2000.cms.BuildConfig;
import com.paradigm2000.cms.app.PhotoGroup;
import com.paradigm2000.core.Common;
import com.paradigm2000.core.io.Folder;

import org.parceler.Parcel;
import org.parceler.Transient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@Parcel
public class ContainerOut implements PhotoGroup
{
    static final boolean DEBUG = BuildConfig.debug;
    static final String TAG = "ContainerOut";

    public static String[] def_stat_list = new String[] { "AV", "DM" };
    public static String[] def_std_list = Common.EMPTY_STR_ARRAY;

    public int ref;
    public String oper;
    public String grade;
    public String cont;
    public String size;
    public String type;
    public String stat;
    public String time1;
    public String iso;
    public int gat;
    public String nwgt;
    public int mgw;
    public String trac;
    public int tare;
    public String mdte;
    public String std;
    public String remark;

    public String rtable;
    public String status;
    String std_list;
    String stat_list;
    public String[] acc_list;
    public String[] macc_list;
    public Summary[] summaries = new Summary[0];
    public Detail[] details = new Detail[0];

    @Transient
    transient SimpleDateFormat formatter = new SimpleDateFormat("", Locale.getDefault());

    public Detail newDetail()
    {
        Detail detail = new Detail();
        detail.acc = acc_list[0];
        detail.cont = cont;
        return detail;
    }

    @Override @SuppressWarnings("CloneDoesntCallSuperClone")
    public ContainerOut clone()
    {
        ContainerOut containerout = new ContainerOut();
        containerout.gat = gat;
        containerout.ref = ref;
        containerout.oper = oper;
        containerout.grade = grade;
        containerout.cont = cont;
        containerout.size = size;
        containerout.type = type;
        containerout.time1 = time1;
        containerout.nwgt = nwgt;
        containerout.std_list = std_list;
        containerout.stat_list = stat_list;
        containerout.acc_list = acc_list;
        containerout.macc_list = macc_list;
        containerout.details = details;
        containerout.rtable = rtable;
        containerout.trac = trac;
        return containerout;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof ContainerOut)
        {
            ContainerOut containerout = (ContainerOut) obj;
            return compare(iso, containerout.iso) && compare(mdte, containerout.mdte) &&
                    compare(mgw, containerout.mgw) && compare(tare, containerout.tare) &&
                    compare(stat, containerout.stat) && compare(std, containerout.std) &&
                    compare(remark, containerout.remark) && compare(ref, containerout.ref) &&
                    compare(nwgt,containerout.nwgt)&&
                    compare(oper, containerout.oper) && compare(cont, containerout.cont) &&
                    compare(size, containerout.size) && compare(type, containerout.type) &&
                    compare(time1, containerout.time1) && compare(grade, containerout.grade);
        }
        return false;
    }

    public String[] stat_list()
    {
        return TextUtils.isEmpty(stat_list)? def_stat_list: stat_list.split("\\|");
    }

    public String[] std_list()
    {
        return TextUtils.isEmpty(std_list)? def_std_list: std_list.split("\\|");
    }

    public boolean isSUH()
    {
        return "SUH".equals(rtable);
    }

    public boolean isCompleted()
    {
        return isSUH() && "C".equals(status);
    }

    public Calendar getCalendar(String input)
    {
        Date date = new Date();
        input = TextUtils.isEmpty(input)? mdte: input;
        if (input != null && (input.length() != 4 || input.length() != 6))
        {
            try
            {
                formatter.applyPattern("MMyy" + (input.length() == 6? "yy": ""));
                if (!TextUtils.isEmpty(input)) date = formatter.parse(input);
            }
            catch (Exception e)
            {
                if (DEBUG) Log.w(TAG, "Fail to parse @" + input, e);
            }
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
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

    boolean compare(int value1, int value2)
    {
        return value1 == value2;
    }

    /****************************************/
    // TODO Implement interfaces
    /****************************************/

    @Override
    public String getTitle()
    {
        return cont;
    }

    @Override
    public String getName(int position, String timestamp)
    {
        return String.format(Locale.getDefault(), "%s-00-%s%02d", cont, timestamp, position);
    }

    @Override @SuppressWarnings("ConstantConditions")
    public Folder getFolder(Context context)
    {
        return Folder.data(context, true).folder(trac).folder("containerout");
    }
}
