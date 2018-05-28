package com.paradigm2000.cms.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.paradigm2000.cms.R;
import com.paradigm2000.cms.gson.Enquiry;
import com.paradigm2000.core.widget.RecyclerViewAdapterBase;
import com.paradigm2000.core.widget.ViewWrapper;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.Collections;

@EBean
public class EnquiryAdapter extends RecyclerViewAdapterBase<Enquiry, EnquiryView>
{
    @RootContext
    Context context;

    boolean enabled = true;
    EnquiryListener listener;

    public void setEnabled(boolean flag)
    {
        enabled = flag;
        notifyItemRangeChanged(0, getItemCount());
    }

    public void setListener(EnquiryListener listener)
    {
        this.listener = listener;
    }

    public void apply(Enquiry[] enquiries)
    {
        if (getItemCount() > 0)
        {
            notifyItemRangeRemoved(0, getItemCount());
            items.clear();
        }
        Collections.addAll(items, enquiries);
        notifyItemRangeInserted(0, getItemCount());
    }

    @Override
    protected EnquiryView onCreateItemView(ViewGroup parent, int viewType)
    {
        return EnquiryView_.build(context);
    }

    @Override
    public void onBindViewHolder(ViewWrapper<EnquiryView> holder, EnquiryView view, int position, Enquiry item)
    {
        view.bind(item);
    }

    @Override
    protected boolean onItemClick(EnquiryView container, View view, int position, Enquiry item)
    {
        switch (view.getId())
        {
            case R.id.approve:
            {
                if (listener != null) listener.approve(item, position);
                return true;
            }
            case R.id.print:
            {
                if (listener != null) listener.print(item, position);
                return true;
            }
        }
        return false;
    }

    /****************************************/
    // TODO Public interfaces
    /****************************************/

    public interface EnquiryListener
    {
        void approve(Enquiry item, int position);
        void print(Enquiry item, int position);
    }
}
