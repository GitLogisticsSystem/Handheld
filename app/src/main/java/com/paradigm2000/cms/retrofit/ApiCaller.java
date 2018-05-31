package com.paradigm2000.cms.retrofit;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.JsonObject;
import com.paradigm2000.cms.BuildConfig;
import com.paradigm2000.cms.app.MyPref_;
import com.paradigm2000.cms.app.PhotoGroup;
import com.paradigm2000.cms.gson.Booking;
import com.paradigm2000.cms.gson.Container;
import com.paradigm2000.cms.gson.Detail;
import com.paradigm2000.cms.gson.Enquiry;
import com.paradigm2000.cms.gson.Header;
import com.paradigm2000.cms.gson.Repair;
import com.paradigm2000.cms.gson.Summary;
import com.paradigm2000.cms.gson.User;
import com.paradigm2000.core.Common;
import com.paradigm2000.core.retrofit.Factory;
import com.paradigm2000.core.retrofit.Logger;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;

@EBean(scope=EBean.Scope.Singleton)
public class ApiCaller
{
    static final boolean DEBUG = BuildConfig.debug;
    static final String TAG = "ApiCaller";

    @RootContext
    Context context;
    @Pref
    MyPref_ pref;

    String serverUrl;
    long[] timeouts;
    OkHttpClient mClient;
    IWebServices services;

    @AfterInject
    void afterInject()
    {
        serverUrl = pref.ServerURL().getOr("");
        serverUrl = (serverUrl.contains("://") ? "" : "http://") + serverUrl + "/" + pref.AppPath().getOr("sml") + "_app/";
        int value = pref.Timeout().get();
        timeouts = new long[] { (int) (value / 1.5), value };
        update();
    }

    public void timeout(long... timeouts)
    {
        if (timeouts.length == 1)
        {
            timeout(timeouts[0], timeouts[0]);
        }
        else if (timeouts.length > 1)
        {
            this.timeouts = timeouts;
            update();
        }
    }

    public void updateServerUrl()
    {
        serverUrl = pref.ServerURL().getOr("");
        serverUrl = (serverUrl.contains("://") ? "" : "http://") + serverUrl + "/" + pref.AppPath().getOr("sml") + "_app/";
        update();
    }

    private void update()
    {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(Logger.create())
                .connectTimeout(timeouts[0], TimeUnit.SECONDS)
                .readTimeout(timeouts[1], TimeUnit.SECONDS);
        if (BuildConfig.debug) builder.addNetworkInterceptor(new StethoInterceptor());
        mClient = builder.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .addConverterFactory(Factory.create(context))
                .client(mClient)
                .build();
        services = retrofit.create(IWebServices.class);
    }

    public Call<User> login(String usercode, String password)
    {
        JsonObject body = new JsonObject();
        body.addProperty("usercode", usercode);
        body.addProperty("password", password);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.login(body);
    }

    public Call<Header[]> list_inspection()
    {
        JsonObject body = new JsonObject();
        body.addProperty("usercode", pref.username().get());
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.list_inspection(body);
    }

    public Call<Container[]> list_container(String input)
    {
        JsonObject body = new JsonObject();
        body.addProperty("usercode", pref.username().get());
        body.addProperty("cont", input);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.list_container(body);
    }

    public Call<Repair[]> list_repair(String input)
    {
        JsonObject body = new JsonObject();
        body.addProperty("usercode", pref.username().get());
        body.addProperty("cont", input);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.list_repair(body);
    }

    public Call<Boolean> link(Header header)
    {
        JsonObject body = new JsonObject();
        body.addProperty("usercode", pref.username().get());
        body.addProperty("prev_suh", header.prev_suh);
        body.addProperty("current_suh", header.ref);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.link(body);
    }

    public Call<String[]> getAcc(Header header)
    {
        JsonObject body = new JsonObject();
        body.addProperty("suh", header.ref);
        body.addProperty("rtable", header.rtable);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.getAcc(body);
    }

