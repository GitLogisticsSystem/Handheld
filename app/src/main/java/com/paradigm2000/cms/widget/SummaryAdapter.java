package com.paradigm2000.cms.widget;

import android.content.Context;
import android.view.ViewGroup;

import com.paradigm2000.cms.gson.Summary;
import com.paradigm2000.core.widget.RecyclerViewAdapterBase;
import com.paradigm2000.core.widget.ViewWrapper;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

@EBean
public class SummaryAdapter extends RecyclerViewAdapterBase<Summary, SummaryView>
{
    @RootContext
    Context context;

    public void apply(Summary[] summaries)
    {
        if (getItemCount() > 0)
        {
            notifyItemRangeRemoved(0, getItemCount());
            items.clear();
        }
        for (Summary summary: summaries) items.add(summary);
        notifyDataSetChanged();
    }

    @Override
    protected SummaryView onCreateItemView(ViewGroup parent, int viewType)
    {
        return SummaryView_.build(context);
    }

    @Override
    public void onBindViewHolder(ViewWrapper<SummaryView> holder, SummaryView view, int position, Summary item)
    {
        view.bind(item);
    }
}
