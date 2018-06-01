package com.paradigm2000.cms;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.paradigm2000.cms.gson.Container;
import com.paradigm2000.cms.gson.Detail;
import com.paradigm2000.cms.gson.Enquiry;
import com.paradigm2000.cms.retrofit.ApiCaller;
import com.paradigm2000.cms.widget.EnquiryAdapter;
import com.paradigm2000.core.Activity;
import com.paradigm2000.core.Common;
import com.paradigm2000.core.dialog.Dialog;
import com.paradigm2000.core.dialog.Dialog2;
import com.paradigm2000.core.io.Folder;
import com.paradigm2000.core.retrofit.InternalServerError;
import com.paradigm2000.core.retrofit.RetrofitUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.BeforeTextChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.DimensionPixelSizeRes;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.Executor;

@EActivity(R.layout.activity_enquiry) @OptionsMenu(R.menu.menu_enquiry)
public class EnquiryActivity extends Activity implements EnquiryAdapter.EnquiryListener
{
    static final int REQUEST = 0x008622;
    static final String TAG = "Enquiry_";
    static final boolean DEBUG = BuildConfig.debug;

    @OptionsMenuItem(R.id.photos)
    MenuItem _photos;
    @OptionsMenuItem(R.id.refresh)
    MenuItem _refresh;
    @ViewById(R.id.oper)
    EditText _oper;
    @ViewById(R.id.sys)
    EditText _sys;
    @ViewById(R.id.esttot)
    EditText _esttot;
    @ViewById(R.id.brmk)
    EditText _brmk;
    @ViewById(R.id.loc)
    EditText _loc;
    @ViewById(R.id.stat)
    Button _status;
    @ViewById(R.id.upload)
    Button _upload;
    @ViewById(R.id.update1)
    Button _update1;
    @ViewById(R.id.update2)
    Button _update2;
    @ViewById(R.id.estimate)
    Button _estimate;
    @ViewById(R.id.details)
    RecyclerView _details;
    @Bean
    ApiCaller api;
    @Bean
    RetrofitUtil util;
    @Bean
    EnquiryAdapter enquiryAdapter;
    @Extra("Enquiry_container") @InstanceState
    Container container;
    @DimensionPixelSizeRes(R.dimen.dp32)
    int dp32;
    @InstanceState
    boolean inited = false;
    @SystemService
    Vibrator vibrator;

