package com.paradigm2000.cms;

import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.paradigm2000.cms.gson.ContainerOut;
import com.paradigm2000.cms.retrofit.ApiCaller;
import com.paradigm2000.cms.widget.AutoCompleteView;
import com.paradigm2000.core.Activity;
import com.paradigm2000.core.Common;
import com.paradigm2000.core.dialog.Dialog;

import com.paradigm2000.core.io.Folder;
import com.paradigm2000.core.retrofit.InternalServerError;
import com.paradigm2000.core.retrofit.RetrofitUtil;


import org.androidannotations.annotations.AfterExtras;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EditorAction;
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
import java.text.SimpleDateFormat;

import java.util.Locale;


    @EActivity(R.layout.activity_containerout) @OptionsMenu(R.menu.menu_header)
    public class ContainerOutActivity  extends Activity implements  AdapterView.OnItemClickListener
    {
        static final boolean DEBUG = BuildConfig.debug;
        static final String TAG = "ContainerOut_";
        static final int REQUEST = 0x003da9;

        @OptionsMenuItem(R.id.photos)
        MenuItem _photos;
        @OptionsMenuItem(R.id.refresh)
        MenuItem _refresh;
        @ViewById(R.id.cont)
        EditText _cont;
        @ViewById(R.id.oper)
        AutoCompleteView _oper;
        @ViewById(R.id.grade)
        EditText _grade;
        @ViewById(R.id.size)
        EditText _size;
        @ViewById(R.id.type)
        EditText _type;
        @ViewById(R.id.iso)
        EditText _iso;
        @ViewById(R.id.mdte)
        EditText _mdte;
        @ViewById(R.id.nwgt)
        EditText _nwgt;
        @ViewById(R.id.mgw)
        EditText _mgw;
        @ViewById(R.id.tare)
        EditText _tare;
        @ViewById(R.id.status)
        Button _status;
        @ViewById(R.id.std)
        Button _std;
        @ViewById(R.id.remark)
        EditText _remark;
        @ViewById(R.id.estimate)
        Button _estimate;
        @ViewById(R.id.complete)
        Button _complete;
        @Bean
        ApiCaller api;
        @Bean
        RetrofitUtil util;
        @Extra("Container_Out") @InstanceState
        ContainerOut containerout;
        @DimensionPixelSizeRes(R.dimen.dp32)
        int dp32;
        @SystemService
        InputMethodManager inputManager;
        @SystemService
        Vibrator vibrator;

        Handler handler = new Handler();
        SimpleDateFormat formatter = new SimpleDateFormat("MMyyyy", Locale.getDefault());
        String[] opers = new String[0];

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setDisplayHomeAsUpEnabled(true);
        }

        @AfterExtras
        void afterExtras()
        {

        }

        @AfterViews
        void afterViews()
        {
            Common.get().forceUppercase(_cont);
                _cont.requestFocus();
                _complete.setEnabled(true);

            showHeader();
        }

        @Override
        public boolean onPrepareOptionsMenu(Menu menu)
        {
            _photos.setVisible(true);
            return super.onPrepareOptionsMenu(menu);
        }

        @Override
        protected void onRefresh()
        {
            updateImageMenuItem();
            refresh();
            changeMode();
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

        @Override
        public void onBackPressed()
        {
            if (containerout.equals(commit()))
            {
                super.onBackPressed();
            }
            else
            {
                new Dialog(this, Dialog.PROMPT_TYPE)
                        .setTitle(containerout.cont)
                        .setContentRes(R.string.confirm, getString(R.string.leave2))
                        .setConfirm(R.string.leave)
                        .setConfirmListener(new Dialog.OnClickListener()
                        {
                            @Override
                            public void onClick(Dialog dialog)
                            {
                                ContainerOutActivity.super.onBackPressed();
                            }
                        })
                        .show();
            }
        }

        void changeMode()
        {
            setTitle(containerout.cont);
            ((View) _cont.getParent()).setVisibility( View.GONE);

            _oper.setOnItemClickListener(this);
            setViewEditable(_grade, !containerout.isCompleted());
            setViewEditable(_iso, !containerout.isCompleted());
            setViewEditable(_mdte, !containerout.isCompleted());

            setViewEditable(_mgw, !containerout.isCompleted());
            setViewEditable(_nwgt, !containerout.isCompleted());
            setViewEditable(_tare, !containerout.isCompleted());
            _status.setEnabled(!containerout.isCompleted());
            _std.setEnabled(!containerout.isCompleted());
            setViewEditable(_remark, !containerout.isCompleted());
            _complete.setText(containerout.isCompleted()? R.string.delete: R.string.complete);
        }

        void setViewEditable(EditText view, boolean editable)
        {
            view.setFocusable(editable);
            view.setFocusableInTouchMode(editable);
            view.setLongClickable(editable);
            view.setGravity(editable? Gravity.NO_GRAVITY: Gravity.CENTER_HORIZONTAL);
        }

        void showHeader()
        {
//            _oper.setText(containerout.oper);
//            _grade.setText(containerout.grade);
//            _size.setText(containerout.size);
//            _type.setText(containerout.type);
//            _iso.setText(containerout.iso);
//            _mdte.setText(containerout.mdte);
//            _mgw.setText(containerout.mgw == 0? null: String.valueOf(containerout.mgw));
//
//            _nwgt.setText(containerout.nwgt);
//
//            _tare.setText(containerout.tare == 0? null: String.valueOf(containerout.tare));
//            _status.setText(containerout.stat);
//            _std.setText(containerout.std);
//            _remark.setText(containerout.remark);
        }

        void updateImageMenuItem()
        {
            if (Common.get().isExternalStorageAvailable())
            {
                Folder folder = containerout.getFolder(this);
                File[] files = folder.listFiles();
                if (files == null || files.length == 0)
                {
                    if (containerout.isCompleted())
                    {
                        _photos.setVisible(false);
                    }
                    else
                    {
                        _photos.setVisible(true);
                        _photos.setIcon(R.drawable.ic_add_a_photo_white_24dp);
                    }
                }
                else
                {
                    _photos.setVisible(false);
                    _photos.setIcon(R.drawable.ic_add_a_photo_light_24dp);
                /*ImageRequest request = ImageRequest.fromFile(files[0]);
                DataSource<CloseableReference<CloseableImage>> dataSource = Fresco.getImagePipeline().fetchDecodedImage(request, this);
                Executor executor = UiThreadImmediateExecutorService.getInstance();
                dataSource.subscribe(subscriber, executor);*/
                }
            }
            else
            {
                _photos.setVisible(false);
                Toast.makeText(this, R.string.no_external, Toast.LENGTH_SHORT).show();
                vibrator.vibrate(Common.PATTERN, -1);
            }
        }

        ContainerOut commit()
        {
            ContainerOut edited = containerout.clone();
            edited.cont = _cont.getText().toString();
            edited.oper = _oper.getText().toString();

            edited.grade = _grade.getText().toString();
            edited.iso = _iso.getText().toString();
            edited.mdte = _mdte.getText().toString();
            edited.size = _size.getText().toString();
            edited.type = _type.getText().toString();
            if (_mgw.length() > 0) edited.mgw = Integer.parseInt(_mgw.getText().toString());
            edited.nwgt = _nwgt.getText().toString();
            if (_tare.length() > 0) edited.tare = Integer.parseInt(_tare.getText().toString());
            edited.stat = _status.getText().toString();
            edited.std = _std.getText().toString();
            edited.remark = _remark.getText().toString();
            return edited;
        }

        int chk_dig2(String cont)
        {
            int[] data = new int[26];
            data[0] = 10;
            data[1] = 12;
            data[2] = 13;
            data[3] = 14;
            data[4] = 15;
            data[5] = 16;
            data[6] = 17;
            data[7] = 18;
            data[8] = 19;
            data[9] = 20;
            data[10] = 21;
            data[11] = 23;
            data[12] = 24;
            data[13] = 25;
            data[14] = 26;
            data[15] = 27;
            data[16] = 28;
            data[17] = 29;
            data[18] = 30;
            data[19] = 31;
            data[20] = 32;
            data[21] = 34;
            data[22] = 35;
            data[23] = 36;
            data[24] = 37;
            data[25] = 38;
            int tot = 0;
            int y;
            int x = 1;
            cont = cont.toUpperCase(Locale.ENGLISH);
            for (int i = 0, length = 10; i < length; i += 1)
            {
                y = cont.charAt(i) >= 65? data[cont.charAt(i) - 65]: Integer.parseInt(String.valueOf(cont.charAt(i)));
                tot = y * x + tot;
                x = x * 2;
            }
            y = tot % 11;
            if (y == 10) y = 0;
            return y;
        }

        /****************************************/
        // TODO UI events
        /****************************************/

        @OptionsItem(R.id.photos)
        void addPhoto()
        {
            PhotoActivity_.intent(this)
                    .containerout(containerout)
                    .startForResult(REQUEST);
        }

        @OptionsItem(R.id.refresh)
        void refresh()
        {
            _refresh.setActionView(R.layout.menuitem_loading);
            _estimate.setEnabled(false);
            _complete.setEnabled(false);
        }

        @EditorAction(R.id.tare)
        void submitTare()
        {
            _status.performClick();
        }

        @FocusChange(R.id.cont)
        void checkCont(View view, boolean hasFocus)
        {
            if (!hasFocus)
            {
                boolean error = false;
                String cont = _cont.getText().toString();
                if (cont.length() != 11)
                {
                    Toast.makeText(this, R.string.invalid_cont, Toast.LENGTH_SHORT).show();
                    error = true;
                }
                else
                {
                    int dig = Integer.parseInt(String.valueOf(cont.charAt(10)));
                    int dig2 = chk_dig2(cont);
                    if (dig2 != dig)
                    {
                        Toast.makeText(this, getString(R.string.invalid_chk_dig, dig2), Toast.LENGTH_SHORT).show();
                        error = true;
                    }
                }
                if (error)
                {
                    vibrator.vibrate(Common.PATTERN, -1);
                    handler.post(new Runnable() { public void run() { _cont.requestFocus(); }});
                }
                else
                {

                }
            }
        }


        @Click(R.id.complete)
        void complete()
        {
            new Dialog(this, Dialog.PROMPT_TYPE)
                    .setTitle(containerout.cont)
                    .setContentRes(R.string.confirm, getString(containerout.isCompleted()? R.string.delete: R.string.complete).toLowerCase(Locale.ENGLISH))
                    .setConfirm(containerout.isCompleted()? R.string.delete: R.string.complete)
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
        // TODO Implement interfaces
        /****************************************/

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            _grade.requestFocus();
        }






        /****************************************/
        // TODO POST uptGatelog
        /****************************************/

        @Background
        void doUpdate(ContainerOut containerout, Dialog dialog)
        {
            Boolean result = null;
            IOException error = null;
            try
            {
                result = api.updateContainerOut(containerout).execute().body();
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
            afterUpdate(error, result, containerout, dialog);
        }

        @UiThread
        void afterUpdate(IOException error, Boolean result, ContainerOut header, Dialog dialog)
        {
            if (error != null)
            {
                vibrator.vibrate(Common.PATTERN, -1);
                dialog.changeType(Dialog.ERROR_TYPE).setContent(error.getMessage());
                if (error instanceof InternalServerError) util.report("Header_update", error);
            }
            else if (dialog != null)
            {
//                dialog.dismissWithAnimation();
//                this.containerout = containerout;
//                showHeader();
//                DetailActivity_.intent(this)
//                        .containerout(containerout)
//                        .start();
            }
            else
            {
                _estimate.setEnabled(true);
                _complete.setEnabled(true);
            }
        }

        /****************************************/
        // TODO POST uploadPhoto.ashx
        /****************************************/

        @Background
        void doComplete(ContainerOut edited, Dialog dialog)
        {
            Boolean result = null;
            IOException error = null;
            try
            {

                    result = api.complete_containerout(containerout).execute().body();
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
            afterComplete(error, result, dialog);
        }

        @UiThread
        void afterComplete(IOException error, Boolean result, Dialog dialog)
        {
            if (error != null)
            {
                vibrator.vibrate(Common.PATTERN, -1);
                dialog.changeType(Dialog.ERROR_TYPE).setContent(error.getMessage());
                if (error instanceof InternalServerError) util.report("ContainerOut_complete", error);
            }
            else if (result != null && result)
            {
                dialog.dismissWithAnimation();
                Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show();
                finish();
            }
            else
            {
                dialog.dismissWithAnimation();
            }
        }
    }

