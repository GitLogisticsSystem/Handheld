package com.paradigm2000.cms;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.paradigm2000.cms.event.SelectDetailEvent;
import com.paradigm2000.cms.gson.Container;
import com.paradigm2000.cms.gson.Detail;
import com.paradigm2000.cms.gson.Header;
import com.paradigm2000.cms.gson.Summary;
import com.paradigm2000.cms.retrofit.ApiCaller;
import com.paradigm2000.cms.widget.DetailAdapter;
import com.paradigm2000.cms.widget.SummaryAdapter;
import com.paradigm2000.core.Activity;
import com.paradigm2000.core.Common;
import com.paradigm2000.core.bean.EventBus;
import com.paradigm2000.core.dialog.Dialog;
import com.paradigm2000.core.dialog.Dialog2;
import com.paradigm2000.core.io.Folder;
import com.paradigm2000.core.retrofit.InternalServerError;
import com.paradigm2000.core.retrofit.RetrofitUtil;
import com.paradigm2000.core.widget.RefreshView;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FocusChange;
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
import java.util.Locale;

@EActivity(R.layout.activity_detail) @OptionsMenu(R.menu.menu_detail) @SuppressWarnings("unused")
public class DetailActivity extends Activity
{
    static final boolean DEBUG = BuildConfig.debug;
    static final String TAG = "Detail";
    static final int REQUEST = 0x0064d2;

    @OptionsMenuItem(R.id.photos)
    MenuItem _photos;
    @ViewById(R.id.ac_title)
    TextView _acTitle;
    @ViewById(R.id.estm)
    EditText _estm;
    @ViewById(R.id.ac)
    EditText _ac;
    @ViewById(R.id.mac)
    EditText _mac;
    @ViewById(R.id.cpn)
    EditText _cpn;
    @ViewById(R.id.rp)
    EditText _rp;
    @ViewById(R.id.refresh)
    RefreshView _refresh;
    @ViewById(R.id.description)
    EditText _description;
    @ViewById(R.id.s1)
    EditText _s1;
    @ViewById(R.id.s2)
    EditText _s2;
    @ViewById(R.id.qty)
    EditText _qty;
    @ViewById(R.id.loca)
    EditText _loca;
    @ViewById(R.id.dmcode)
    EditText _dmcode;
    @ViewById(R.id.refresh2)
    RefreshView _refresh2;
    @ViewById(R.id.hr)
    EditText _hr;
    @ViewById(R.id.mat)
    EditText _mat;
    @ViewById(R.id.tpc)
    EditText _tpc;
    @ViewById(R.id.delete)
    Button _delete;
    @ViewById(R.id.save)
    Button _save;
    @ViewById(R.id.complete)
    Button _complete;
    @ViewById(R.id.section)
    View _section;
    @ViewById(R.id.summaries)
    RecyclerView _summaries;
    @ViewById(R.id.details)
    RecyclerView _details;
    @Bean
    ApiCaller api;
    @Bean
    EventBus bus;
    @Bean
    RetrofitUtil util;
    @Bean
    DetailAdapter detailAdapter;
    @Bean
    SummaryAdapter summaryAdapter;
    @Extra("Detail_header") @InstanceState
    Header header;
    @Extra("Detail_container") @InstanceState
    Container container;
    @InstanceState
    Detail selectedDetail;
    @DimensionPixelSizeRes(R.dimen.dp32)
    int dp32;
    @SystemService
    Vibrator vibrator;

