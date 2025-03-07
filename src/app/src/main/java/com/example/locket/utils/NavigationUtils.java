package com.example.locket.utils;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.example.locket.ui.chat.FullChatActivity;

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
}