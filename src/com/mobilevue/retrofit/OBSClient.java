package com.mobilevue.retrofit;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

import com.mobilevue.data.ActivePlanDatum;
import com.mobilevue.data.ClientDatum;
import com.mobilevue.data.ClientnConfigDatum;
import com.mobilevue.data.DeviceDatum;
import com.mobilevue.data.EPGData;
import com.mobilevue.data.MediaDetailRes;
import com.mobilevue.data.MediaDetailsResDatum;
import com.mobilevue.data.OrderDatum;
import com.mobilevue.data.Paytermdatum;
import com.mobilevue.data.PlanDatum;
import com.mobilevue.data.ResForgetPwd;
import com.mobilevue.data.ResetPwdDatum;
import com.mobilevue.data.SenderMailId;
import com.mobilevue.data.ServiceDatum;
import com.mobilevue.data.TemplateDatum;

public interface OBSClient {

	//https://41.76.90.173:8181/obsplatform/api/v1
	
	/**
	 * getClientConfigDataSync get method used to get clientData n configData
	 * Synchronously
	 */
	@GET("/mediadevices/client/{clientId}")
	ClientnConfigDatum getClientnConfigDataSync(
			@Path("clientId") String clientId);
	/**
	 * getMediaDevice get method used to get client details based on device id
	 * Async'ly
	 */
	@GET("/mediadevices/{device}")
	void getMediaDevice(@Path("device") String device, Callback<DeviceDatum> cb);

	@GET("/orders/{clientId}/activeplans")
	void getActivePlans(@Path("clientId") String clientId,
			Callback<List<ActivePlanDatum>> cb);

	@GET("/clients/template")
	void getTemplate(Callback<TemplateDatum> cb);

	@GET("/plans?planType=prepaid")
	void getPrepaidPlans(Callback<List<PlanDatum>> cb);

	@GET("/orders/{planid}/template?template=true")
	void getPlanPayterms(@Path("planid") String planid, Callback<List<Paytermdatum>> cb);
	
	@GET("/planservices/{clientId}?serviceType=IPTV")
	ArrayList<ServiceDatum> getPlanServicesSync(
			@Path("clientId") String clientId);

	@GET("/planservices/{clientId}?serviceType=IPTV")
	void getPlanServices(@Path("clientId") String clientId,
			Callback<List<ServiceDatum>> cb);

	@GET("/epgprogramguide/{channelName}/{reqDate}")
	void getEPGDetails(@Path("channelName") String channelName,
			@Path("reqDate") String reqDate, Callback<EPGData> cb);

	@GET("/assets")
	void getPageCountAndMediaDetails(@Query("filterType") String category,
			@Query("pageNo") String pageNo, @Query("deviceId") String deviceId,
			Callback<MediaDetailRes> cb);

	@GET("/assetdetails/{mediaId}")
	void getMediaDetails(@Path("mediaId") String mediaId,
			@Query("eventId") String eventId,
			@Query("deviceId") String deviceId,
			Callback<MediaDetailsResDatum> cb);

	@GET("/clients/{clientId}")
	void getClinetDetails(@Path("clientId") String clientId,
			Callback<ClientDatum> cb);

	@GET("/orders/{clientId}/orders")
	void getClinetPackageDetails(@Path("clientId") String clientId,
			Callback<List<OrderDatum>> cb);

	/**
	 * getMediaDevice put method used to update the device status for the client
	 * Async'ly
	 *//*
	@PUT("/mediadevices/{device}")
	ResourceIdentifier updateAppStatus(@Path("device") String device,
			@Body StatusReqDatum request);
*/
	/**
	 * sendPasswordToMail post method used to initiate the server process of
	 * sending mail to specified MailId Sync'ly. Usage: DoBGTasksService
	 */
	@POST("/selfcare/forgotpassword")
	ResForgetPwd sendPasswordToMail(@Body SenderMailId senderMailId);

	/**
	 * changePassword put method used to reset the password the server process
	 * of sending mail to specified MailId Sync'ly. Usage: DoBGTasksService
	 */
	@PUT("/selfcare/resetpassword")
	ResForgetPwd resetPassword(@Body ResetPwdDatum restPwdData);
}
