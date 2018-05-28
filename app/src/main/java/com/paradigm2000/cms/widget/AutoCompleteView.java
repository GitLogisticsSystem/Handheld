package com.paradigm2000.cms.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;

import com.paradigm2000.core.widget.NoImeAutoCompleteView;

import java.util.ArrayList;
import java.util.Locale;

public class AutoCompleteView extends NoImeAutoCompleteView
{
    String[] items = new String[0];

    public AutoCompleteView(Context context)
    {
        super(context);
    }
    public AutoCompleteView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    public AutoCompleteView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onCreate(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super.onCreate(context, attrs, defStyleAttr);
        setAdapter(new Adapter(context));
        setSoftInputShownOnFocus(true);
        setThreshold(1);
    }

    public void setItems(String[] items)
    {
        this.items = items;
        performFiltering(getText(), 0);
    }

    class Adapter extends ArrayAdapter<String>
    {
        Filter filter;

        Adapter(Context context)
        {
            super(context, android.R.layout.simple_dropdown_item_1line);
            setNotifyOnChange(false);
        }

        @NonNull
        @Override
        public android.widget.Filter getFilter()
        {
            if (filter == null) filter = new Filter(this);
            return filter;
        }
    }

    class Filter extends android.widget.Filter
    {
        Adapter adapter;

        Filter(Adapter adapter)
        {
            this.adapter = adapter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            String input = constraint == null ? "" : constraint.toString();
            FilterResults results = new FilterResults();
            ArrayList<String> filtered = new ArrayList<>();
            for (String item : items) if (item.toLowerCase(Locale.ENGLISH).startsWith(input.toLowerCase(Locale.ENGLISH))) filtered.add(item);
            String[] result = filtered.toArray(new String[0]);
            results.values = result;
            results.count = result.length;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {
            adapter.clear();
            adapter.addAll((String[]) results.values);
            adapter.notifyDataSetChanged();
        }
    }
}