    Header context;
    /*BaseBitmapDataSubscriber subscriber = new BaseBitmapDataSubscriber()
    {
        @Override
        protected void onNewResultImpl(@javax.annotation.Nullable Bitmap bitmap)
        {
            _photos.setIcon(new BitmapDrawable(getResources(), bitmap));
        }

        @Override
        protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource)
        {
            _photos.setIcon(R.drawable.ic_add_a_photo_white_24dp);
        }
    };*/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setDisplayHomeAsUpEnabled(true);
        bus.register(this);
    }

    @AfterInject
    void afterInject()
    {
        context = container != null? container: header;
        setTitle(context.cont);
    }

    @AfterViews
    void afterViews()
    {
        if (Common.get().isEmpty(context.macc_list))
        {
            _acTitle.setText(R.string.ac_cpn_rp);
            _mac.setVisibility(View.GONE);
        }
        else
        {
            _acTitle.setText(R.string.ac_mac_cpn_rp);
            _mac.setVisibility(View.VISIBLE);
        }
        Common.get().forceUppercase(_estm, _cpn, _rp, _description, _loca, _dmcode, _tpc);
        selectedDetail = context.isCompleted()? context.details[0]: selectedDetail == null? context.newDetail(): selectedDetail;
        if (context.isCompleted()) detailAdapter.select(selectedDetail);
        showDetail(selectedDetail);
        _complete.setVisibility(context instanceof Container? View.VISIBLE: View.GONE);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        _details.setLayoutManager(manager);
        _details.setAdapter(detailAdapter);
        detailAdapter.setHeader(context);
        detailAdapter.setRequest(REQUEST);
        detailAdapter.apply(context.details);
        _section.setVisibility(context instanceof Container? View.GONE: View.VISIBLE);
        if (!(context instanceof Container))
        {
            manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            _summaries.setLayoutManager(manager);
            _summaries.setAdapter(summaryAdapter);
            summaryAdapter.apply(context.summaries);
        }
        if (context.isCompleted())
        {
            setViewEditable(_estm, false);
            _ac.setEnabled(false);
            setViewEditable(_cpn, false);
            setViewEditable(_rp, false);
            _refresh.setVisibility(View.GONE);
            setViewEditable(_description, false);
            setViewEditable(_s1, false);
            setViewEditable(_s2, false);
            setViewEditable(_qty, false);
            setViewEditable(_loca, false);
            setViewEditable(_dmcode, false);
            _refresh2.setVisibility(View.GONE);
            setViewEditable(_hr, false);
            setViewEditable(_mat, false);
            setViewEditable(_tpc, false);
            detailAdapter.setCompleted(true);
        }
    }

    @Override
    protected void onRefresh()
    {
        updateImageMenuItem();
    }

    @OnActivityResult(REQUEST)
    void onPhotoAdded(int resultCode)
    {
        if (resultCode == RESULT_OK)
        {
            updateImageMenuItem();
            detailAdapter.notifyDataSetChanged();
        }
        else if (!Common.get().isExternalStorageAvailable())
        {
            vibrator.vibrate(Common.PATTERN, -1);
            Toast.makeText(this, R.string.no_external, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed()
    {
        if (context.isCompleted())
        {
            super.onBackPressed();
        }
        else if (detailAdapter.unselect())
        {
            int colorId = R.color.colorPrimary;
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, colorId)));
            showDetail(context.newDetail());
        }
        else if (!selectedDetail.equals(commit()))
        {
            new Dialog(this, Dialog.PROMPT_TYPE)
                    .setTitle(context.cont)
                    .setContentRes(R.string.confirm, getString(R.string.leave2))
                    .setConfirm(R.string.leave)
                    .setConfirmListener(new Dialog.OnClickListener()
                    {
                        @Override
                        public void onClick(Dialog dialog)
                        {
                            DetailActivity.super.onBackPressed();
                        }
                    })
                    .show();
        }
        else if (!isDestroyed())
        {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        bus.unregister(this);
    }

    void setViewEditable(EditText view, boolean editable)
    {
        view.setFocusable(editable);
        view.setFocusableInTouchMode(editable);
        view.setLongClickable(editable);
        view.setGravity(editable? Gravity.NO_GRAVITY: Gravity.CENTER_HORIZONTAL);
    }

    Detail commit()
    {
        Detail detail = selectedDetail.clone();
        detail.estm = _estm.getText().toString();
        detail.acc = _ac.getText().toString();
        detail.macc = _mac.getText().toString();
        detail.cpn = _cpn.getText().toString();
        detail.rpc = _rp.getText().toString();
        detail.description = _description.getText().toString();
        detail.s1 = _s1.length() == 0? 0: Integer.parseInt(_s1.getText().toString());
        detail.s2 = _s2.length() == 0? 0: Integer.parseInt(_s2.getText().toString());
        detail.qty = _qty.length() == 0? 0: Integer.parseInt(_qty.getText().toString());
        detail.loca = _loca.getText().toString();
        detail.dama = _dmcode.getText().toString();
        detail.hrs = _hr.length() == 0? 0: Double.parseDouble(_hr.getText().toString());
        detail.mat = _mat.length() == 0? 0: Double.parseDouble(_mat.getText().toString());
        detail.tpc = _tpc.getText().toString();
        return detail;
    }

    void showDetail(Detail detail)
    {
        selectedDetail = detail;
        _estm.setText(detail.estm);
        _ac.setText(detail.acc);
        _mac.setText(detail.macc);
        _cpn.setText(detail.cpn);
        _rp.setText(detail.rpc);
        _description.setText(detail.description);
        _s1.setText(detail.s1 == 0? null: String.valueOf(detail.s1));
        _s2.setText(detail.s2 == 0? null: String.valueOf(detail.s2));
        _qty.setText(detail.qty == 0? null: String.valueOf(detail.qty));
        _loca.setText(detail.loca);
        _dmcode.setText(detail.dama);
        _hr.setText(detail.hrs == 0? null: Common.get().toString(detail.hrs));
        _mat.setText(detail.mat == 0? null: Common.get().toString(detail.mat));
        _tpc.setText(detail.tpc);
        _delete.setVisibility(context.isCompleted() || detail.isNew()? View.GONE: View.VISIBLE);
        _save.setVisibility(context.isCompleted()? View.GONE: View.VISIBLE);
        _save.setText(detail.isNew()? R.string.create: R.string.save);
    }

    void updateImageMenuItem()
    {
        if (context.isCompleted())
        {
            _photos.setVisible(false);
        }
        else if (Common.get().isExternalStorageAvailable())
        {
            Folder folder = context.getFolder(this);
            File[] files = folder.listFiles();
            _photos.setIcon(files == null || files.length == 0 ? R.drawable.ic_add_a_photo_white_24dp : R.drawable.ic_add_a_photo_light_24dp);
            /*if (files == null || files.length == 0)
            {
                _photos.setIcon(R.drawable.ic_add_a_photo_white_24dp);
            }
            else
            {
                ImageRequest request = ImageRequest.fromFile(files[0]);
                DataSource<CloseableReference<CloseableImage>> dataSource = Fresco.getImagePipeline().fetchDecodedImage(request, this);
                Executor executor = UiThreadImmediateExecutorService.getInstance();
                dataSource.subscribe(subscriber, executor);
            }*/
        }
        else
        {
            Toast.makeText(this, R.string.no_external, Toast.LENGTH_SHORT).show();
            vibrator.vibrate(Common.PATTERN, -1);
        }
    }

    /****************************************/
    // TODO UI events
    /****************************************/

    @OptionsItem(R.id.photos)
    void addPhoto()
    {
        PhotoActivity_.intent(this)
                .header(context)
                .startForResult(REQUEST);
    }

    @Click(R.id.mac)
    void setMacc()
    {
        String[] array = selectedDetail.isNew()? context.macc_list: selectedDetail.mas_macc();
        if (Common.get().isEmpty(array)) array = context.macc_list;
        new Dialog2(this, Dialog2.LIST_TYPE)
                .setTitle(context.cont)
                .setSelectedItem(_mac.getText().toString())
                .setItems(array, new Dialog2.OnClickListener()
                {
                    @Override
                    public void onClick(Dialog2 dialog)
                    {
                        _mac.setText(dialog.getSelectedItem().toString());
                        _cpn.requestFocus();
                    }
                })
                .show();
    }

    @Click(R.id.ac)
    void setAcc()
    {
        String[] array = selectedDetail.isNew()? context.acc_list: selectedDetail.mas_acc();
        if (Common.get().isEmpty(array)) array = context.acc_list;
        new Dialog2(this, Dialog2.LIST_TYPE)
                .setTitle(context.cont)
                .setSelectedItem(_ac.getText().toString())
                .setItems(array, new Dialog2.OnClickListener()
                {
                    @Override
                    public void onClick(Dialog2 dialog)
                    {
                        _ac.setText(dialog.getSelectedItem().toString());
                        _cpn.requestFocus();
                    }
                })
                .show();
    }

    @FocusChange({ R.id.cpn, R.id.rp })
    void triggerRefresh(View v, boolean hasFocus)
    {
        if (!hasFocus) refresh();
    }

    @Click(R.id.refresh)
    void refresh()
    {
        String cpn = _cpn.getText().toString();
        String rp = _rp.getText().toString();
        if (cpn.length() > 0 || rp.length() > 0)
        {
            _refresh.setRefreshing(true);
            _description.setEnabled(false);
            refreshDesc(cpn, rp);
        }
        else if (cpn.length() + rp.length() == 0)
        {
            _description.setText(null);
        }
    }

    @FocusChange(R.id.loca)
    void triggerRefresh2(View v, boolean hasFocus)
    {
        if (!hasFocus) refresh2();
    }

    @Click(R.id.refresh2)
    void refresh2()
    {
        String cpn = _cpn.getText().toString();
        String rp = _rp.getText().toString();
        String s1 = _s1.getText().toString();
        String s2 = _s2.getText().toString();
        String qty = _qty.getText().toString();
        String loca = _loca.getText().toString();
        String dmcode = _dmcode.getText().toString();
        String macc = _mac.getText().toString();
        String estm = _estm.getText().toString();
        _refresh2.setRefreshing(true);
        _hr.setEnabled(false);
        _mat.setEnabled(false);
        refreshCalc(cpn, rp, s1, s2, qty, loca, dmcode, macc, estm);
    }

    @Click(R.id.delete)
    void delete()
    {
        new Dialog(this, Dialog.PROMPT_TYPE)
                .setTitle(context.cont)
                .setContentRes(R.string.confirm, getString(R.string.delete).toLowerCase(Locale.ENGLISH))
                .setConfirm(R.string.delete)
                .setConfirmListener(new Dialog.OnClickListener()
                {
                    @Override
                    public void onClick(Dialog dialog)
                    {
                        dialog.changeType(Dialog.PROGRESS_TYPE)
                                .setCloseOnClick(false)
                                .setCancelOnBack(false)
                                .setContentRes(R.string.LOADING);
                        doDelete(selectedDetail, dialog);
                    }
                })
                .show();
    }

    @Click(R.id.save)
    void save()
    {
        final Detail edited = commit();
        if (edited.equals(selectedDetail)) return;
        Dialog dialog = new Dialog(this, Dialog.PROGRESS_TYPE)
                .setTitle(context.cont)
                .setContentRes(R.string.LOADING)
                .setCloseOnClick(false)
                .setCancelOnBack(false);
        dialog.show();
        doSubmit(edited, dialog);
    }

    @Click(R.id.complete)
    void complete()
    {
        new Dialog(this, Dialog.PROMPT_TYPE)
                .setTitle(context.cont)
                .setContentRes(R.string.confirm, getString(R.string.complete).toLowerCase(Locale.ENGLISH))
                .setConfirm(R.string.complete)
                .setConfirmListener(new Dialog.OnClickListener()
                {
                    @Override
                    public void onClick(Dialog dialog)
                    {
                        dialog.changeType(Dialog.PROGRESS_TYPE)
                                .setCloseOnClick(false)
                                .setCancelOnBack(false)
                                .setContentRes(R.string.LOADING);
                        doComplete(commit(), dialog);
                    }
                })
                .show();
    }

    /****************************************/
    // TODO POST getDesc
    /****************************************/

    @Background
    void refreshDesc(String cpn, String rp)
    {
        String result = null;
        IOException error = null;
        try
        {
            result = api.getDesc(cpn, rp).execute().body();
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
        if (!isDestroyed()) afterRefreshDesc(error, result);
    }

    @UiThread
    void afterRefreshDesc(IOException error, String result)
    {
        if (error != null)
        {
            String msg = error.getMessage();
            if (msg == null) msg = error.toString();
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            if (error instanceof InternalServerError) util.report("Detail_refreshDesc", error);
        }
        else if (result != null)
        {
            _description.setText(result);
        }
        _refresh.setRefreshing(false);
        _description.setEnabled(true);
    }

    /****************************************/
    // TODO POST getCharge(_CTN)
    /****************************************/

    @Background
    void refreshCalc(String cpn, String rp, String s1, String s2, String qty, String loca, String dmcode, String macc, String estm)
    {
        double[] result = null;
        IOException error = null;
        try
        {
            result = context instanceof Container?
                    api.getCharge(container, cpn, rp, s1, s2, qty, loca, dmcode, macc, estm).execute().body():
                    api.getCharge(context, cpn, rp, s1, s2, qty, loca, dmcode, macc, estm).execute().body();
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
        if (!isDestroyed()) afterRefreshCalc(error, result);
    }

    @UiThread
    void afterRefreshCalc(IOException error, double[] result)
    {
        if (error != null)
        {
            String msg = error.getMessage();
            if (msg == null) msg = error.toString();
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            if (error instanceof InternalServerError) util.report("Detail_refreshCalc", error);
        }
        else if (result != null)
        {
            _hr.setText(Common.get().toString(result[0]));
            _mat.setText(Common.get().toString(result[1]));
        }
        _refresh2.setRefreshing(false);
        _hr.setEnabled(true);
        _mat.setEnabled(true);
    }

    /****************************************/
    // TODO POST uptSurveyCharge(_CTN)
    /****************************************/

    @Background
    void doSubmit(Detail detail, Dialog dialog)
    {
        Integer result = null;
        IOException error = null;
        try
        {
            result = ((context instanceof Container)?
                        api.updateDetail(container, detail):
                        api.updateDetail(context, detail)
                     ).execute().body();
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
        if (!isDestroyed()) afterSubmit(error, result, detail, dialog);
    }

    @UiThread
    void afterSubmit(IOException error, Integer result, Detail detail, Dialog dialog)
    {
        if (error != null)
        {
            vibrator.vibrate(Common.PATTERN, -1);
            dialog.changeType(Dialog.ERROR_TYPE).setContent(error.getMessage());
            if (error instanceof InternalServerError) util.report("Detail_submit", error);
        }
        else if (result != null)
        {
            dialog.dismissWithAnimation();
            if (detail.isNew())
            {
                detail.sud = result;
                if (detail.qty == 0) detail.qty = 1;
                context.details = detailAdapter.add(detail);
                showDetail(context.newDetail());
                detailAdapter.unselect();
            }
            else
            {
                selectedDetail.merge(detail);
                detailAdapter.refresh(selectedDetail);
            }
            Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show();
            _cpn.requestFocus();
            if (!(context instanceof Container)) refreshSummary();
        }
        else
        {
            dialog.dismissWithAnimation();
        }
    }

    @Background
    void refreshSummary()
    {
        IOException error = null;
        try
        {
            Summary[] summaries = api.summary(header).execute().body();
            if (summaries != null) header.summaries = summaries;
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
        afterRefresh(error);
    }

    @UiThread
    void afterRefresh(IOException error)
    {
        if (error != null)
        {
            Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
        }
        else
        {
            summaryAdapter.apply(header.summaries);
        }
    }

    /****************************************/
    // TODO POST delSurveyCharge
    /****************************************/

    @Background
    void doDelete(Detail detail, Dialog dialog)
    {
        Boolean result = null;
        IOException error = null;
        try
        {
            result = api.deleteDetail(detail).execute().body();
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
        if (!isDestroyed()) afterDelete(error, result, detail, dialog);
    }

    @UiThread
    void afterDelete(IOException error, Boolean result, Detail detail, Dialog dialog)
    {
        if (error != null)
        {
            vibrator.vibrate(Common.PATTERN, -1);
            dialog.changeType(Dialog.ERROR_TYPE).setContent(error.getMessage());
            if (error instanceof InternalServerError) util.report("Detail_delete", error);
        }
        else if (result != null)
        {
            dialog.dismissWithAnimation();
            context.details = detailAdapter.delete(detail);
            if (Common.get().isExternalStorageAvailable())
            {
                Folder folder = detail.getFolder(this);
                if (!folder.delete() && DEBUG) Log.w(TAG, "Fail to delete @" + folder);
            }
            int colorId = R.color.colorPrimary;
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, colorId)));
            showDetail(context.newDetail());
            Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show();
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
    void doComplete(Detail detail, Dialog dialog)
    {
        Boolean result = null;
        IOException error = null;
        try
        {
            if (!detail.equals(selectedDetail))
            {
                Integer sud = api.updateDetail(context, detail).execute().body();
                if (sud == null) error = util.createError();
            }
            if (error == null)
            {
                result = api.complete_container(container).execute().body();
                if (result == null) error = util.createError();
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
        if (!isDestroyed()) afterComplete(error, result, dialog);
    }

    @UiThread
    void afterComplete(IOException error, Boolean result, Dialog dialog)
    {
        if (error != null)
        {
            vibrator.vibrate(Common.PATTERN, -1);
            dialog.changeType(Dialog.ERROR_TYPE).setContent(error.getMessage());
            if (error instanceof InternalServerError) util.report("Detail_complete", error);
        }
        else if (result != null)
        {
            dialog.dismissWithAnimation();
            Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show();
            Folder folder = context.getFolder(this);
            if (!folder.delete() && DEBUG) Log.w(TAG, "Fail to delete @" + folder);
            super.onBackPressed();
        }
        else
        {
            dialog.dismissWithAnimation();
        }
    }

    /****************************************/
    // TODO Event subscription
    /****************************************/

    @Subscribe
    public void onDetailSelected(SelectDetailEvent event)
    {
        int colorId = R.color.colorPrimaryDark;
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, colorId)));
        showDetail(event.getDetail());
    }
}
