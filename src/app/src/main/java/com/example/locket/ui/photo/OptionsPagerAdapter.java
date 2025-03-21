package com.example.locket.ui.photo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;


public class OptionsPagerAdapter extends FragmentStateAdapter {
    private final String[] options = {"Thêm một tin nhắn", "\uD83C\uDFB5 Thêm nhạc", "\uD83C\uDF0D Thêm vị trí"};

    public OptionsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return OptionsFragment.newInstance(options[position]);
    }

    @Override
    public int getItemCount() {
        return options.length;
    }
}