    public Call<String[]> getMacc(Header header)
    {
        JsonObject body = new JsonObject();
        body.addProperty("suh", header.ref);
        body.addProperty("rtable", header.rtable);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.getMacc(body);
    }

    public Call<Detail[]> details(Header header)
    {
        JsonObject body = new JsonObject();
        body.addProperty("suh", header.ref);
        body.addProperty("rtable", header.rtable);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.details(body);
    }

    public Call<Summary[]> summary(Header header)
    {
        JsonObject body = new JsonObject();
        body.addProperty("suh", header.ref);
        body.addProperty("rtable", header.rtable);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.summary(body);
    }

    public Call<String> getDesc(String cpn, String rp)
    {
        JsonObject body = new JsonObject();
        body.addProperty("cpn", cpn);
        body.addProperty("rpc", rp);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.getDesc(body);
    }

    public Call<String[]> findSizeType(String iso)
    {
        JsonObject body = new JsonObject();
        body.addProperty("iso", iso);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.findSizeType(body);
    }

    public Call<String[]> listStd()
    {
        return services.listStd(new JsonObject());
    }

    public Call<Booking> queryBooking(String cont)
    {
        JsonObject body = new JsonObject();
        body.addProperty("cont", cont);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.queryBooking(body);
    }

    public Call<Integer> addHeader(Header header)
    {
        JsonObject body = new JsonObject();
        body.addProperty("usercode", pref.username().get());
        body.addProperty("gat", "");
        body.addProperty("xoper", header.oper.toUpperCase(Locale.ENGLISH));
        body.addProperty("grade", header.grade);
        body.addProperty("xcont", header.cont.toUpperCase(Locale.ENGLISH));
        body.addProperty("xsize", header.size);
        body.addProperty("xtype", header.type.toUpperCase(Locale.ENGLISH));
        body.addProperty("iso", header.iso.toUpperCase(Locale.ENGLISH));
        body.addProperty("mdte", header.mdte);
        body.addProperty("mgw", header.mgw);
        body.addProperty("nwgt", header.nwgt);
        body.addProperty("tare", header.tare);
        body.addProperty("stat", header.stat);
        body.addProperty("std", header.std);
        body.addProperty("intrmk", header.remark);
        body.addProperty("rtable", "");
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.addHeader(body);
    }

    public Call<Boolean> delHeader(Header header)
    {
        JsonObject body = new JsonObject();
        body.addProperty("suh", header.ref);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.delHeader(body);
    }

    public Call<Header> checkContainer(String oper, String cont)
    {
        JsonObject body = new JsonObject();
        body.addProperty("usercode", pref.username().get());
        body.addProperty("oper", oper);
        body.addProperty("cont", cont);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.chkCont(body);
    }

    public Call<String> checkOperator(String oper)
    {
        JsonObject body = new JsonObject();
        body.addProperty("xoper", oper);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.chkOper(body);
    }

    public Call<Boolean> updateHeader(Header header)
    {
        JsonObject body = new JsonObject();
        body.addProperty("usercode", pref.username().get());
        body.addProperty("gat", header.ref);
        body.addProperty("iso", header.iso.toUpperCase(Locale.ENGLISH));
        body.addProperty("grade", header.grade);
        body.addProperty("mdte", header.mdte);
        body.addProperty("size", header.size);
        body.addProperty("type", header.type);
        body.addProperty("mgw", header.mgw);
        body.addProperty("tare", header.tare);
        body.addProperty("nwgt", header.nwgt);
        body.addProperty("stat", header.stat);
        body.addProperty("std", header.std);
        body.addProperty("intrmk", header.remark);
        body.addProperty("rtable", header.rtable);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.updateHeader(body);
    }

