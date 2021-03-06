package com.slfuture.pluto.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.slfuture.carrie.base.etc.Serial;
import com.slfuture.carrie.base.text.Text;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

/**
 * 存储器
 */
public class Storage {
	/**
	 * 隐藏构造函数
	 */
	private Storage() { }

	/**
	 * 获取存储卡根路径
	 * 
	 * @return 存储卡根路径
	 */
	public static String externalDirectory() {
		return Environment.getExternalStorageDirectory().getPath() + "/";
	}

	/**
	 * 获取存储卡相机相册根路径
	 * 
	 * @return 存储卡相机相册根路径
	 */
	public static String cameraDirectory() {
		String result = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
		if(Text.isBlank(result)) {
			return null;
		}
		if((new File(result + "/Camera/")).exists()) {
			return result + "/Camera/";
		}
		else {
			return result + "/";
		}
	}

	/**
	 * 从URI中获取实际路径
	 * 
	 * @param context 上下文
	 * @param uri 统一地址
	 */
	public static String getPathFromURI(Context context, Uri uri) {
		String result = getImageAbsolutePath(context, uri);
		if(null != result) {
			return result;
		}
		Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if(null == cursor) {
			return uri.getPath().replace("file://", "");
		}
		else {
			result = null;
			if(cursor.moveToFirst()) {
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				result = cursor.getString(column_index);
			}
			cursor.close();
			return result;
		}
	}

	/**
	 * 保存摄像头图片
	 * 
	 * @param data 摄像头数据
	 * @param path 保存文件路径
	 * @return 保存的数据
	 */
	public static File saveCamera(Intent data) {
		Bundle bundle = data.getExtras();
		Bitmap bitmap = (Bitmap) bundle.get("data");
		String path = cameraDirectory() + Serial.makeSerialString() + ".jpg";
		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(path, false);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			stream.flush();
			return new File(path);
		}
		catch (FileNotFoundException e) {
			Log.e("pluto", "call saveCamera() failed", e);
		}
		catch (IOException e) {
			Log.e("pluto", "call saveCamera() failed", e);
		}
		finally {
			try {
				if(null != stream) {
					stream.close();
				}
				stream = null;
			}
			catch (IOException e) { }
		}
		return null;
	}

	private static String getImageAbsolutePath(Context context, Uri imageUri) {  
	    if (context == null || imageUri == null)  
	        return null;  
	    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, imageUri)) {  
	        if (isExternalStorageDocument(imageUri)) {
	            String docId = DocumentsContract.getDocumentId(imageUri);
	            String[] split = docId.split(":");
	            String type = split[0];
	            if ("primary".equalsIgnoreCase(type)) {
	                return Environment.getExternalStorageDirectory() + "/" + split[1];
	            }
	        }
	        else if (isDownloadsDocument(imageUri)) {
	            String id = DocumentsContract.getDocumentId(imageUri);
	            Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));  
	            return getDataColumn(context, contentUri, null, null);
	        }
	        else if (isMediaDocument(imageUri)) {
	            String docId = DocumentsContract.getDocumentId(imageUri);
	            String[] split = docId.split(":");
	            String type = split[0];
	            Uri contentUri = null;
	            if ("image".equals(type)) {
	                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;  
	            } else if ("video".equals(type)) {  
	                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;  
	            } else if ("audio".equals(type)) {  
	                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	            }  
	            String selection = MediaStore.Images.Media._ID + "=?";  
	            String[] selectionArgs = new String[] { split[1] };
	            return getDataColumn(context, contentUri, selection, selectionArgs);
	        }
	    }
	    else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
	        if (isGooglePhotosUri(imageUri)) {
	            return imageUri.getLastPathSegment();
	        }
	        return getDataColumn(context, imageUri, null, null);
	    }
	    else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
	        return imageUri.getPath();
	    }
	    return null;  
	}
	private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
	    Cursor cursor = null;
	    String column = MediaStore.Images.Media.DATA;
	    String[] projection = {column};
	    try {
	        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
	        if (cursor != null && cursor.moveToFirst()) {
	            int index = cursor.getColumnIndexOrThrow(column);
	            return cursor.getString(index);
	        }
	    }
	    finally {
	        if(cursor != null) {
	            cursor.close();
	        }
	        cursor = null;
	    }
	    return null;
	}
	private static boolean isExternalStorageDocument(Uri uri) {
	    return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}
	private static boolean isDownloadsDocument(Uri uri) {
	    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}
	private static boolean isMediaDocument(Uri uri) {
	    return "com.android.providers.media.documents".equals(uri.getAuthority());
	}
	private static boolean isGooglePhotosUri(Uri uri) {
	    return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}
}
