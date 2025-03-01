package com.example.locket.data;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper {
    private static final String CLOUD_NAME = "dnxpozctm";
    private static final String API_KEY = "558248146156451";
    private static final String API_SECRET = "E-khSfa64cf5-dDkn77f5TJa2v4";

    private static Cloudinary cloudinary;

    public static Cloudinary getInstance() {
        if (cloudinary == null) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", CLOUD_NAME);
            config.put("api_key", API_KEY);
            config.put("api_secret", API_SECRET);
            cloudinary = new Cloudinary(config);
        }
        return cloudinary;
    }
}