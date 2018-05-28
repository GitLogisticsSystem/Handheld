package com.paradigm2000.cms.widget;

import android.content.Context;
import android.view.ViewGroup;

import com.paradigm2000.cms.gson.Container;
import com.paradigm2000.core.widget.RecyclerViewAdapterBase;
import com.paradigm2000.core.widget.ViewWrapper;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.Collections;

@EBean
public class ContainerAdapter extends RecyclerViewAdapterBase<Container, ContainerView>
{
    @RootContext
    Context context;

    String lastInput;
    boolean mEnabled = true;

    public String lastInput()
    {
        return lastInput;
    }

    public void apply(String input, Container[] containers)
    {
        lastInput = input;
        notifyItemRangeRemoved(0, getItemCount());
        items.clear();
        Collections.addAll(items, containers);
        notifyItemRangeInserted(0, getItemCount());
    }

    public void setEnabled(boolean flag)
    {
        mEnabled = flag;
        notifyItemRangeChanged(0, getItemCount());
    }

    @Override
    protected ContainerView onCreateItemView(ViewGroup parent, int viewType)
    {
        return ContainerView_.build(context);
    }

    @Override
    public void onBindViewHolder(ViewWrapper<ContainerView> holder, ContainerView view, int position, Container item)
    {
        view.bind(item);
        view.setEnabled(mEnabled);
    }
}
