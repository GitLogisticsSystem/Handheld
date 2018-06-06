package com.paradigm2000.cms;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.labo.kaji.fragmentanimations.MoveAnimation;
import com.paradigm2000.cms.event.PhotosCheckEvent;
import com.paradigm2000.cms.gson.ContainerOut;
import com.paradigm2000.cms.gson.Header;
import com.paradigm2000.cms.gson.Headers;
import com.paradigm2000.cms.retrofit.ApiCaller;
import com.paradigm2000.cms.widget.ContainerOutAdapter;
import com.paradigm2000.core.Fragment;
import com.paradigm2000.core.bean.EventBus;
import com.paradigm2000.core.dialog.Dialog;
import com.paradigm2000.core.retrofit.InternalServerError;
import com.paradigm2000.core.retrofit.RetrofitUtil;
import com.paradigm2000.core.widget.RecyclerViewAdapterBase;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

@EFragment(R.layout.fragment_containerout) @OptionsMenu(R.menu.menu_containerout)
public class ContainerOutFragment extends Fragment implements RecyclerViewAdapterBase.OnItemClickListener{
    @OptionsMenuItem(R.id.refresh)
    MenuItem _refresh;
    @ViewById(R.id.list)
    RecyclerView _list;
    @ViewById(R.id.empty)
    TextView _empty;
    @Bean
    EventBus bus;
    @Bean
    ApiCaller api;
    @Bean
    RetrofitUtil util;
    @Bean
    ContainerOutAdapter containerOutAdapter;
    @InstanceState
    boolean firstTime = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        bus.register(this);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim)
    {
        if (firstTime)
        {
            firstTime = false;
            return super.onCreateAnimation(transit, enter, nextAnim);
        }
        else
        {
            return MoveAnimation.create(MoveAnimation.LEFT, enter, 384);
        }
    }

    @AfterViews
    void afterViews()
    {
        getActivity().setTitle(R.string.conatiner_out);
        containerOutAdapter.setEmptyView(_list, _empty);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        Drawable list_divider = ContextCompat.getDrawable(getContext(), R.drawable.list_divider_h);
        SimpleListDividerDecorator decorator = new SimpleListDividerDecorator(list_divider, true);
        _list.setLayoutManager(manager);
        _list.addItemDecoration(decorator);
        _list.setAdapter(containerOutAdapter);
        containerOutAdapter.setOnItemClickListener(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onRefresh()
    {
        refresh();
        bus.post(new PhotosCheckEvent(getContext()));
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        bus.unregister(this);
    }

    /****************************************/
    // TODO UI events
    /****************************************/


    @OptionsItem(R.id.refresh)
    void refresh()
    {
        _refresh.setActionView(R.layout.menuitem_loading);
        containerOutAdapter.setEnabled(false);
        doList();
    }

    /****************************************/
    // TODO Implement interfaces
    /****************************************/

    @Override
    public void onItemClick(RecyclerView.Adapter adapter, int position, Object item)
    {
        final ContainerOut containerout = (ContainerOut) item;

        ContainerOutActivity_.intent(this)
                .containerout(containerout)
                .start();


    }

    /****************************************/
    // TODO POST listGateOUT
    /****************************************/

    @Background
    void doList()
    {
        IOException error = null;
        ContainerOut[] result = null;
        try
        {
            result = api.list_gateout().execute().body();
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
        if (!isDestroyed()) afterList(error, result);
    }

    @UiThread
    void afterList(IOException error, ContainerOut[] result)
    {
        if (error != null)
        {
            util.error(getContext(), R.string.containerout_list, "ContainerOutF_List", error);
        }
        else if (result != null)
        {
            containerOutAdapter.apply(result);
//            InspectionService_.intent(getContext())
//                    .check(new containerout(result))
//                    .start();
        }
        containerOutAdapter.setEnabled(true);
        _refresh.setActionView(null);
    }

    /****************************************/
    // TODO POST LinkOldSUD
    /****************************************/

    @Background
    void doLink(Header header, Dialog dialog)
    {
        IOException error = null;
        Boolean result = null;
        try
        {
            result = api.link(header).execute().body();
            if (result == null) error = util.createError();
            else header.prev_suh = 0;
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
        if (!isDestroyed()) afterLink(error, result, header, dialog);
    }

    @UiThread
    void afterLink(IOException error, Boolean result, Header header, Dialog dialog)
    {
        if (error != null)
        {
            String msg = error.getMessage();
            if (TextUtils.isEmpty(msg)) msg = error.toString();
            dialog.changeType(Dialog.ERROR_TYPE).setContent(msg);
            if (error instanceof InternalServerError) util.report("ContainerOutF_link", error);
        }
        else if (result != null)
        {
            dialog.dismissWithAnimation();
            HeaderActivity_.intent(ContainerOutFragment.this)
                    .header(header)
                    .start();
        }
        else
        {
            dialog.dismissWithAnimation();
        }
    }
}
