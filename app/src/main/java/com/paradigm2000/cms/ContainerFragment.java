package com.paradigm2000.cms;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.labo.kaji.fragmentanimations.MoveAnimation;
import com.paradigm2000.cms.event.PhotosCheckEvent;
import com.paradigm2000.cms.gson.Container;
import com.paradigm2000.cms.retrofit.ApiCaller;
import com.paradigm2000.cms.widget.ContainerAdapter;
import com.paradigm2000.core.Fragment;
import com.paradigm2000.core.bean.EventBus;
import com.paradigm2000.core.retrofit.RetrofitUtil;
import com.paradigm2000.core.widget.RecyclerViewAdapterBase;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

@EFragment(R.layout.fragment_container) @OptionsMenu(R.menu.menu_container)
public class ContainerFragment extends Fragment implements RecyclerViewAdapterBase.OnItemClickListener
{
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
    ContainerAdapter containerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        bus.register(this);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim)
    {
        return MoveAnimation.create(MoveAnimation.LEFT, enter, 384);
    }

    @AfterViews
    void afterViews()
    {
        getActivity().setTitle(R.string.container_enquiry);
        _input.setHint(containerAdapter.lastInput());
        containerAdapter.setEmptyView(_results, _empty);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        Drawable list_divider = ContextCompat.getDrawable(getContext(), R.drawable.list_divider_h);
        SimpleListDividerDecorator decorator = new SimpleListDividerDecorator(list_divider, true);
        _results.setLayoutManager(manager);
        _results.addItemDecoration(decorator);
        _results.setAdapter(containerAdapter);
        containerAdapter.setOnItemClickListener(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        bus.post(new PhotosCheckEvent(getContext()));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.refresh).setVisible(containerAdapter.getItemCount() > 0);
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
        bus.unregister(this);
    }

    /****************************************/
    // TODO UI events
    /****************************************/

    @OptionsItem(R.id.refresh)
    void refresh()
    {
        CharSequence input = _input.getHint();
        if (!TextUtils.isEmpty(input)) list(input.toString(), false);
    }

    @EditorAction(R.id.input) @TextChange(R.id.input)
    void submit()
    {
        if (_input.length() == 4) list(_input.getText().toString(), true);
    }

    /****************************************/
    // TODO Implement interfaces
    /****************************************/

    @Override
    public void onItemClick(RecyclerView.Adapter adapter, int position, Object item)
    {
        if (item == null) return;
        EnquiryActivity_.intent(getContext())
                .container((Container) item)
                .start();
    }

    /****************************************/
    // TODO POST listCTN
    /****************************************/

    private void list(String input, boolean isSubmit)
    {
        _refresh.setActionView(R.layout.menuitem_loading);
        _refresh.setVisible(true);
        _input.setEnabled(false);
        doList(input, isSubmit);
    }

    @Background
    void doList(String input, boolean isSubmit)
    {
        IOException error = null;
        Container[] result = null;
        try
        {
            result = api.list_container(input).execute().body();
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
        if (!isDestroyed()) afterList(error, result, input, isSubmit);
    }

    @UiThread
    void afterList(IOException error, Container[] result, String input, boolean isSubmit)
    {
        if (error != null)
        {
            util.error(getContext(), R.string.container_enquiry, "ContainerF_List", error);
        }
        else if (result != null)
        {
            _input.setHint(input);
            _input.setText(null);
            containerAdapter.apply(input, result);
            if (isSubmit && result.length == 1) onItemClick(containerAdapter, 0, result[0]);
        }
        _refresh.setVisible(!TextUtils.isEmpty(_input.getHint()));
        _input.setEnabled(true);
        _refresh.setActionView(null);
    }
}
