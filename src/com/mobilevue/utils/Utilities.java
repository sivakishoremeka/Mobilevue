package com.mobilevue.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.mobilevue.data.ResponseObj;
import com.mobilevue.vod.R;

public class Utilities {

	private static final String TAG = "Utilities";
	static boolean D;

	// private static ResponseObj resObj;

	public static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd",
			new Locale("en"));

	public static ResponseObj callExternalApiGetMethod(Context context,
			HashMap<String, String> param) {
		D = ((com.mobilevue.vod.MyApplication) context.getApplicationContext()).D;
		if (D)
			Log.d(TAG, "callExternalApiGetMethod");
		StringBuilder builder = new StringBuilder();
		ResponseObj resObj = new ResponseObj();
		HttpClient client = MySSLSocketFactory.getNewHttpClient();
		StringBuilder url = new StringBuilder(
				context.getString(R.string.server_url));
		url.append(param.get("TagURL"));
		param.remove("TagURL");

		if (param.size() > 0) {
			url.append("?");
			for (int i = 0; i < param.size(); i++) {
				url.append("&" + (String) param.keySet().toArray()[i] + "="
						+ (String) param.values().toArray()[i]);
			}
		}
		try {
			HttpGet httpGet = new HttpGet(url.toString());
			httpGet.setHeader("X-Mifos-Platform-TenantId", "default");
			httpGet.setHeader(
					"Authorization",
					"Basic "
							+ context
									.getString(R.string.server_Authorization_base64));
			httpGet.setHeader("Content-Type", "application/json");
			if (D)
				Log.d("callExternalApiGetMethod", " " + httpGet.getURI());

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
			} else if (statusCode == 404) {
				resObj.setFailResponse(statusCode, statusCode
						+ " Communication Error.Please try again.");
				Log.e("callExternalAPI", statusCode
						+ " Communication Error.Please try again.");
			} else {
				entity = response.getEntity();
				String content = EntityUtils.toString(entity);
				String sError = new JSONObject(content).getJSONArray("errors")
						.getJSONObject(0).getString("developerMessage");
				resObj.setFailResponse(statusCode, sError);
				Log.e("callExternalAPI", sError + statusCode);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			resObj.setFailResponse(100, "No Valid Data");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			resObj.setFailResponse(100, "Unknown Server Address.");
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
			resObj.setFailResponse(100,
					"Connection timed out.Please try again.");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			resObj.setFailResponse(100, "Please send proper information");
		} catch (Exception e) {
			e.printStackTrace();
			resObj.setFailResponse(100, "Communication Error.Please try again.");
		}
		return resObj;
	}

	public static ResponseObj callExternalApiPostMethod(Context context,
			HashMap<String, String> param) {
		D = ((com.mobilevue.vod.MyApplication) context.getApplicationContext()).D;
		if (D)
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
			httpPost.setHeader(
					"Authorization",
					"Basic "
							+ context
									.getString(R.string.server_Authorization_base64));
			httpPost.setHeader("Content-Type", "application/json");
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
			if (D)
				Log.d("callExternalApiPostMethod", " httpPost.getURI "
						+ httpPost.getURI());
			if (D)
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
			} else if (statusCode == 404) {
				resObj.setFailResponse(statusCode, statusCode
						+ " Communication Error.Please try again.");
				Log.e("callExternalAPI", statusCode
						+ " Communication Error.Please try again.");
			} else {
				entity = response.getEntity();
				String content = EntityUtils.toString(entity);
				String sError = new JSONObject(content).getJSONArray("errors")
						.getJSONObject(0).getString("developerMessage");
				resObj.setFailResponse(statusCode, sError);
				Log.e("callExternalAPI", sError + statusCode);
			}

		} catch (JSONException e) {
			e.printStackTrace();
			resObj.setFailResponse(100, "No Valid Data");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			resObj.setFailResponse(100, "Unknown Server Address.");
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
			resObj.setFailResponse(100,
					"Connection timed out.Please try again.");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			resObj.setFailResponse(100, "Please send proper information");
		} catch (Exception e) {
			e.printStackTrace();
			resObj.setFailResponse(100, "Communication Error.Please try again.");
		}
		return resObj;
	}

	public static boolean isNetworkAvailable(Context context) {
		D = ((com.mobilevue.vod.MyApplication) context.getApplicationContext()).D;
		if (D)
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

	public static <T> T parseJSON(String jsonText) {
		if (D)
			Log.d("readJsonUser", "result is \r\n" + jsonText);
		T result = null;
		try {
			ObjectMapper mapper = new ObjectMapper().setVisibility(
					JsonMethod.FIELD, Visibility.ANY);
			mapper.configure(
					DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
					false);
			result = mapper.readValue(jsonText, new TypeReference<T>() {
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