    public Call<double[]> getCharge(Header header, String cpn, String rp, String s1, String s2, String qty, String loca, String dmcode, String macc, String estm)
    {
        JsonObject body = new JsonObject();
        body.addProperty("gat", header.ref);
        body.addProperty("cpn", cpn);
        body.addProperty("rpc", rp);
        body.addProperty("s1", TextUtils.isEmpty(s1)? 0: Integer.parseInt(s1));
        body.addProperty("s2", TextUtils.isEmpty(s2)? 0: Integer.parseInt(s2));
        body.addProperty("qty", TextUtils.isEmpty(qty)? 1: Integer.parseInt(qty));
        body.addProperty("loca", loca);
        body.addProperty("xmcode", dmcode);
        body.addProperty("xmachine", macc);
        body.addProperty("rtable", header.rtable);
        body.addProperty("estm", estm);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.getCharge(body);
    }

    public Call<Integer> updateDetail(Header header, Detail detail)
    {
        JsonObject body = new JsonObject();
        body.addProperty("usercode", pref.username().get());
        body.addProperty("gat", header.ref);
        if (detail.sud == -1) body.addProperty("sud", ""); else body.addProperty("sud", detail.sud);
        body.addProperty("acc", detail.acc);
        body.addProperty("macc", detail.macc);
        body.addProperty("cpn", detail.cpn);
        body.addProperty("rpc", detail.rpc);
        body.addProperty("desc", detail.description);
        body.addProperty("s1", detail.s1);
        body.addProperty("s2", detail.s2);
        body.addProperty("qty", detail.qty == 0? 1: detail.qty);
        body.addProperty("loca", detail.loca);
        body.addProperty("dama", detail.dama);
        body.addProperty("hrs", detail.hrs);
        body.addProperty("mat", detail.mat);
        body.addProperty("tpc", detail.tpc);
        body.addProperty("estm", detail.estm);
        body.addProperty("rtable", header.rtable);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.updateDetail(body);
    }
    public Call<Boolean> updateSpecific(Container container, String field, String value)
    {
        JsonObject body = new JsonObject();

        body.addProperty("usercode", pref.username().get());
        body.addProperty("mkey", container.mkey);
        body.addProperty("utype", field);
        body.addProperty("val", value);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.updateSpecific(body);
    }

    public Call<Boolean> deleteDetail(Detail detail)
    {
        JsonObject body = new JsonObject();
        body.addProperty("sud", detail.sud);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.deleteDetail(body);
    }

    public Call<Boolean> complete_inspect(Header header)
    {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        builder.addFormDataPart("gat", String.valueOf(header.ref));
        builder.addFormDataPart("rtable", String.valueOf(header.rtable));
        if (Common.get().isExternalStorageAvailable())
        {
            bindPhotos(header, builder);
            Detail[] details = header.details;
            if (details != null && details.length > 0)
            {
                for (int i = 0, length = header.details.length; i < length; i += 1)
                {
                    Detail detail = details[i];
                    detail.position = i;
                    bindPhotos(detail, builder);
                }
            }
        }
        return services.complete_inspect(builder.build());
    }

    public Call<Boolean> complete_repair(Repair repair, String subc)
    {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        builder.addFormDataPart("usercode", pref.username().get());
        builder.addFormDataPart("mkey", repair.mkey);
        builder.addFormDataPart("eth", String.valueOf(repair.eth));
        builder.addFormDataPart("subc", subc);
        if (Common.get().isExternalStorageAvailable()) bindPhotos(repair, builder);
        return services.complete_repair(builder.build());
    }

    public Call<Enquiry[]> enquiries(Container container)
    {
        JsonObject body = new JsonObject();
        body.addProperty("usercode", pref.username().get());
        body.addProperty("mkey", container.mkey);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.enquiries(body);
    }

    public Call<Boolean> print(Enquiry enquiry)
    {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        builder.addFormDataPart("usercode", pref.username().get());
        builder.addFormDataPart("eth", String.valueOf(enquiry.est_ref));
        return services.print(builder.build());
    }

