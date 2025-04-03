package com.example.locket.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;

public class CloudinaryUploader {

    public static String uploadImage(Context context, Uri fileUri) {
        try {
            File file = getFileFromUri(context, fileUri);

            String mimeType = context.getContentResolver().getType(fileUri);
            String resourceType = "auto";
            if (mimeType != null) {
                if (mimeType.startsWith("image/")) {
                    resourceType = "image";
                } else if (mimeType.startsWith("audio/") || mimeType.startsWith("video/")) {
                    resourceType = "video";
                }
            }

            Cloudinary cloudinary = CloudinaryHelper.getInstance();
            Map options = ObjectUtils.asMap("resource_type", resourceType);
            Map uploadResult = cloudinary.uploader().upload(file, options);

            return (String) uploadResult.get("url");

        } catch (Exception e) {
            Log.e("Upload", "Lỗi upload: " + e.getMessage(), e);
            return null;
        }
    }


    private static File getFileFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            String fileName = getFileName(context, uri);
            File tempFile = File.createTempFile(fileName, null, context.getCacheDir());
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getFileName(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex != -1 && cursor.moveToFirst()) {
                String name = cursor.getString(nameIndex);
                cursor.close();
                return name;
            }
            cursor.close();
        }
        return "temp_file";
    }
}