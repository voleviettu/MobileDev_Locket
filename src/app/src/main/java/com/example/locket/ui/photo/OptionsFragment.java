package com.example.locket.ui.photo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.locket.R;

public class OptionsFragment extends Fragment {
    private static final String ARG_TEXT = "arg_text";

    public static OptionsFragment newInstance(String text) {
        OptionsFragment fragment = new OptionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_message_option, container, false);
        Button textView = view.findViewById(R.id.option_button);
        EditText editText = view.findViewById(R.id.message_input);

        String text = getArguments() != null ? getArguments().getString(ARG_TEXT) : "";
        if (text.equals("Thêm một tin nhắn")) {
            editText.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
        } else {
            editText.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            textView.setText(text);
        }
        return view;
    }
}
