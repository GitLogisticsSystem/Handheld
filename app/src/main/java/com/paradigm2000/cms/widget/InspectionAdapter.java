package com.paradigm2000.cms.widget;

import android.content.Context;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.paradigm2000.cms.gson.Header;
import com.paradigm2000.core.widget.RecyclerViewAdapterBase;
import com.paradigm2000.core.widget.ViewWrapper;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.Collections;

@EBean
public class InspectionAdapter extends RecyclerViewAdapterBase<Header, InspectionView>
{
    Gson gson = new Gson();

    @RootContext
    Context context;

    boolean mEnabled = true;

    public void apply(Header[] headers)
    {
        notifyItemRangeRemoved(0, getItemCount());
        items.clear();
        Collections.addAll(items, headers);
        notifyItemRangeInserted(0, getItemCount());
    }

    public void setEnabled(boolean flag)
    {
        mEnabled = flag;
        notifyItemRangeChanged(0, getItemCount());
    }

    @Override
    protected InspectionView onCreateItemView(ViewGroup parent, int viewType)
    {
        return InspectionView_.build(context);
    }

    @Override
    public void onBindViewHolder(ViewWrapper<InspectionView> holder, InspectionView view, int position, Header item)
    {
        view.bind(item);
        view.setEnabled(mEnabled);
    }
}
