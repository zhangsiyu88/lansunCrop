package com.lansun.qmyo.net;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.lansun.qmyo.app.App;
import com.lansun.qmyo.utils.GlobalValue;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class OkHttp {
	public static final OkHttpClient okHttpClient = new OkHttpClient();
	public static final int NET_STATE=0;
	/**
	 * 设置的缓存大小
	 */
	private static int cacheSize = 10 * 1024 * 1024; // 10 MiB

	private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
//	private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("text/x-markdown; charset=utf-8");

	// timeout
	static {
		okHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
		okHttpClient.setWriteTimeout(60, TimeUnit.SECONDS);
		okHttpClient.setReadTimeout(60, TimeUnit.SECONDS);
		okHttpClient.setCookieHandler(new CookieManager(new PersistentCookieStore(App.app.getApplicationContext()), CookiePolicy.ACCEPT_ALL));
		okHttpClient.setCache(new Cache(App.app.getApplicationContext().getExternalCacheDir(), cacheSize));
	}
	/**
	 * @param request
	 * @return
	 * @throws IOException
	 */
	private static Response execute(Request request) throws IOException {
		return okHttpClient.newCall(request).execute();
	}

	/**
	 *
	 * @param request
	 * @param responseCallback
	 */
	private static void enqueue(Request request, Callback responseCallback) {
		okHttpClient.newCall(request).enqueue(responseCallback);
	}
	/**
	 * 异步get
	 *
	 * @param url
	 * @param callback
	 * @return
	 */
	public static void asyncGet(String url, Callback callback) {
		Request request = new Request.Builder()
		.url(url)
		.build();
		enqueue(request, callback);
	}

	public static void asyncPost(String url, Map<String, String> body, Callback callback) {


		FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
		for (String key : body.keySet()) {
			if (TextUtils.isEmpty(body.get(key))) {
				return;
			}
			formEncodingBuilder.add(key, body.get(key));
		}
		RequestBody formBody = formEncodingBuilder.build();
		Request request = new Request.Builder()
		.url(url)
		.header("Authorization", "Bearer "+App.app.getData("access_token"))
		.post(formBody)
		.build();
		enqueue(request, callback);
	}

	// post without file with tag
	public static void asyncPost(String url, Map<String, String> body, String tag, Callback callback) {
		FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
		for (String key : body.keySet()) {
			if (TextUtils.isEmpty(body.get(key))) {
				return;
			}
			formEncodingBuilder.add(key, body.get(key));
		}
		RequestBody formBody = formEncodingBuilder.build();
		Request request = new Request.Builder()
		.url(url)
		.post(formBody)
		.tag(tag)
		.build();
		enqueue(request, callback);
	}
	/**
	 * 带有头请求的
	 * @param url
	 * @param body
	 * @param tag
	 * @param callback
	 */
	public static void asyncPost(String url,String key,String value, Map<String, String> body, String tag, Callback callback) {
		FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
		for (String keys : body.keySet()) {
			if (TextUtils.isEmpty(body.get(keys))) {
				return;
			}
			formEncodingBuilder.add(keys, body.get(keys));
		}
		RequestBody formBody = formEncodingBuilder.build();
		Request request = new Request.Builder()
		.url(url)
		.post(formBody)
		.addHeader(key, value)
		.tag(tag)
		.build();
		enqueue(request, callback);
	}
	/**
	 * @param url
	 * @param body
	 * @param tag
	 * @param callback
	 */
	public static void asyncGet(String url,String key,String value,String tag, Callback callback) {
		Request request = new Request.Builder()
		.url(url)
		.addHeader(key, value)
		.build();
		enqueue(request, callback);
	}
	//	 post with file
	public static void asyncPost(String url, Map<String, String> body, File file, Callback callback) {
		MultipartBuilder multipartBuilder = new MultipartBuilder();
        multipartBuilder.type(MultipartBuilder.FORM);
        for (String key : body.keySet()) {
            multipartBuilder.addFormDataPart(key, body.get(key));
        }
        if (file != null && file.exists()) {
            multipartBuilder.addFormDataPart("avatar","image", RequestBody.create(MEDIA_TYPE_PNG, getSmallBitmap(file.getAbsolutePath())));
        }
        RequestBody formBody = multipartBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization","Bearer "+App.app.getData("access_token"))
                .post(formBody)
                .build();
        enqueue(request, callback);
	}
	  // 根据路径获得图片并压缩，返回bitmap用于显示
    public synchronized static byte[] getSmallBitmap(String filePath) {
        ByteArrayOutputStream baos = null;
        Bitmap bitmap = null;
        Bitmap bitmapCache = null;
        byte[] bytes = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);
            options.inSampleSize = calculateInSampleSize(options, 320, 540);
            options.inJustDecodeBounds = false;
            bitmapCache = BitmapFactory.decodeFile(filePath, options);
            if (bitmapCache == null) {
                return null;
            }
            bitmap = ImageUtils.rotaingImageView(ImageUtils.readPictureDegree(filePath), bitmapCache);
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            bytes = baos.toByteArray();
        } catch (Exception e) {

        } finally {
            if (baos != null) try {
                baos.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (baos != null) try {
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }

	//计算图片的缩放值
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}
}
