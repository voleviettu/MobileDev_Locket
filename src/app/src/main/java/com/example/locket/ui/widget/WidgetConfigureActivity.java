package com.example.locket.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.locket.MyApplication;
import com.example.locket.R;
import com.example.locket.model.User;
import com.example.locket.viewmodel.FriendViewModel;
import com.example.locket.viewmodel.UserViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WidgetConfigureActivity extends AppCompatActivity {

    private static final String TAG = "WidgetConfigure";
    private static final String PREFS_NAME = "com.example.locket.widget.WidgetPrefs";
    private static final String PREF_PREFIX_KEY = "widget_friend_";
    public static final String KEY_FRIEND_ID = "FRIEND_ID"; // Key cho SharedPreferences
    public static final String VALUE_ALL_FRIENDS = "ALL"; // Giá trị đặc biệt cho tất cả bạn bè
    public static final String VALUE_SELF = "SELF";      // Giá trị đặc biệt cho chính mình

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private FriendViewModel friendViewModel;
    private UserViewModel userViewModel;
    private RecyclerView rvFriendList;
    private FriendSelectionAdapter adapter;
    private String currentUserId;
    private List<User> displayFriendList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Đặt kết quả mặc định là CANCELED
        setResult(RESULT_CANCELED);

        setContentView(R.layout.activity_widget_configure);
        rvFriendList = findViewById(R.id.rv_friend_list);
        rvFriendList.setLayoutManager(new LinearLayoutManager(this));

        // Lấy AppWidgetId từ Intent
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // Nếu không có AppWidgetId, đóng Activity
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e(TAG, "Invalid AppWidgetId");
            finish();
            return;
        }

        // Khởi tạo ViewModels
        friendViewModel = new ViewModelProvider(this).get(FriendViewModel.class);
        // Giả sử UserViewModel có sẵn từ Application
        userViewModel = ((MyApplication) getApplication()).getUserViewModel();

        userViewModel.getCurrentUser().observe(this, currentUser -> {
            if (currentUser == null) {
                Toast.makeText(this, "Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            currentUserId = currentUser.getUid();

            // Thêm lựa chọn "Tất cả bạn bè"
            displayFriendList.clear();
            User allFriendsOption = new User(VALUE_ALL_FRIENDS, "", "", "Tất cả bạn bè", "", "", false);
            displayFriendList.add(allFriendsOption);

            // Thêm lựa chọn "Chỉ mình bạn"
            User selfOption = new User(VALUE_SELF, currentUser.getEmail(), "", "Chỉ mình bạn", currentUser.getUsername(), currentUser.getAvatar(), currentUser.isPremium());
            displayFriendList.add(selfOption);


            // Load bạn bè thật
            friendViewModel.loadFriends(currentUserId);
            friendViewModel.getFriends().observe(this, friends -> {
                if (friends != null) {
                    // Thêm bạn bè vào sau các lựa chọn đặc biệt
                    displayFriendList.addAll(friends);
                }
                // Cập nhật RecyclerView
                if (adapter == null) {
                    adapter = new FriendSelectionAdapter(this, displayFriendList, selectedFriend -> {
                        // Người dùng đã chọn
                        saveConfiguration(this, appWidgetId, selectedFriend);
                        triggerWidgetUpdate(this, appWidgetId);
                        // Đặt kết quả là OK và đóng Activity
                        Intent resultValue = new Intent();
                        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                        setResult(RESULT_OK, resultValue);
                        finish();
                    });
                    rvFriendList.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged(); // Cập nhật nếu danh sách thay đổi
                }
            });
        });
    }

    // Lưu cấu hình vào SharedPreferences
    static void saveConfiguration(Context context, int appWidgetId, User selectedFriend) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        String selectionValue;
        if (selectedFriend == null || VALUE_ALL_FRIENDS.equals(selectedFriend.getUid())) {
            selectionValue = VALUE_ALL_FRIENDS; // Dùng giá trị đặc biệt
        } else if (VALUE_SELF.equals(selectedFriend.getUid())){
            selectionValue = VALUE_SELF; // Dùng giá trị đặc biệt
        }
        else {
            selectionValue = selectedFriend.getUid(); // Lưu ID bạn bè
        }
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, selectionValue);
        prefs.apply();
        Log.d(TAG, "Saved config for widget " + appWidgetId + ": " + selectionValue);
    }

    // Đọc cấu hình
    static String loadConfiguration(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        // Mặc định là ALL nếu chưa có cấu hình
        return prefs.getString(PREF_PREFIX_KEY + appWidgetId, VALUE_ALL_FRIENDS);
    }

    // Xóa cấu hình
    static void deleteConfiguration(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
        Log.d(TAG, "Deleted config for widget " + appWidgetId);
    }

    static void triggerWidgetUpdate(Context context, int appWidgetId) {
        Log.d(TAG, "Scheduling periodic updates for widget " + appWidgetId);

        String uniqueWorkName = "widget_update_" + appWidgetId;

        // Pass the widgetId along so your Worker knows which instance to update:
        Data inputData = new Data.Builder()
                .putInt(WidgetUpdateWorker.WIDGET_ID_KEY, appWidgetId)
                .build();

        // Build a periodic request — Android mandates a minimum 15‑minute interval:
        PeriodicWorkRequest periodicRequest = new PeriodicWorkRequest.Builder(
                WidgetUpdateWorker.class,
                30, TimeUnit.MINUTES        // choose your cadence (>= 15 minutes)
        )
                .setInputData(inputData)
                // (optional) only run when there’s a network connection:
                .setConstraints(
                        new Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                )
                .build();

        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniquePeriodicWork(
                        uniqueWorkName,                     // distinct per widget instance
                        ExistingPeriodicWorkPolicy.REPLACE,    // keep the very first periodic job
                        periodicRequest
                );
    }

}
