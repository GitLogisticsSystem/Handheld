package com.paradigm2000.cms.gson;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

@Parcel
public class ContainerOuts
{
    ContainerOut[] containerouts;

    @ParcelConstructor
    public ContainerOuts(ContainerOut[] containerouts)
    {
        this.containerouts = containerouts;
    }

    public ContainerOut[] get()
    {
        return containerouts;
    }
}