    public Call<Boolean> approve(Enquiry enquiry)
    {
        JsonObject body = new JsonObject();
        body.addProperty("usercode", pref.username().get());
        body.addProperty("eth", enquiry.est_ref);
        return services.approve(body);
    }

    public Call<String[]> getAcc(Container container)
    {
        JsonObject body = new JsonObject();
        body.addProperty("mkey", container.mkey);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.getAccCTN(body);
    }

    public Call<String[]> getMacc(Container container)
    {
        JsonObject body = new JsonObject();
        body.addProperty("mkey", container.mkey);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.getMaccCTN(body);
    }

    public Call<Detail[]> details(Container container)
    {
        JsonObject body = new JsonObject();
        body.addProperty("mkey", container.mkey);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.detailsCTN(body);
    }

    public Call<double[]> getCharge(Container container, String cpn, String rp, String s1, String s2, String qty, String loca, String dmcode, String macc, String estm)
    {
        JsonObject body = new JsonObject();
        body.addProperty("mkey", container.mkey);
        body.addProperty("cpn", cpn);
        body.addProperty("rpc", rp);
        body.addProperty("s1", TextUtils.isEmpty(s1)? 0: Integer.parseInt(s1));
        body.addProperty("s2", TextUtils.isEmpty(s2)? 0: Integer.parseInt(s2));
        body.addProperty("qty", TextUtils.isEmpty(qty)? 1: Integer.parseInt(qty));
        body.addProperty("loca", loca);
        body.addProperty("xmcode", dmcode);
        body.addProperty("xmachine", macc);
        body.addProperty("estm", estm);
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.getChargeCTN(body);
    }

    public Call<Integer> updateDetail(Container container, Detail detail)
    {
        JsonObject body = new JsonObject();
        body.addProperty("usercode", pref.username().get());
        body.addProperty("mkey", container.mkey);
        if (detail.sud == -1) body.addProperty("sud", ""); else body.addProperty("sud", detail.sud);
        body.addProperty("acc", detail.acc);
        body.addProperty("macc", detail.macc);
        body.addProperty("cpn", detail.cpn);
        body.addProperty("rpc", detail.rpc);
        body.addProperty("desc", detail.description);
        body.addProperty("s1", detail.s1);
        body.addProperty("s2", detail.s2);
        body.addProperty("qty", detail.qty == 0? 1: detail.qty);
        body.addProperty("loca", detail.loca);
        body.addProperty("dama", detail.dama);
        body.addProperty("hrs", detail.hrs);
        body.addProperty("mat", detail.mat);
        body.addProperty("tpc", detail.tpc);
        body.addProperty("estm", "");
        if (DEBUG) Log.i(TAG, "params: " + body);
        return services.updateDetailCTN(body);
    }

    public Call<Boolean> complete_container(Container container)
    {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        builder.addFormDataPart("mkey", container.mkey);
        builder.addFormDataPart("std", container.std == null? "": container.std);
        if (Common.get().isExternalStorageAvailable())
        {
            bindPhotos(container, builder);
            Detail[] details = container.details;
            if (details != null && details.length > 0) for (Detail detail: details) bindPhotos(detail, builder);
        }
        return services.complete_container(builder.build());
    }

    private void bindPhotos(PhotoGroup group, MultipartBody.Builder builder)
    {
        MediaType mediaType = MediaType.parse("image/jpg");
        File[] files = group.getFolder(context).listFiles();
        if (files == null || files.length == 0) return;
        for (int i = 0, length = files.length; i < length; i += 1)
        {
            File file = files[i];
            int index = file.getName().lastIndexOf(".");
            String timestamp = file.getName().substring(0, index);
            String name = group.getName(i, timestamp) + ".jpg";
            Log.i("test", "name = " + name);
            builder.addFormDataPart(name, name, RequestBody.create(mediaType, files[i]));
        }
    }

    public Call<String[]> listSubcon()
    {
        return services.list_subcon(new JsonObject());
    }

    public Call<String[]> listOper()
    {
        return services.list_oper(new JsonObject());
    }
}
