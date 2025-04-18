package com.example.locket.ui.photo;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locket.R;
import com.example.locket.model.User;
import com.example.locket.ui.settings.FriendPopupAdapter;

import java.util.ArrayList;
import java.util.List;

public class FriendDialog extends DialogFragment {

    private final List<User> allFriends;
    private final OnFriendSelected listener;

    public interface OnFriendSelected {
        void onSelected(User user); // null nếu chọn "Tất cả bạn bè"
    }

    public FriendDialog(List<User> friends, OnFriendSelected listener) {
        this.allFriends = friends;
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_friend_popup, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerFriends);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<User> friendListWithAll = new ArrayList<>();
        friendListWithAll.add(new User("0", null, null, null, getString(R.string.all_friends), "ic_friends", false));
        friendListWithAll.addAll(allFriends);

        FriendPopupAdapter adapter = new FriendPopupAdapter(friendListWithAll, friend -> {
            dismiss();
            if ("0".equals(friend.getUid())) {
                listener.onSelected(null);
            } else {
                listener.onSelected(friend);
            }
        });

        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            // Set layout width/height
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // Căn giữa popup
            window.setGravity(Gravity.CENTER);
            // Tùy chọn nền trong suốt nếu bạn muốn
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}
