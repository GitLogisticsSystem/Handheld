package com.paradigm2000.cms.retrofit;

import com.google.gson.JsonObject;
import com.paradigm2000.cms.gson.Booking;
import com.paradigm2000.cms.gson.Container;
import com.paradigm2000.cms.gson.ContainerOut;
import com.paradigm2000.cms.gson.Detail;
import com.paradigm2000.cms.gson.Enquiry;
import com.paradigm2000.cms.gson.Header;
import com.paradigm2000.cms.gson.Repair;
import com.paradigm2000.cms.gson.Summary;
import com.paradigm2000.cms.gson.User;
import com.paradigm2000.core.retrofit.RetrieveMethod;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IWebServices
{
    @POST("androidbc.asmx/login")
    @RetrieveMethod(cls=Parser.class, value="login")
    Call<User> login(@Body JsonObject body);

    @POST("androidbc.asmx/listGateIN")
    Call<Header[]> list_inspection(@Body JsonObject body);

    @POST("androidbc.asmx/listGateOUT")
    Call<ContainerOut[]> list_gateout(@Body JsonObject body);


    @POST("androidbc.asmx/listCTN")
    Call<Container[]> list_container(@Body JsonObject body);

    @POST("androidbc.asmx/listARRCTN")
    Call<Repair[]> list_repair(@Body JsonObject body);

    @POST("androidbc.asmx/LinkOldSUD")
    Call<Boolean> link(@Body JsonObject body);

    @POST("androidbc.asmx/getAcc")
    @RetrieveMethod(cls=Parser.class, value="getList")
    Call<String[]> getAcc(@Body JsonObject body);

    @POST("androidbc.asmx/getMacc")
    @RetrieveMethod(cls=Parser.class, value="getList")
    Call<String[]> getMacc(@Body JsonObject body);

    @POST("androidbc.asmx/getAllSurveyCharge")
    Call<Detail[]> details(@Body JsonObject body);

    @POST("androidbc.asmx/getAllSurveySummary")
    Call<Summary[]> summary(@Body JsonObject body);

    @POST("androidbc.asmx/find_sizetype")
    @RetrieveMethod(cls=Parser.class, value="findSizeType")
    Call<String[]> findSizeType(@Body JsonObject body);

    @POST("androidbc.asmx/listSTD")
    @RetrieveMethod(cls=Parser.class, value="listStd")
    Call<String[]> listStd(@Body JsonObject body);

    @POST("androidbc.asmx/getBooking")
    @RetrieveMethod(cls=Parser.class, value="queryBooking")
    Call<Booking> queryBooking(@Body JsonObject body);

    @POST("androidbc.asmx/addSUH")
    @RetrieveMethod(cls=Parser.class, value="addSUH")
    Call<Integer> addHeader(@Body JsonObject body);

    @POST("androidbc.asmx/delSUH")
    Call<Boolean> delHeader(@Body JsonObject body);


    @POST("androidbc.asmx/chkCont")
    @RetrieveMethod(cls=Parser.class, value="chkCont")
    Call<Header> chkCont(@Body JsonObject body);

    @POST("androidbc.asmx/voper")
    @RetrieveMethod(cls=Parser.class, value="chkOper")
    Call<String> chkOper(@Body JsonObject body);

    @POST("androidbc.asmx/uptGatelog")
    Call<Boolean> updateHeader(@Body JsonObject body);

    @POST("androidbc.asmx/uptOut")
    Call<Boolean> updateContainerOut(@Body JsonObject body);

    @POST("androidbc.asmx/uptCTN")
    Call<Boolean> updateSpecific(@Body JsonObject body);

    @POST("androidbc.asmx/getDesc")
    @RetrieveMethod(cls=Parser.class, value="getDesc")
    Call<String> getDesc(@Body JsonObject body);

    @POST("androidbc.asmx/getCharge")
    @RetrieveMethod(cls=Parser.class, value="getCharge")
    Call<double[]> getCharge(@Body JsonObject body);

    @POST("androidbc.asmx/uptSurveyCharge")
    @RetrieveMethod(cls=Parser.class, value="updateDetail")
    Call<Integer> updateDetail(@Body JsonObject body);

    @POST("androidbc.asmx/delSurveyCharge")
    Call<Boolean> deleteDetail(@Body JsonObject body);

    @POST("uploadPhoto.ashx")
    @RetrieveMethod(cls=Parser.class, value="upload")
    Call<Boolean> complete_inspect(@Body MultipartBody body);

    @POST("upREPPhoto.ashx")
    @RetrieveMethod(cls=Parser.class, value="upload")
    Call<Boolean> complete_repair(@Body MultipartBody body);

    @POST("androidbc.asmx/listCTNEst")
    @RetrieveMethod(cls=Parser.class, value="enquiries")
    Call<Enquiry[]> enquiries(@Body JsonObject body);

    @POST("printJob.ashx")
    Call<Boolean> print(@Body MultipartBody body);

    @POST("androidbc.asmx/AppEst")
    Call<Boolean> approve(@Body JsonObject body);

    @POST("androidbc.asmx/getAcc_CTN")
    @RetrieveMethod(cls=Parser.class, value="getList")
    Call<String[]> getAccCTN(@Body JsonObject body);

    @POST("androidbc.asmx/getMacc_CTN")
    @RetrieveMethod(cls=Parser.class, value="getList")
    Call<String[]> getMaccCTN(@Body JsonObject body);

    @POST("androidbc.asmx/getAllSurveyCharge_CTN")
    Call<Detail[]> detailsCTN(@Body JsonObject body);

    @POST("androidbc.asmx/uptSurveyCharge_CTN")
    @RetrieveMethod(cls=Parser.class, value="updateDetail")
    Call<Integer> updateDetailCTN(@Body JsonObject body);

    @POST("androidbc.asmx/getCharge_CTN")
    @RetrieveMethod(cls=Parser.class, value="getCharge")
    Call<double[]> getChargeCTN(@Body JsonObject body);

    @POST("uploadPhoto_CTN.ashx")
    @RetrieveMethod(cls=Parser.class, value="upload")
    Call<Boolean> complete_container(@Body MultipartBody body);

    @POST("androidbc.asmx/listSubc")
    @RetrieveMethod(cls=Parser.class, value="list_subcon")
    Call<String[]> list_subcon(@Body JsonObject body);

    @POST("androidbc.asmx/listOPER")
    @RetrieveMethod(cls=Parser.class, value="list_oper")
    Call<String[]> list_oper(@Body JsonObject body);
}
