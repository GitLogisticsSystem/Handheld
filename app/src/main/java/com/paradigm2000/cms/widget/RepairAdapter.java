package com.paradigm2000.cms.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.paradigm2000.cms.R;
import com.paradigm2000.cms.gson.Repair;
import com.paradigm2000.core.widget.RecyclerViewAdapterBase;
import com.paradigm2000.core.widget.ViewWrapper;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.Collections;

@EBean
public class RepairAdapter extends RecyclerViewAdapterBase<Repair, RepairView> implements RecyclerViewAdapterBase.OnItemClickListener
{
    @RootContext
    Context context;

    String lastInput;
    int selected = -1;
    boolean mEnabled = true;
    RepairListener listener;

    RepairAdapter()
    {
        super.setOnItemClickListener(this);
    }

    public void setListener(RepairListener listener)
    {
        this.listener = listener;
    }

    public String lastInput()
    {
        return lastInput;
    }

    public void apply(String input, Repair[] repair)
    {
        lastInput = input;
        selected = -1;
        notifyItemRangeRemoved(0, getItemCount());
        items.clear();
        if (repair.length == 1) repair[selected = 0].selected = true;
        Collections.addAll(items, repair);
        notifyItemRangeInserted(0, getItemCount());
    }

    public boolean deselect()
    {
        if (getItemCount() > 1 && selected > -1)
        {
            selectRepair(selected, false);
            selected = -1;
            return true;
        }
        return false;
    }

    public void refresh()
    {
        notifyItemChanged(selected);
    }

    public void setEnabled(boolean flag)
    {
        mEnabled = flag;
        notifyItemRangeChanged(0, getItemCount());
    }

    @Override
    protected RepairView onCreateItemView(ViewGroup parent, int viewType)
    {
        return RepairView_.build(context);
    }

    @Override
    public void onBindViewHolder(ViewWrapper<RepairView> holder, RepairView view, int position, Repair item)
    {
        view.bind(item);
        view.setEnabled(mEnabled);
    }

    protected boolean onItemClick(RepairView container, View view, int position, Repair item)
    {
        switch (view.getId())
        {
            case R.id.photos:
            {
                if (listener != null) listener.onPhotos(this, item);
                return true;
            }
            case R.id.submit:
            {
                if (listener != null) listener.onSubmit(this, item);
                return true;
            }
        }
        return false;
    }

    private void selectRepair(int position, boolean selected)
    {
        Repair repair = items.get(position);
        repair.selected = selected;
        notifyItemChanged(position);
    }

    /****************************************/
    // TODO Implement interfaces
    /****************************************/

    @Override
    public void onItemClick(RecyclerView.Adapter adapter, int position, Object item)
    {
        if (selected == position) return;
        if (selected > -1 && selected < getItemCount()) selectRepair(selected, false);
        selectRepair(selected = position, true);
    }

    /****************************************/
    // TODO Public interfaces
    /****************************************/

    public interface RepairListener
    {
        void onPhotos(RepairAdapter adapter, Repair repair);
        void onSubmit(RepairAdapter adapter, Repair repair);
    }
}
