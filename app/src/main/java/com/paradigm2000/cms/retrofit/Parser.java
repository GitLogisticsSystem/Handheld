package com.paradigm2000.cms.retrofit;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.paradigm2000.cms.R;
import com.paradigm2000.cms.gson.Booking;
import com.paradigm2000.cms.gson.Detail;
import com.paradigm2000.cms.gson.Enquiry;
import com.paradigm2000.cms.gson.Header;
import com.paradigm2000.cms.gson.User;
import com.paradigm2000.core.Common;
import com.paradigm2000.core.retrofit.APIException;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

@SuppressWarnings("unused")
public class Parser
{
    public static User login(Context context, Gson gson, String response) throws Exception
    {
        User[] users = gson.fromJson(response, User[].class);
        if (users.length == 0) throw new APIException(context.getString(R.string.login_fail));
        return users[0];
    }

    public static String[] getList(Context context, Gson gson, String response) throws Exception
    {
        response = new JSONArray(response).getJSONObject(0).getString("Column1");
        return TextUtils.isEmpty(response)? Common.EMPTY_STR_ARRAY: response.split("\\|");
    }

    public static String getDesc(Context context, Gson gson, String response) throws Exception
    {
        JSONArray array = new JSONArray(response);
        if (array.length() == 0) throw new APIException(context.getString(R.string.error));
        JSONObject object = array.getJSONObject(0);
        response = object.getString("edesc");
        return "null".equals(response.trim())? "": response;
    }

    public static double[] getCharge(Context context, Gson gson, String response) throws Exception
    {
        JSONArray array = new JSONArray(response);
        if (array.length() == 0) throw new APIException(context.getString(R.string.error));
        JSONObject object = array.getJSONObject(0);
        return new double[] { object.getDouble("Column1"), object.getDouble("Column2"), object.getDouble("Column3") };
    }

    public static String[] listStd(Context context, Gson gson, String response) throws Exception
    {
        JSONArray array = new JSONArray(response);
        String[] result = new String[array.length()];
        for (int i = 0, length = result.length; i < length; i += 1)
        {
            JSONObject object = array.getJSONObject(i);
            result[i] = object.getString("std_list");
        }
        return result;
    }

    public static String[] findSizeType(Context context, Gson gson, String response) throws Exception
    {
        JSONArray array = new JSONArray(response);
        if (array.length() == 0) throw new APIException(context.getString(R.string.error));
        JSONObject object = array.getJSONObject(0);
        return new String[] { object.getString("f_size"), object.getString("f_type") };
    }

    public static String chkOper(Context context, Gson gson, String response) throws Exception
    {
        JSONArray array = new JSONArray(response);
        if (array.length() == 0) throw new APIException(context.getString(R.string.invalid_oper));
        JSONObject object = array.getJSONObject(0);
        return object.getString("oper");
    }

    public static Header chkCont(Context context, Gson gson, String response) throws Exception
    {
        JSONArray array = new JSONArray(response);
        if (array.length() == 0) throw new APIException(context.getString(R.string.error));
        JSONObject object = array.getJSONObject(0);
        String message = object.getString("message");
        if ("Success".equals(message)) return gson.fromJson(response, Header[].class)[0];
        throw new APIException(message);
    }

    public static Booking queryBooking(Context context, Gson gson, String response) {
        Booking[] result = gson.fromJson(response, Booking[].class);
        if (result.length == 0) return null;
        Booking booking = result[0];
        return TextUtils.isEmpty(booking.inno) && TextUtils.isEmpty(booking.hdte) && TextUtils.isEmpty(booking.oper) && TextUtils.isEmpty(booking.dpp) && TextUtils.isEmpty(booking.inle) && TextUtils.isEmpty(booking.remark) ? null : booking;
    }

    public static Integer addSUH(Context context, Gson gson, String response) throws Exception
    {
        response = response.trim();
        try
        {
            return Integer.parseInt(response);
        }
        catch (NumberFormatException e)
        {
            throw new APIException(response);
        }
    }

    public static Integer updateDetail(Context context, Gson gson, String response) throws Exception
    {
        response = response.trim().toLowerCase(Locale.ENGLISH);
        try
        {
            return "success".equals(response)? -1: Integer.parseInt(response);
        }
        catch (NumberFormatException e)
        {
            throw new APIException(response);
        }
    }

    public static Boolean upload(Context context, Gson gson, String response) throws Exception
    {
        if (response.endsWith("success")) return true;
        throw new APIException(response.contains("<BR>") && !response.endsWith("<BR>")? response.split("<BR>")[1]: response);
    }

    public static Enquiry[] enquiries(Context context, Gson gson, String response) throws Exception
    {
        JSONArray array = new JSONArray(response);
        HashMap<Integer, Enquiry> details = new HashMap<>();
        for (int i = 0, length = array.length(); i < length; i += 1)
        {
            String json = array.getString(i);
            JSONObject object = array.getJSONObject(i);
            int key = object.getInt("est_ref");
            if (!details.containsKey(key)) details.put(key, gson.fromJson(json, Enquiry.class));
            Enquiry detail = details.get(key);
            detail.details.add(gson.fromJson(json, Detail.class));
        }
        Collection<Enquiry> result = details.values();
        return result.toArray(new Enquiry[result.size()]);
    }

    public static String[] list_subcon(Context context, Gson gson, String response) throws Exception
    {
        JSONArray array = new JSONArray(response);
        String[] subcons = new String[array.length()];
        for (int i = 0, length = subcons.length; i < length; i += 1)
        {
            subcons[i] = array.getJSONObject(i).getString("subc");
        }
        return subcons;
    }

    public static String[] list_oper(Context context, Gson gson, String response) throws Exception
    {
        JSONArray array = new JSONArray(response);
        String[] opers = new String[array.length()];
        for (int i = 0, length = opers.length; i < length; i += 1)
        {
            opers[i] = array.getJSONObject(i).getString("oper");
        }
        return opers;
    }
}
