package com.paradigm2000.cms.gson;

import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel
public class Enquiry
{
    public int est_ref;
    public String est_no;
    public String est_date;
    public String est_appr;
    public float est_tot;
    public int est_prt;
    public String est_CanBeApp;
    public int etd;
    public ArrayList<Detail> details = new ArrayList<>();
}
