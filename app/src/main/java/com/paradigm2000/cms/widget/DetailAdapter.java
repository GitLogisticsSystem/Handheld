package com.paradigm2000.cms.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.paradigm2000.cms.PhotoActivity_;
import com.paradigm2000.cms.R;
import com.paradigm2000.cms.event.SelectDetailEvent;
import com.paradigm2000.cms.gson.Detail;
import com.paradigm2000.cms.gson.Header;
import com.paradigm2000.core.bean.EventBus;
import com.paradigm2000.core.widget.RecyclerViewAdapterBase;
import com.paradigm2000.core.widget.ViewWrapper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

@EBean
public class DetailAdapter extends RecyclerViewAdapterBase<Detail, DetailView>
{
    @RootContext
    Context context;
    @Bean
    EventBus bus;

    Header header;
    int request;
    Detail lastSelected;
    boolean enabled = true, completed;

    public void setHeader(Header header)
    {
        this.header = header;
    }

    public void setRequest(int request)
    {
        this.request = request;
    }

    public void setCompleted(boolean flag)
    {
        completed = flag;
        notifyItemRangeChanged(0, getItemCount());
    }

    public void setEnabled(boolean flag)
    {
        enabled = flag;
        notifyItemRangeChanged(0, getItemCount());
    }

    public void apply(Detail[] details)
    {
        if (getItemCount() > 0)
        {
            notifyItemRangeRemoved(0, getItemCount());
            items.clear();
        }
        for (Detail detail: details) add(detail);
    }

    public Detail[] add(Detail detail)
    {
        items.add(0, detail);
        notifyItemInserted(0);
        notifyItemRangeChanged(1, items.size() - 1);
        return items.toArray(new Detail[getItemCount()]);
    }

    public void refresh(Detail detail)
    {
        int index = items.indexOf(detail);
        if (index > -1) notifyItemChanged(index);
    }

    public void select(Detail item)
    {
        if (lastSelected != null)
        {
            lastSelected.setSelected(false);
            notifyItemChanged(items.indexOf(lastSelected));
        }
        bus.post(new SelectDetailEvent(lastSelected = item));
        item.setSelected(true);
        notifyItemChanged(items.indexOf(item));
    }

    public boolean unselect()
    {
        if (lastSelected == null) return false;
        lastSelected.setSelected(false);
        notifyItemChanged(items.indexOf(lastSelected));
        lastSelected = null;
        return true;
    }

    public Detail[] delete(Detail detail)
    {
        int index = items.indexOf(detail);
        if (index > -1)
        {
            items.remove(index);
            notifyItemRemoved(index);
            notifyItemRangeChanged(0, getItemCount() - index);
        }
        if (lastSelected == detail) lastSelected = null;
        return items.toArray(new Detail[getItemCount()]);
    }

    @Override
    protected DetailView onCreateItemView(ViewGroup parent, int viewType)
    {
        return DetailView_.build(context);
    }

    @Override
    public void onBindViewHolder(ViewWrapper<DetailView> holder, DetailView view, int position, Detail item)
    {
        item.position = items.size() - 1 - position;
        view.bind(item, enabled, completed);
    }

    @Override
    protected boolean onItemClick(DetailView container, View view, int position, Detail item)
    {
        if (!enabled) return false;
        if (view.getId() == R.id.photo)
        {
            PhotoActivity_.intent(context)
                    .header(header)
                    .detail(item)
                    .startForResult(request);
        }
        else
        {
            select(item);
        }
        return true;
    }
}
