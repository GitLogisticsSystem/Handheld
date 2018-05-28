package com.paradigm2000.cms;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.labo.kaji.fragmentanimations.MoveAnimation;
import com.paradigm2000.cms.app.SubconDialog;
import com.paradigm2000.cms.event.PhotosCheckEvent;
import com.paradigm2000.cms.gson.Repair;
import com.paradigm2000.cms.retrofit.ApiCaller;
import com.paradigm2000.cms.widget.RepairAdapter;
import com.paradigm2000.core.Common;
import com.paradigm2000.core.Fragment;
import com.paradigm2000.core.bean.EventBus;
import com.paradigm2000.core.dialog.Dialog2;
import com.paradigm2000.core.io.Folder;
import com.paradigm2000.core.retrofit.RetrofitUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

@EFragment(R.layout.fragment_container) @OptionsMenu(R.menu.menu_repair)
public class RepairFragment extends Fragment implements RepairAdapter.RepairListener
{
    static final boolean DEBUG = BuildConfig.debug;
    static final String TAG = "RepairF_";
    static final int REQUEST = 0x008ed6;

    @OptionsMenuItem(R.id.refresh)
    MenuItem _refresh;
    @ViewById(R.id.input)
    EditText _input;
    @ViewById(R.id.results)
    RecyclerView _results;
    @ViewById(R.id.empty)
    TextView _empty;
    @Bean
    EventBus bus;
    @Bean
    ApiCaller api;
    @Bean
    RetrofitUtil util;
    @Bean
    RepairAdapter repairAdapter;
    @SystemService
    Vibrator vibrator;
    @InstanceState
    String[] subcons;

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim)
    {
        return MoveAnimation.create(MoveAnimation.LEFT, enter, 384);
    }

    @AfterViews
    void afterViews()
    {
        getActivity().setTitle(R.string.repair_complete);
        _input.setHint(repairAdapter.lastInput());
        repairAdapter.setEmptyView(_results, _empty);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        Drawable list_divider = ContextCompat.getDrawable(getContext(), R.drawable.list_divider_h);
        SimpleListDividerDecorator decorator = new SimpleListDividerDecorator(list_divider, true);
        _results.setLayoutManager(manager);
        _results.addItemDecoration(decorator);
        _results.setAdapter(repairAdapter);
        repairAdapter.setListener(this);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.refresh).setVisible(repairAdapter.getItemCount() > 0);
    }

    @OnActivityResult(REQUEST)
    void onPhotoReturn(int resultCode)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            if (DEBUG) Log.i(TAG, "onPhotoReturn: repairAdapter.refresh()");
            repairAdapter.refresh();
        }
        else if (!Common.get().isExternalStorageAvailable())
        {
            vibrator.vibrate(Common.PATTERN, -1);
            Toast.makeText(getContext(), R.string.no_external, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onBackPressed()
    {
        return repairAdapter.deselect();
    }

    @Override
    public boolean isDestroyed()
    {
        return super.isDestroyed() && _input == null;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        bus.post(new PhotosCheckEvent(getContext()));
    }

    /****************************************/
    // TODO UI events
    /****************************************/

    @OptionsItem(R.id.refresh)
    void refresh()
    {
        CharSequence input = _input.getHint();
        if (!TextUtils.isEmpty(input)) list(input.toString());
    }

    @EditorAction(R.id.input) @TextChange(R.id.input)
    void submit()
    {
        if (_input.length() == 4) list(_input.getText().toString());
    }

    /****************************************/
    // TODO Implement interfaces
    /****************************************/

    @Override
    public void onPhotos(RepairAdapter adapter, Repair repair)
    {
        PhotoActivity_.intent(this)
                .repair(repair)
                .startForResult(REQUEST);
    }

    @Override
    public void onSubmit(RepairAdapter adapter, final Repair repair)
    {
        if (subcons.length == 0)
        {
            Dialog2 dialog = new Dialog2(getContext(), Dialog2.PROGRESS_TYPE)
                    .setTitleRes(R.string.app_name)
                    .setContentRes(R.string.LOADING)
                    .setCancelOnBack(false);
            doComplete(repair, "", dialog);
            dialog.show();
        }
        else
        {
            new SubconDialog(getContext(), subcons)
                    .setListener(new SubconDialog.OnSelectListener() {
                        @Override
                        public void onSelect(Dialog2 dialog, String subcon) {
                            dialog.changeType(Dialog2.PROGRESS_TYPE)
                                    .setContentRes(R.string.LOADING)
                                    .setCancelOnBack(false);
                            checkSubcon(repair, subcon, dialog);
                        }
                    })
                    .show();
            /*new Dialog2(getContext(), Dialog2.LIST_TYPE)
                    .setTitleRes(R.string.app_name)
                    .setContentRes(R.string.subc)
                    .showConfirm(true)
                    .setItems(subcons, new Dialog2.OnClickListener()
                    {
                        @Override
                        public void onClick(Dialog2 dialog)
                        {
                            dialog.changeType(Dialog2.PROGRESS_TYPE)
                                    .setContentRes(R.string.LOADING)
                                    .setCancelOnBack(false);
                            doComplete(repair, dialog.getSelectedItem().toString(), dialog);
                        }
                    })
                    .show();*/
        }
    }

    @Background
    void checkSubcon(Repair repair, String subcon, Dialog2 dialog)
    {
        for (String s : subcons)
        {
            if (s.equalsIgnoreCase(subcon))
            {
                afterCheckSubcon(repair, subcon, dialog);
                return;
            }
        }
        afterCheckSubcon(repair, null, dialog);
    }

    @UiThread
    void afterCheckSubcon(Repair repair, String subcon, Dialog2 dialog)
    {
        if (subcon != null)
        {
            doComplete(repair, subcon, dialog);
        }
        else
        {
            dialog.dismiss();
            onSubmit(repairAdapter, repair);
            Toast.makeText(getContext(), R.string.subcon404, Toast.LENGTH_SHORT).show();
            vibrator.vibrate(Common.PATTERN, -1);
        }
    }

    /****************************************/
    // TODO POST listAEECTN
    /****************************************/

    private void list(String input)
    {
        _refresh.setActionView(R.layout.menuitem_loading);
        repairAdapter.setEnabled(false);
        _refresh.setVisible(true);
        _input.setEnabled(false);
        doList(input);
    }

    @Background
    void doList(String input)
    {
        IOException error = null;
        Repair[] result = null;
        try
        {
            subcons = api.listSubcon().execute().body();
            result = api.list_repair(input).execute().body();
            if (result == null) error = util.createError();
        }
        catch (UnknownHostException e)
        {
            if (!isDestroyed()) util.noNetwork(getContext());
        }
        catch (SocketTimeoutException e)
        {
            if (!isDestroyed()) util.timeout(getContext());
        }
        catch (IOException e)
        {
            error = e;
        }
        if (!isDestroyed()) afterList(error, result, input);
    }

    @UiThread
    void afterList(IOException error, Repair[] result, String input)
    {
        if (error != null)
        {
            util.error(getContext(), R.string.container_enquiry, "Repair_List", error);
        }
        else if (result != null)
        {
            _input.setHint(input);
            _input.setText(null);
            repairAdapter.apply(input, result);
        }
        _refresh.setVisible(!TextUtils.isEmpty(_input.getHint()));
        repairAdapter.setEnabled(true);
        _refresh.setActionView(null);
        _input.setEnabled(true);
    }

    /****************************************/
    // TODO upREPPhoto.ashx
    /****************************************/

    @Background
    void doComplete(Repair repair, String subc, Dialog2 dialog)
    {
        Boolean result = null;
        IOException error = null;
        try
        {
            result = api.complete_repair(repair, subc).execute().body();
            if (result == null) error = util.createError();
        }
        catch (UnknownHostException e)
        {
            util.noNetwork(getContext());
        }
        catch (SocketTimeoutException e)
        {
            util.timeout(getContext());
        }
        catch (IOException e)
        {
            error = e;
        }
        afterComplete(error, repair, result, dialog);
    }

    @UiThread
    void afterComplete(IOException error, Repair repair, Boolean result, Dialog2 dialog)
    {
        if (error != null)
        {
            vibrator.vibrate(Common.PATTERN, -1);
            Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            dialog.dismissWithAnimation();
        }
        else if (result)
        {
            dialog.dismissWithAnimation();
            Toast.makeText(getContext(), R.string.success, Toast.LENGTH_SHORT).show();
            Folder folder = repair.getFolder(getContext());
            if (!folder.delete() && DEBUG) Log.w(TAG, "Fail to delete @" + folder);
            refresh();
        }
        else
        {
            dialog.dismissWithAnimation();
        }
    }
}
