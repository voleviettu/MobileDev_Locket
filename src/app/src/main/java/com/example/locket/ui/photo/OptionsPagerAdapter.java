package com.example.locket.ui.photo;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.locket.R;

import java.util.Arrays;


public class OptionsPagerAdapter extends FragmentStateAdapter {
    private final String[] options ;

    public OptionsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        options = new String[]{
                fragmentActivity.getString(R.string.option_add_message),
                "\uD83C\uDFB5 " + fragmentActivity.getString(R.string.option_add_music),
                "\uD83C\uDF0D " + fragmentActivity.getString(R.string.option_add_location)
        };
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

