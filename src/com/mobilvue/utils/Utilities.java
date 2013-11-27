package com.mobilvue.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.mobilevue.vod.R;
import com.mobilvue.data.ResponseObj;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Utilities {

	private static final String TAG = "Utilities";

	// private static ResponseObj resObj;

	public static ResponseObj callExternalApiGetMethod(Context context,
			HashMap<String, String> param) {
		Log.d(TAG, "callExternalApiGetMethod");
		StringBuilder builder = new StringBuilder();
		ResponseObj resObj = new ResponseObj();
		HttpClient client = MySSLSocketFactory.getNewHttpClient();
		StringBuilder url = new StringBuilder(
				context.getString(R.string.server_url));
		url.append(param.get("TagURL"));
		param.remove("TagURL");
		/*
		 * // adding params to url for (int i = 0; i < param.size(); i++) {
		 * url.append("&" + (String) param.keySet().toArray()[i] + "=" +
		 * (String) param.values().toArray()[i]); } // append device id to url
		 * String androidId = Settings.Secure.getString(
		 * context.getContentResolver(), Settings.Secure.ANDROID_ID);
		 * //url.append("&deviceId="+androidId);
		 *//*
			 * String androidId = Settings.Secure.getString(
			 * context.getContentResolver(), Settings.Secure.ANDROID_ID);
			 * url.append(androidId);
			 */
		try {
			HttpGet httpGet = new HttpGet(url.toString());
			httpGet.setHeader("X-Mifos-Platform-TenantId", "default");
			httpGet.setHeader("Authorization", "Basic "
					+"YWRtaW46b2JzQDEyMw=="); // this is for admin/obs@123(https://spark.openbillingsystem.com/mifosng-provider/api/v1/)
				//	+ "YmlsbGluZzpiaWxsaW5nYWRtaW5AMTM=");// this is for billing/billingadmin@13(https://41.75.85.206:8080/mifosng-provider/api/v1/)// YmlsbGluZzpiaWxsaW5nYWRtaW5AMTM=
			httpGet.setHeader("Content-Type", "application/json");

			Log.i("callClientsApi", "Calling " + httpGet.getURI());

			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();

			HttpEntity entity;
			if (statusCode == 200) {
				entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				resObj.setSuccessResponse(statusCode, builder.toString());
			} else {
				entity = response.getEntity();
				String content = EntityUtils.toString(entity);
				String sError = new JSONObject(content).getJSONArray("errors")
						.getJSONObject(0).getString("developerMessage");
				resObj.setFailResponse(statusCode, sError);
				Log.e("callExternalAPI", sError + statusCode);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			resObj.setFailResponse(100, e.getMessage());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			resObj.setFailResponse(100, e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			resObj.setFailResponse(100, e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			resObj.setFailResponse(100, e.getMessage());
		}
		return resObj;
	}

	public static ResponseObj callExternalApiPostMethod(Context context,
			HashMap<String, String> param) {
		Log.d(TAG, "callExternalApi");
		ResponseObj resObj = new ResponseObj();
		StringBuilder builder = new StringBuilder();
		HttpClient client = MySSLSocketFactory.getNewHttpClient();
		String url = context.getString(R.string.server_url);
		url += (param.get("TagURL"));
		param.remove("TagURL");
		JSONObject json = new JSONObject();
		try {

			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader("X-Mifos-Platform-TenantId", "default");
			httpPost.setHeader("Authorization", "Basic "
					+"YWRtaW46b2JzQDEyMw=="); // this is for admin/obs@123(https://spark.openbillingsystem.com/mifosng-provider/api/v1/)
			//	+ "YmlsbGluZzpiaWxsaW5nYWRtaW5AMTM=");// this is for billing/billingadmin@13(https://41.75.85.206:8080/mifosng-provider/api/v1/)// YmlsbGluZzpiaWxsaW5nYWRtaW5AMTM=
		
			//		+ "YmlsbGluZzpiaWxsaW5nYWRtaW5AMTM=");
			httpPost.setHeader("Content-Type", "application/json");
			// append device id to url
			//String androidId = Settings.Secure.getString(
			//		context.getContentResolver(), Settings.Secure.ANDROID_ID);
			/*
			 * try { //json.put("deviceId",androidId);
			 * json.put("deviceId","efa4c629924f8139"); } catch (JSONException
			 * e1) { // TODO Auto-generated catch block e1.printStackTrace(); }
			 */

			for (int i = 0; i < param.size(); i++) {
				json.put((String) param.keySet().toArray()[i], (String) param
						.values().toArray()[i]);
			}
			StringEntity se = null;
			se = new StringEntity(json.toString());

			se.setContentType("application/json");
			se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json"));
			httpPost.setEntity(se);
			Log.d("callExternalApiPostMethod",
					" httpPost.getURI " + httpPost.getURI());
			Log.d("callExternalApiPostMethod", "json: " + json);
			HttpResponse response = client.execute(httpPost);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			HttpEntity entity;
			if (statusCode == 200) {
				entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				resObj.setSuccessResponse(statusCode, builder.toString());
			} else {
				entity = response.getEntity();
				String content = EntityUtils.toString(entity);
				String sError = new JSONObject(content).getJSONArray("errors")
						.getJSONObject(0).getString("developerMessage");
				resObj.setFailResponse(statusCode, sError);
				Log.e("callExternalAPI", sError + statusCode);
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			resObj.setFailResponse(100, e.getMessage());
		} catch (JSONException e) {
			e.printStackTrace();
			resObj.setFailResponse(100, e.getMessage());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			resObj.setFailResponse(100, e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			resObj.setFailResponse(100, e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			resObj.setFailResponse(100, e.getMessage());
		}
		return resObj;
	}

	public static boolean isNetworkAvailable(Context context) {
		Log.d(TAG, "getDetails");
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifiNetwork = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetwork != null && wifiNetwork.isConnected()) {
			return true;
		}

		NetworkInfo mobileNetwork = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobileNetwork != null && mobileNetwork.isConnected()) {
			return true;
		}

		NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.isConnected()) {
			return true;
		}
		return false;
	}

	public static void lockScreenOrientation(Context context,Activity activity) {
	    int currentOrientation = context.getResources().getConfiguration().orientation;
	    if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
	    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    } else {
	    	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    }
	}
	
	public static void unlockScreenOrientation(Activity activity) {
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}
	
}
