package com.mobilevue.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.mobilevue.vod.R;

public class ImageLoader {
	private static final int CONNECT_TIMEOUT = 5000;
	private static final int READ_TIMEOUT = 10000;

	MemoryCache memoryCache = new MemoryCache();
	FileCache fileCache;
	private Map<ImageView, String> imageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	ExecutorService executorService;

	public ImageLoader(Context context) {
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(5);
	}

	final int stub_id = R.drawable.img_film_clap;

	public void DisplayImage(String url, ImageView imageView) {

		imageViews.put(imageView, url);
		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null)
			imageView.setImageBitmap(bitmap);
		else {
			queuePhoto(url, imageView);
			imageView.setImageResource(stub_id);
		}
	}

	private void queuePhoto(String url, ImageView imageView) {
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		executorService.submit(new PhotosLoader(p));
	}

	/*
	 * private Bitmap getBitmap(String url) { File f=fileCache.getFile(url);
	 * 
	 * //from SD cache Bitmap b = decodeFile(f); if(b!=null) return b;
	 * 
	 * //from web try { Bitmap bitmap=null; URL imageUrl = new URL(url);
	 * HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
	 * conn.setConnectTimeout(30000); conn.setReadTimeout(30000);
	 * conn.setInstanceFollowRedirects(true); InputStream
	 * is=conn.getInputStream(); OutputStream os = new FileOutputStream(f);
	 * Utils.CopyStream(is, os); os.close(); bitmap = decodeFile(f); return
	 * bitmap; } catch (Exception ex){ ex.printStackTrace(); return null; } }
	 */

	// To Retrive the Image From the Url shaik
	private Bitmap getBitmap(String url) {

		File f = fileCache.getFile(url);
		Bitmap bitmap = null;

		// from SD cache
		Bitmap b = decodeFile(f);
		if (b != null)
			return b;

		try {

			// added for handling https request....
			if (url.contains("https")) {

				// Create a trust manager that does not validate certificate
				// chains
				TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

					@Override
					public X509Certificate[] getAcceptedIssuers() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public void checkServerTrusted(X509Certificate[] chain,
							String authType) throws CertificateException {
						// TODO Auto-generated method stub

					}

					@Override
					public void checkClientTrusted(X509Certificate[] chain,
							String authType) throws CertificateException {
						// TODO Auto-generated method stub

					}
				} };
				// Install the all-trusting trust manager
				final SSLContext sc = SSLContext.getInstance("TLS");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc
						.getSocketFactory());
				// Create all-trusting host name verifier
				HostnameVerifier allHostsValid = new HostnameVerifier() {
					@Override
					public boolean verify(String hostname, SSLSession session) {
						// TODO Auto-generated method stub
						return true;
					}
				};
				// Install the all-trusting host verifier
				HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

				URL image = new URL(url);
				URLConnection conn = image.openConnection();
				conn.setConnectTimeout(CONNECT_TIMEOUT);
				conn.setReadTimeout(READ_TIMEOUT);
				bitmap = BitmapFactory.decodeStream((InputStream) conn
						.getContent());

			} else {
				URLConnection conn = new URL(url).openConnection();
				conn.setConnectTimeout(CONNECT_TIMEOUT);
				conn.setReadTimeout(READ_TIMEOUT);
				bitmap = BitmapFactory.decodeStream((InputStream) conn
						.getContent());

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bitmap;
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE = 70;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE
						|| height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;

		public PhotoToLoad(String u, ImageView i) {
			url = u;
			imageView = i;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			Bitmap bmp = getBitmap(photoToLoad.url);
			memoryCache.put(photoToLoad.url, bmp);
			if (imageViewReused(photoToLoad))
				return;
			BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
			Activity a = (Activity) photoToLoad.imageView.getContext();
			a.runOnUiThread(bd);
		}
	}

	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			if (bitmap != null)
				photoToLoad.imageView.setImageBitmap(bitmap);
			else
				photoToLoad.imageView.setImageResource(stub_id);
		}
	}

	public void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}

	/*
	 * private Bitmap getBitmapFromUrl(String sUrl) { Bitmap bitmap = null;
	 * 
	 * try { Bitmap bitmap = null; //added for handling https request....
	 * if(url.contains("https")){
	 * 
	 * // Create a trust manager that does not validate certificate chains
	 * TrustManager[] trustAllCerts = new TrustManager[] { new
	 * X509TrustManager() {
	 * 
	 * @Override public X509Certificate[] getAcceptedIssuers() { // TODO
	 * Auto-generated method stub return null; }
	 * 
	 * @Override public void checkServerTrusted(X509Certificate[] chain, String
	 * authType) throws CertificateException { // TODO Auto-generated method
	 * stub
	 * 
	 * }
	 * 
	 * @Override public void checkClientTrusted(X509Certificate[] chain, String
	 * authType) throws CertificateException { // TODO Auto-generated method
	 * stub
	 * 
	 * } }}; // Install the all-trusting trust manager final SSLContext sc =
	 * SSLContext.getInstance("TLS"); sc.init(null, trustAllCerts, new
	 * java.security.SecureRandom());
	 * HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory()); //
	 * Create all-trusting host name verifier HostnameVerifier allHostsValid =
	 * new HostnameVerifier() {
	 * 
	 * @Override public boolean verify(String hostname, SSLSession session) { //
	 * TODO Auto-generated method stub return true; } }; // Install the
	 * all-trusting host verifier
	 * HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	 * 
	 * URL url = new URL(sUrl); URLConnection conn = url.openConnection();
	 * conn.setConnectTimeout(CONNECT_TIMEOUT);
	 * conn.setReadTimeout(READ_TIMEOUT); bitmap =
	 * BitmapFactory.decodeStream((InputStream) conn.getContent());
	 * 
	 * } else{ URLConnection conn = new URL(url).openConnection();
	 * conn.setConnectTimeout(CONNECT_TIMEOUT);
	 * conn.setReadTimeout(READ_TIMEOUT); bitmap =
	 * BitmapFactory.decodeStream((InputStream) conn.getContent()); } }
	 * catch(Exception e) { e.printStackTrace(); } return bitmap; }
	 */

}
