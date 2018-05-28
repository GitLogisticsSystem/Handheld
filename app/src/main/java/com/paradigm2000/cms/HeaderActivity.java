package com.paradigm2000.cms;

import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.paradigm2000.cms.gson.Booking;
import com.paradigm2000.cms.gson.Detail;
import com.paradigm2000.cms.gson.Header;
import com.paradigm2000.cms.retrofit.ApiCaller;
import com.paradigm2000.cms.widget.AutoCompleteView;
import com.paradigm2000.core.Activity;
import com.paradigm2000.core.Common;
import com.paradigm2000.core.dialog.Dialog;
import com.paradigm2000.core.dialog.Dialog2;
import com.paradigm2000.core.io.Folder;
import com.paradigm2000.core.retrofit.InternalServerError;
import com.paradigm2000.core.retrofit.RetrofitUtil;
import com.paradigm2000.core.widget.TextView;

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
import java.util.Calendar;
import java.util.Locale;

import static com.paradigm2000.core.widget.TextView.DRAWABLE_END;
import static com.paradigm2000.core.widget.TextView.Position;
import static com.paradigm2000.core.widget.TextView.SHOWN_ON_FOCUS;

@EActivity(R.layout.activity_header) @OptionsMenu(R.menu.menu_header)
public class HeaderActivity extends Activity implements TextView.OnCDrawableListener, AdapterView.OnItemClickListener
{
    static final boolean DEBUG = BuildConfig.debug;
    static final String TAG = "Header_";
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
    @ViewById(R.id.section)
    View _section;
    @ViewById(R.id.bkno)
    TextView _bkno;
    @ViewById(R.id.dpp)
    TextView _dpp;
    @ViewById(R.id.lessee)
    TextView _lessee;
    @ViewById(R.id.ohdate)
    TextView _ohdate;
    @ViewById(R.id.bremark)
    TextView _bremark;
    @Bean
    ApiCaller api;
    @Bean
    RetrofitUtil util;
    @Extra("Header_add") @InstanceState
    boolean isNew;
    @Extra("Header_header") @InstanceState
    Header header;
    @DimensionPixelSizeRes(R.dimen.dp32)
    int dp32;
    @SystemService
    InputMethodManager inputManager;
    @SystemService
    Vibrator vibrator;