    BaseBitmapDataSubscriber subscriber = new BaseBitmapDataSubscriber()
    {
        @Override
        protected void onNewResultImpl(@javax.annotation.Nullable Bitmap bitmap)
        {
            _photos.setIcon(new BitmapDrawable(getResources(), bitmap));
            _upload.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource)
        {
            _photos.setIcon(R.drawable.ic_add_a_photo_white_24dp);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setDisplayHomeAsUpEnabled(true);
    }

    @AfterInject
    void afterInject()
    {
        setTitle(container.cont);
    }

    @AfterViews
    void afterViews()
    {

        showEnquiry();
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        _details.setLayoutManager(manager);
        _details.setAdapter(enquiryAdapter);
        enquiryAdapter.setListener(this);
        _loc.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7),new InputFilter.AllCaps()});
    }

    @Override
    protected void onRefresh()
    {
        refresh();
        updateImageMenuItem();
        if (!inited) inited = true;
    }

    @OnActivityResult(REQUEST)
    void onPhotoAdded(int resultCode)
    {
        if (resultCode == RESULT_OK)
        {
            updateImageMenuItem();
        }
        else if (!Common.get().isExternalStorageAvailable())
        {
            vibrator.vibrate(Common.PATTERN, -1);
            Toast.makeText(this, R.string.no_external, Toast.LENGTH_SHORT).show();
        }
    }

    void updateImageMenuItem()
    {
        if (Common.get().isExternalStorageAvailable())
        {
            Folder folder = container.getFolder(this);
            File[] files = folder.listFiles();
            if (files == null || files.length == 0)
            {
                _photos.setIcon(R.drawable.ic_add_a_photo_white_24dp);
                _upload.setVisibility(View.GONE);
            }
            else
            {
                ImageRequest request = ImageRequest.fromFile(files[0]);
                DataSource<CloseableReference<CloseableImage>> dataSource = Fresco.getImagePipeline().fetchDecodedImage(request, this);
                Executor executor = UiThreadImmediateExecutorService.getInstance();
                dataSource.subscribe(subscriber, executor);
            }
        }
        else
        {
            Toast.makeText(this, R.string.no_external, Toast.LENGTH_SHORT).show();
            vibrator.vibrate(Common.PATTERN, -1);
        }
    }

    void showEnquiry()
    {
        _oper.setText(container.oper);
        _sys.setText(container.sys);
        _esttot.setText(String.valueOf(container.esttot));
        _brmk.setText(container.rmk);
        _status.setText(container.stat);
        _loc.setText(container.loca);
    }

    String last4Digits()
    {
        String cont = container.cont;
        return cont.substring(cont.length() - 4);
    }

    Container findContainer(Container[] containers)
    {
        for (Container container: containers)
        {
            if (this.container.cont.equals(container.cont)) return container;
        }
        return null;
    }

    /****************************************/
    // TODO UI events
    /****************************************/


    @OptionsItem(R.id.photos)
    void addPhoto()
    {
        PhotoActivity_.intent(this)
                .header(container)
                .startForResult(REQUEST);
    }

    @OptionsItem(R.id.refresh)
    void refresh()
    {
        _refresh.setActionView(R.layout.menuitem_loading);
        enquiryAdapter.setEnabled(false);
        _upload.setEnabled(false);
        doRefresh(inited? null: container);
    }

    @Click(R.id.upload)
    void upload()
    {
        new Dialog(this, Dialog.PROMPT_TYPE)
                .setTitle(container.cont)
                .setContentRes(R.string.confirm, getString(R.string.upload).toLowerCase(Locale.ENGLISH))
                .setConfirm(R.string.submit)
                .setConfirmListener(new Dialog.OnClickListener()
                {
                    @Override
                    public void onClick(Dialog dialog)
                    {
                        dialog.changeType(Dialog.PROGRESS_TYPE)
                                .setCloseOnClick(false)
                                .setCancelOnBack(false)
                                .setContentRes(R.string.LOADING);
                        doUpload(dialog);
                    }
                })
                .setCloseOnClick(false)
                .show();
    }

    @Click(R.id.stat)
    void chooseStatus()
    {
        new Dialog2(this, Dialog2.LIST_TYPE)
                .setTitle(container.cont)
                .setSelectedItem(_status.getText().toString())
                .setItems(container.stat_list(), new Dialog2.OnClickListener()
                {
                    @Override
                    public void onClick(Dialog2 dialog)
                    {
                        String item = dialog.getSelectedItem().toString();
                        _status.setText(item);
                    }
                })
                .showCancel(true)
                .show();

    }
    @Click(R.id.update1)
    void updateSpec1()
    {
        new Dialog(this, Dialog.PROMPT_TYPE)
                .setTitle(container.cont)
                .setContentRes(R.string.confirm, getString(R.string.update).toLowerCase(Locale.ENGLISH))
                .setConfirm(R.string.submit)
                .setConfirmListener(new Dialog.OnClickListener()
                {
                    @Override
                    public void onClick(Dialog dialog)
                    {
                        dialog.changeType(Dialog.PROGRESS_TYPE)
                                .setCloseOnClick(false)
                                .setCancelOnBack(false)
                                .setContentRes(R.string.LOADING);
                        doUpdate(container,"STAT",_status.getText().toString(),dialog);
                    }
                })
                .setCloseOnClick(false)
                .show();
    }
    @Click(R.id.update2)
    void updateSpec2()
    {
        new Dialog(this, Dialog.PROMPT_TYPE)
                .setTitle(container.cont)
                .setContentRes(R.string.confirm, getString(R.string.update).toLowerCase(Locale.ENGLISH))
                .setConfirm(R.string.submit)
                .setConfirmListener(new Dialog.OnClickListener()
                {
                    @Override
                    public void onClick(Dialog dialog)
                    {
                        dialog.changeType(Dialog.PROGRESS_TYPE)
                                .setCloseOnClick(false)
                                .setCancelOnBack(false)
                                .setContentRes(R.string.LOADING);
                        doUpdate(container,"LOCA",_loc.getText().toString(),dialog);
                    }
                })
                .setCloseOnClick(false)
                .show();
    }



    @Click(R.id.estimate)
    void estimate()
    {
        DetailActivity_.intent(this)
                .container(container)
                .start();
    }

    /****************************************/
    // TODO Implement interfaces
    /****************************************/

    @Override
    public void approve(final Enquiry item, int position)
    {
        new Dialog(this, Dialog.PROMPT_TYPE)
                .setTitle(container.cont)
                .setContentRes(R.string.confirm, getString(R.string.approve).toLowerCase(Locale.ENGLISH) + " " + item.est_no)
                .setConfirm(R.string.print)
                .setConfirmListener(new Dialog.OnClickListener()
                {
                    @Override
                    public void onClick(Dialog dialog)
                    {
                        doApprove(item, dialog);
                    }
                })
                .show();
    }

    @Override
    public void print(final Enquiry item, int position)
    {
        new Dialog(this, Dialog.PROMPT_TYPE)
                .setTitle(container.cont)
                .setContentRes(R.string.confirm, getString(R.string.print).toLowerCase(Locale.ENGLISH) + " " + item.est_no)
                .setConfirm(R.string.print)
                .setConfirmListener(new Dialog.OnClickListener()
                {
                    @Override
                    public void onClick(Dialog dialog)
                    {
                        doPrint(item, dialog);
                    }
                })
                .show();
    }

    /****************************************/
    // TODO POST listCTN, getAcc, getMacc, listCTNEst
    /****************************************/

    @Background
    void doRefresh(Container container)
    {
        IOException error = null;
        try
        {
            if (container == null)
            {
                Container[] enquiries = api.list_container(last4Digits()).execute().body();
                if (enquiries == null) error = util.createError();
                else container = findContainer(enquiries);
            }
            if (container != null)
            {
                if ("AET".equals((this.container = container).sys))
                {
                    container.acc_list = api.getAcc(container).execute().body();
                    if (container.acc_list == null) error = util.createError();
                    container.macc_list = api.getMacc(container).execute().body();
                    if (container.macc_list == null) error = util.createError();
                    container.details = api.details(container).execute().body();
                    if (container.details == null) error = util.createError();
                    else
                    {
                        Arrays.sort(container.details);
                        for (Detail detail: container.details)
                        {
                            detail.enquiry = true;
                            detail.cont = container.cont;
                        }
                    }
                }
                else
                {
                    Enquiry[] result = api.enquiries(container).execute().body();
                    if (result == null) error = util.createError();
                    else container.enquiries = result;
                }
            }
        }
        catch (UnknownHostException e)
        {
            if (!isDestroyed()) util.noNetwork(this);
        }
        catch (SocketTimeoutException e)
        {
            if (!isDestroyed()) util.timeout(this);
        }
        catch (IOException e)
        {
            error = e;
        }
        if (!isDestroyed()) afterRefresh(error, container);
    }

    @UiThread
    void afterRefresh(IOException error, Container result)
    {
        if (error != null)
        {
            util.error(this, container.cont, "Enquiry_refresh", error);
        }
        else if (result != null)
        {
            if ("AET".equals(container.sys))
            {

                _estimate.setVisibility(View.VISIBLE);

            }
            else
            {

                _estimate.setVisibility(View.GONE);
                enquiryAdapter.apply(result.enquiries);
            }
            showEnquiry();
            _upload.setEnabled(true);
        }
        else
        {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            finish();
        }
        enquiryAdapter.setEnabled(true);
        _refresh.setActionView(null);
    }

    /****************************************/
    // TODO POST printJob.ashx
    /****************************************/

    @Background
    void doPrint(Enquiry enquiry, Dialog dialog)
    {
        IOException error = null;
        Boolean result = null;
        try
        {
            result = api.print(enquiry).execute().body();
            if (result == null) error = util.createError();
        }
        catch (UnknownHostException e)
        {
            if (!isDestroyed()) util.noNetwork(this);
        }
        catch (SocketTimeoutException e)
        {
            if (!isDestroyed()) util.timeout(this);
        }
        catch (IOException e)
        {
            error = e;
        }
        if (!isDestroyed()) afterPrint(error, result, dialog);
    }

    @UiThread
    void afterPrint(IOException error, Boolean result, Dialog dialog)
    {
        if (error != null)
        {
            vibrator.vibrate(Common.PATTERN, -1);
            dialog.changeType(Dialog.ERROR_TYPE).setContent(error.getMessage());
            if (error instanceof InternalServerError) util.report("Enquiry_print", error);
        }
        else if (result != null)
        {
            dialog.changeType(Dialog.SUCCESS_TYPE).setContentRes(R.string.print_success);
            refresh();
        }
        else
        {
            dialog.dismissWithAnimation();
        }
    }

    /****************************************/
    // TODO POST AppEst
    /****************************************/

    @Background
    void doApprove(Enquiry enquiry, Dialog dialog)
    {
        IOException error = null;
        Boolean result = null;
        try
        {
            result = api.approve(enquiry).execute().body();
            if (result == null) error = util.createError();
        }
        catch (UnknownHostException e)
        {
            if (!isDestroyed()) util.noNetwork(this);
        }
        catch (SocketTimeoutException e)
        {
            if (!isDestroyed()) util.timeout(this);
        }
        catch (IOException e)
        {
            error = e;
        }
        if (!isDestroyed()) afterApprove(error, result, dialog);
    }

    @UiThread
    void afterApprove(IOException error, Boolean result, Dialog dialog)
    {
        if (error != null)
        {
            vibrator.vibrate(Common.PATTERN, -1);
            dialog.changeType(Dialog.ERROR_TYPE).setContent(error.getMessage());
            if (error instanceof InternalServerError) util.report("Enquiry_approve", error);
        }
        else if (result != null)
        {
            dialog.changeType(Dialog.SUCCESS_TYPE).setContentRes(R.string.success);
            refresh();
        }
        else
        {
            dialog.dismissWithAnimation();
        }
    }
    /****************************************/
    // TODO POST uptCTN.ashx
    /****************************************/
    @Background
    void doUpdate(Container container,String field1,String value, Dialog dialog)
    {
        IOException error = null;
        Boolean result = null;
        try
        {
            result = api.updateSpecific(container,field1,value).execute().body();
            if (result == null) error = util.createError();
        }
        catch (UnknownHostException e)
        {
            if (!isDestroyed()) util.noNetwork(this);
        }
        catch (SocketTimeoutException e)
        {
            if (!isDestroyed()) util.timeout(this);
        }
        catch (IOException e)
        {
            error = e;
        }
        if (!isDestroyed()) afterUpdate(error, result, dialog);
    }

    @UiThread
    void afterUpdate(IOException error, Boolean result, Dialog dialog)
    {
        if (error != null)
        {
            vibrator.vibrate(Common.PATTERN, -1);
            dialog.changeType(Dialog.ERROR_TYPE).setContent(error.getMessage());
            if (error instanceof InternalServerError) util.report("Enquiry_Update", error);
        }
        else if (result != null)
        {
            dialog.dismissWithAnimation();
            dialog.changeType(Dialog.SUCCESS_TYPE).setContentRes(R.string.success);
            //Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show();
            refresh();
        }
        else
        {
            dialog.dismissWithAnimation();
        }
    }
    /****************************************/
    // TODO POST uploadPhoto_CTN.ashx
    /****************************************/

    @Background
    void doUpload(Dialog dialog)
    {
        Boolean result = null;
        IOException error = null;
        try
        {
            result = api.complete_container(container).execute().body();
            if (result == null) error = util.createError();
        }
        catch (UnknownHostException e)
        {
            if (!isDestroyed()) util.noNetwork(this);
        }
        catch (SocketTimeoutException e)
        {
            if (!isDestroyed()) util.timeout(this);
        }
        catch (IOException e)
        {
            error = e;
        }
        if (!isDestroyed()) afterUpload(error, result, dialog);
    }

    @UiThread
    void afterUpload(IOException error, Boolean result, Dialog dialog)
    {
        if (error != null)
        {
            vibrator.vibrate(Common.PATTERN, -1);
            dialog.changeType(Dialog.ERROR_TYPE).setContent(error.getMessage());
            if (error instanceof InternalServerError) util.report("Enquiry_upload", error);
        }
        else if (result != null)
        {
            dialog.dismissWithAnimation();
            Folder folder = container.getFolder(this);
            if (!folder.delete() && DEBUG) Log.w(TAG, "Fail to delete @" + folder);
            Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show();
            updateImageMenuItem();
        }
        else
        {
            dialog.dismissWithAnimation();
        }
    }
}
