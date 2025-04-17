package com.example.locket.ui.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class LocketWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "LocketWidgetProvider";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Cập nhật cho tất cả các widget IDs được cung cấp
        Log.d(TAG, "onUpdate called for " + appWidgetIds.length + " widgets");
        for (int appWidgetId : appWidgetIds) {
            Log.d(TAG, ">>> Provider: onUpdate BẮT ĐẦU <<<");
            WidgetConfigureActivity.triggerWidgetUpdate(context, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Xóa cấu hình khi widget bị xóa
        Log.d(TAG, ">>> Provider: onDeleted BẮT ĐẦU <<<");
        Log.d(TAG, "onDeleted called for " + appWidgetIds.length + " widgets");
        for (int appWidgetId : appWidgetIds) {
            WidgetConfigureActivity.deleteConfiguration(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, ">>> Provider: onEnabled BẮT ĐẦU <<<");
        // Được gọi khi widget đầu tiên thuộc loại này được thêm
        Log.d(TAG, "onEnabled");
    }

    @Override
    public void onDisabled(Context context) {
        Log.d(TAG, ">>> Provider: onDisabled BẮT ĐẦU <<<");
        // Được gọi khi widget cuối cùng thuộc loại này bị xóa
        Log.d(TAG, "onDisabled");
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Log.d(TAG, ">>> Provider: onAppWidgetOptionsChanged BẮT ĐẦU <<<");
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        // Được gọi khi kích thước widget thay đổi, có thể trigger update nếu cần load lại ảnh theo size mới
        Log.d(TAG, "onAppWidgetOptionsChanged for widget " + appWidgetId);
        // WidgetConfigureActivity.triggerWidgetUpdate(context, appWidgetId);
    }
}
