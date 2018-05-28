package com.paradigm2000.cms.event;

import com.paradigm2000.cms.gson.Detail;

public class SelectDetailEvent
{
    Detail detail;

    public SelectDetailEvent(Detail detail)
    {
        this.detail = detail;
    }

    public Detail getDetail()
    {
        return detail;
    }
}