    Handler handler = new Handler();
    SimpleDateFormat formatter = new SimpleDateFormat("MMyyyy", Locale.getDefault());
    String[] opers = new String[0];

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
    }

    @AfterExtras
    void afterExtras()
    {
        if (isNew)
        {
            header = new Header();
            header.rtable = "SUH";
        }
    }

    @AfterViews
    void afterViews()
    {
        Common.get().forceUppercase(_cont, _oper, _type, _iso, _grade);
        if (isNew)
        {
            _cont.requestFocus();
            _complete.setEnabled(false);
        }
        com.paradigm2000.core.widget.EditText mdte = (com.paradigm2000.core.widget.EditText) _mdte;
        mdte.setCompoundDrawable(DRAWABLE_END, R.drawable.ic_today_black_24dp, SHOWN_ON_FOCUS);
        mdte.setOnCDrawableListener(this);
        com.paradigm2000.core.widget.EditText iso = (com.paradigm2000.core.widget.EditText) _iso;
        iso.setCompoundDrawable(DRAWABLE_END, R.drawable.ic_refresh_black_24dp, SHOWN_ON_FOCUS);
        iso.setOnCDrawableListener(this);
        _std.setEnabled(header.std_list().length > 0);
        showHeader();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        _photos.setVisible(!isNew);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onRefresh()
    {
        if (!isNew) updateImageMenuItem();
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
        if (isNew)
        {
            new Dialog(this, Dialog.PROMPT_TYPE)
                    .setTitleRes(R.string.new_record)
                    .setContentRes(R.string.confirm, getString(R.string.leave).toLowerCase(Locale.ENGLISH))
                    .setConfirm(R.string.leave)
                    .setConfirmListener(new Dialog.OnClickListener()
                    {
                        @Override
                        public void onClick(Dialog dialog)
                        {
                            HeaderActivity.super.onBackPressed();
                        }
                    })
                    .show();
        }
        else if (header.equals(commit()))
        {
            super.onBackPressed();
        }
        else
        {
            new Dialog(this, Dialog.PROMPT_TYPE)
                    .setTitle(header.cont)
                    .setContentRes(R.string.confirm, getString(R.string.leave2))
                    .setConfirm(R.string.leave)
                    .setConfirmListener(new Dialog.OnClickListener()
                    {
                        @Override
                        public void onClick(Dialog dialog)
                        {
                            HeaderActivity.super.onBackPressed();
                        }
                    })
                    .show();
        }
    }

    void changeMode()
    {
        setTitle(isNew? getString(R.string.new_record): header.cont);
        ((View) _cont.getParent()).setVisibility(isNew? View.VISIBLE: View.GONE);
        setViewEditable(_oper, isNew);
        _oper.setOnItemClickListener(this);
        setViewEditable(_grade, !header.isCompleted());
        setViewEditable(_iso, !header.isCompleted());
        setViewEditable(_mdte, !header.isCompleted());
        setViewEditable(_size, isNew);
        setViewEditable(_type, isNew);
        setViewEditable(_mgw, !header.isCompleted());
        setViewEditable(_nwgt, !header.isCompleted());
        setViewEditable(_tare, !header.isCompleted());
        _status.setEnabled(!header.isCompleted());
        _std.setEnabled(!header.isCompleted());
        setViewEditable(_remark, !header.isCompleted());
        _estimate.setVisibility(isNew || header.isCompleted()? View.GONE: View.VISIBLE);
        _complete.setText(header.isCompleted()? R.string.delete: isNew? R.string.add: R.string.complete);
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
        _oper.setText(header.oper);
        _grade.setText(header.grade);
        _size.setText(header.size);
        _type.setText(header.type);
        _iso.setText(header.iso);
        _mdte.setText(header.mdte);
        _mgw.setText(header.mgw == 0? null: String.valueOf(header.mgw));

        _nwgt.setText(header.nwgt);

        _tare.setText(header.tare == 0? null: String.valueOf(header.tare));
        _status.setText(header.stat);
        _std.setText(header.std);
        _remark.setText(header.remark);
    }

    void updateImageMenuItem()
    {
        if (Common.get().isExternalStorageAvailable())
        {
            Folder folder = header.getFolder(this);
            File[] files = folder.listFiles();
            if (files == null || files.length == 0)
            {
                if (header.isCompleted())
                {
                    _photos.setVisible(false);
                }
                else
                {
                    _photos.setVisible(!isNew);
                    _photos.setIcon(R.drawable.ic_add_a_photo_white_24dp);
                }
            }
            else
            {
                _photos.setVisible(!isNew);
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

    Header commit()
    {
        Header edited = header.clone();
        if (isNew)
        {
            edited.cont = _cont.getText().toString();
            edited.oper = _oper.getText().toString();
        }
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
                .header(header)
                .startForResult(REQUEST);
    }

    @OptionsItem(R.id.refresh)
    void refresh()
    {
        _refresh.setActionView(R.layout.menuitem_loading);
        _estimate.setEnabled(false);
        _complete.setEnabled(false);
        doRefresh();
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
                getBooking(cont);
            }
        }
    }

    @FocusChange(R.id.oper)
    void checkOper(View view, boolean hasFocus)
    {
        if (!hasFocus)
        {
            String oper = _oper.getText().toString();
            String cont = _cont.getText().toString();
            checkOper(oper, cont);
        }
    }

    @FocusChange(R.id.iso)
    void checkISO(View view, boolean hasFocus)
    {
        if (!hasFocus && isNew && (_iso.length() > 0 && (_size.length() == 0 && _type.length() == 0)))
        {
            String iso = _iso.getText().toString();
            checkIso(iso);
        }
    }

    @EditorAction(R.id.mdte)
    void onMdteNext()
    {
        handler.post(new Runnable() { public void run() { (!isNew || (_size.length() > 0 && _type.length() > 0)? _mgw: _size).requestFocus(); }});
    }

    @Click(R.id.status)
    void chooseStatus()
    {
        new Dialog2(this, Dialog2.LIST_TYPE)
                .setTitle(isNew? getString(R.string.new_record): header.cont)
                .setSelectedItem(_status.getText().toString())
                .setItems(header.stat_list(), new Dialog2.OnClickListener()
                {
                    @Override
                    public void onClick(Dialog2 dialog)
                    {
                        String item = dialog.getSelectedItem().toString();
                        _estimate.setVisibility(isNew || "AV".equals(item)? View.GONE: View.VISIBLE);
                        _status.setText(item);
                    }
                })
                .showCancel(true)
                .show();

    }

    @Click(R.id.std)
    void chooseStd()
    {
        new Dialog2(this, Dialog2.LIST_TYPE)
                .setTitle(isNew? getString(R.string.new_record): header.cont)
                .setSelectedItem(_std.getText().toString())
                .setItems(header.std_list(), new Dialog2.OnClickListener()
                {
                    @Override
                    public void onClick(Dialog2 dialog)
                    {
                        _std.setText(dialog.getSelectedItem().toString());
                        _remark.requestFocus();
                    }
                })
                .showCancel(true)
                .show();

    }

    @Click(R.id.estimate)
    void estimate()
    {
        Header edited = commit();
        if (header.equals(edited))
        {
            DetailActivity_.intent(this)
                    .header(header)
                    .start();
        }
        else
        {
            Dialog dialog = new Dialog(this, Dialog.PROGRESS_TYPE)
                    .setTitle(header.cont)
                    .setContentRes(R.string.LOADING)
                    .setCancelOnBack(false);
            dialog.show();
            doUpdate(edited, dialog);
        }
    }

    @Click(R.id.complete)
    void complete()
    {
        new Dialog(this, Dialog.PROMPT_TYPE)
                .setTitle(isNew? getString(R.string.new_record): header.cont)
                .setContentRes(R.string.confirm, getString(header.isCompleted()? R.string.delete: isNew? R.string.add: R.string.complete).toLowerCase(Locale.ENGLISH))
                .setConfirm(header.isCompleted()? R.string.delete: isNew? R.string.add: R.string.complete)
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

    @Override
    public void onClick(android.widget.TextView v, @Position int position)
    {
        if (v == _mdte && position == DRAWABLE_END)
        {
            new Dialog2(this, Dialog2.DATE_TYPE)
                    .setTitle(isNew? getString(R.string.new_record): header.cont)
                    .setCalendar(header.getCalendar(_mdte.getText().toString()))
                    .setConfirmListener(new Dialog2.OnClickListener()
                    {
                        @Override
                        public void onClick(Dialog2 dialog)
                        {
                            Calendar calendar = dialog.getCalendar();
                            _mdte.setText(formatter.format(calendar.getTime()));
                            _mgw.requestFocus();
                        }
                    })
                    .setShowDay(false)
                    .show();
        }
        else if (v == _iso && position == DRAWABLE_END)
        {
            checkIso(_iso.getText().toString());
        }
    }

    /****************************************/
    // TODO POST find_sizetype
    /****************************************/

    @Background
    void checkIso(String iso)
    {
        IOException error = null;
        try
        {
            String[] iresult = api.findSizeType(iso).execute().body();
            if (iresult == null) error = util.createError();
            else
            {
                header.size = iresult[0];
                header.type = iresult[1];
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
        afterCheckIso(error);
    }

    @UiThread
    void afterCheckIso(IOException error)
    {
        if (error != null)
        {
            String msg = error.getMessage();
            if (msg == null) msg = error.toString();
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            if (error instanceof InternalServerError) util.report("Header_checkIso", error);
        }
        else
        {
            _size.setText(header.size);
            _type.setText(header.type);
            if (!isNew)
            {
                _estimate.setEnabled(false);
                _complete.setEnabled(false);
                doUpdate(commit(), null);
            }
        }
    }

    /****************************************/
    // TODO POST getBooking
    /****************************************/

    @Background
    void getBooking(String cont)
    {
        Booking booking = null;
        try
        {
            booking = api.queryBooking(cont).execute().body();
            if (booking == null) return;
        }
        catch (UnknownHostException e)
        {
            if (!isDestroyed()) util.noNetwork(this);
            return;
        }
        catch (SocketTimeoutException e)
        {
            if (!isDestroyed()) util.timeout(this);
            return;
        }
        catch (IOException e)
        {
            Log.w(TAG, "Fail to get booking", e);
        }
        afterGetBooking(cont, booking);
    }

    @UiThread
    void afterGetBooking(String cont, Booking booking)
    {
        _section.setVisibility(booking == null? View.GONE: View.VISIBLE);
        if (booking != null)
        {
            _bkno.setText(booking.inno);
            _oper.setText(booking.oper);
            _dpp.setText(booking.dpp);
            _lessee.setText(booking.inle);
            _ohdate.setText(booking.hdte);
            _bremark.setText(booking.remark);
            if (isNew)
            {
                if (!TextUtils.isEmpty(booking.size)) _size.setText(booking.size);
                if (!TextUtils.isEmpty(booking.type)) _type.setText(booking.type);
            }
            inputManager.hideSoftInputFromWindow(_oper.getWindowToken(), 0);
            if (isNew) checkOper(booking.oper, cont);
        }
    }

    /****************************************/
    // TODO POST voper, ChkCont
    /****************************************/

    @Background
    void checkOper(String oper, String cont)
    {
        IOException error = null;
        try
        {
            if (!TextUtils.isEmpty(oper))
            {
                String sresult = api.checkOperator(oper).execute().body();
                if (sresult == null) error = util.createError();
                else if (!oper.equals(sresult)) error = util.createError(R.string.invalid_oper);
                if (error == null && !TextUtils.isEmpty(cont))
                {
                    Header hresult = api.checkContainer(oper, cont).execute().body();
                    if (hresult == null) error = util.createError();
                    else
                    {
                        header.iso = hresult.iso;
                        header.mdte = hresult.mdte;
                        header.size = hresult.size;
                        header.type = hresult.type;
                        header.mgw = hresult.mgw;
                        header.tare = hresult.tare;
                    }
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
        afterCheckOper(error);
    }

    @UiThread
    void afterCheckOper(IOException error)
    {
        if (error != null)
        {
            String msg = error.getMessage();
            if (msg == null) msg = error.toString();
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            if (error instanceof InternalServerError) util.report("Header_checkOper", error);
            vibrator.vibrate(Common.PATTERN, -1);
        }
        else
        {
            if (!TextUtils.isEmpty(header.iso)) _iso.setText(header.iso);
            if (!TextUtils.isEmpty(header.mdte)) _mdte.setText(header.mdte);
            if (!TextUtils.isEmpty(header.size)) _size.setText(header.size);
            if (!TextUtils.isEmpty(header.type)) _type.setText(header.type);
            _mgw.setText(header.mgw == 0? null: String.valueOf(header.mgw));
            _tare.setText(header.tare == 0? null: String.valueOf(header.tare));
        }
        _complete.setEnabled(error == null);
        _refresh.setActionView(null);
    }

    /****************************************/
    // TODO POST getAcc, getMacc, getAllSurveyCharge, getAllSurveySummary
    /****************************************/

    @Background
    void doRefresh()
    {
        IOException error = null;
        try
        {
            if (isNew)
            {
                String[] stds = api.listStd().execute().body();
                if (stds == null) error = util.createError();
                else Header.def_std_list = stds;
                String[] opers = api.listOper().execute().body();
                if (opers == null) error = util.createError();
                else this.opers = opers;
            }
            else
            {
                if (!header.isCompleted())
                {
                    header.acc_list = api.getAcc(header).execute().body();
                    if (header.acc_list == null) error = util.createError();
                    header.macc_list = api.getMacc(header).execute().body();
                    if (header.macc_list == null) error = util.createError();
                }
                header.details = api.details(header).execute().body();
                if (header.details == null) error = util.createError();
                else
                {
                    for (int l = 0, r = header.details.length - 1; l <= r; l += 1, r -= 1)
                    {
                        if (l < r)
                        {
                            Detail temp = header.details[l];
                            header.details[l] = header.details[r];
                            header.details[r] = temp;
                            header.details[r].cont = header.cont;
                        }
                        header.details[l].cont = header.cont;
                    }
                }
                header.summaries = api.summary(header).execute().body();
                if (header.summaries == null) error = util.createError();
                getBooking(header.cont);
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
        afterRefresh(error);
    }

    @UiThread
    void afterRefresh(IOException error)
    {
        if (error != null)
        {
            if (isNew)
            {
                _complete.setEnabled(false);
                String msg = error.getMessage();
                if (msg == null) msg = error.toString();
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                if (error instanceof InternalServerError) util.report("Header_refresh", error);
            }
            else
            {
                util.error(this, R.string.app_name, "Header_refresh", error);
            }
        }
        else
        {
            if (isNew) _oper.setItems(opers);
            _std.setEnabled(!header.isCompleted() && header.std_list().length > 0);
            _estimate.setEnabled(true);
            _complete.setEnabled(true);
            if (header.isCompleted()) _estimate.setVisibility(header.details.length > 0? View.VISIBLE: View.GONE);
        }
        _refresh.setActionView(null);
    }

    /****************************************/
    // TODO POST uptGatelog
    /****************************************/

    @Background
    void doUpdate(Header header, Dialog dialog)
    {
        Boolean result = null;
        IOException error = null;
        try
        {
            result = api.updateHeader(header).execute().body();
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
        afterUpdate(error, result, header, dialog);
    }

    @UiThread
    void afterUpdate(IOException error, Boolean result, Header header, Dialog dialog)
    {
        if (error != null)
        {
            vibrator.vibrate(Common.PATTERN, -1);
            dialog.changeType(Dialog.ERROR_TYPE).setContent(error.getMessage());
            if (error instanceof InternalServerError) util.report("Header_update", error);
        }
        else if (dialog != null)
        {
            dialog.dismissWithAnimation();
            this.header = header;
            showHeader();
            DetailActivity_.intent(this)
                    .header(header)
                    .start();
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
    void doComplete(Header edited, Dialog dialog)
    {
        Boolean result = null;
        IOException error = null;
        try
        {
            if (header.isCompleted())
            {
                result = api.delHeader(header).execute().body();
                if (result == null) error = util.createError();
            }
            else if (isNew)
            {
                Integer iresult = api.addHeader(edited).execute().body();
                if (iresult == null) error = util.createError();
                else
                {
                    edited.ref = iresult;
                    header = edited;
                }
            }
            else
            {
                if (!header.equals(edited))
                {
                    Boolean iresult = api.updateHeader(edited).execute().body();
                    if (iresult == null) error = util.createError();
                }
                if (error == null)
                {
                    result = api.complete_inspect(header).execute().body();
                    if (result == null) error = util.createError();
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
        afterComplete(error, result, dialog);
    }

    @UiThread
    void afterComplete(IOException error, Boolean result, Dialog dialog)
    {
        if (error != null)
        {
            vibrator.vibrate(Common.PATTERN, -1);
            dialog.changeType(Dialog.ERROR_TYPE).setContent(error.getMessage());
            if (error instanceof InternalServerError) util.report("Header_complete", error);
        }
        else if (header.isCompleted())
        {
            Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show();
            dialog.dismissWithAnimation();
            finish();
        }
        else if (isNew)
        {
            Toast.makeText(this, R.string.success, Toast.LENGTH_SHORT).show();
            dialog.dismissWithAnimation();
            isNew = false;
            onRefresh();
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
