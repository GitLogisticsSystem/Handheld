package com.paradigm2000.cms.gson;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

@Parcel
public class Headers
{
    Header[] headers;

    @ParcelConstructor
    public Headers(Header[] headers)
    {
        this.headers = headers;
    }

    public Header[] get()
    {
        return headers;
    }
}
