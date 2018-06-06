package com.paradigm2000.cms.widget;

import android.content.Context;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.paradigm2000.cms.gson.ContainerOut;
import com.paradigm2000.core.widget.RecyclerViewAdapterBase;
import com.paradigm2000.core.widget.ViewWrapper;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.Collections;

@EBean
public class ContainerOutAdapter extends RecyclerViewAdapterBase<ContainerOut, ContainerOutView>
{
    Gson gson = new Gson();

    @RootContext
    Context context;

    boolean mEnabled = true;

    public void apply(ContainerOut[] containerouts)
    {
        notifyItemRangeRemoved(0, getItemCount());
        items.clear();
        Collections.addAll(items, containerouts);
        notifyItemRangeInserted(0, getItemCount());
    }

    public void setEnabled(boolean flag)
    {
        mEnabled = flag;
        notifyItemRangeChanged(0, getItemCount());
    }

    @Override
    protected ContainerOutView onCreateItemView(ViewGroup parent, int viewType)
    {
        return ContainerOutView_.build(context);
    }

    @Override
    public void onBindViewHolder(ViewWrapper<ContainerOutView> holder, ContainerOutView view, int position, ContainerOut item)
    {
        view.bind(item);
        view.setEnabled(mEnabled);
    }
}
