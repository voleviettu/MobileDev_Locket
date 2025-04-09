package com.example.locket.utils;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.example.locket.ui.chat.FullChatActivity;
import com.example.locket.ui.photo.PhotoActivity;
public class NavigationUtils {

    public static void setChatButtonClickListener(View btnChat, Context context) {
        if (btnChat != null) {
            btnChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, FullChatActivity.class);
                    context.startActivity(intent);
                }
            });
        }
    }
    public static void setCaptureButtonClickListener(View btnCapture, Context context) {
        if (btnCapture != null) {
            btnCapture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, PhotoActivity.class);
                    context.startActivity(intent);
                }
            });
        }
    }
}