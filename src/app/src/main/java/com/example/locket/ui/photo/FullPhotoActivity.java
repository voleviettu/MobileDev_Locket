package com.example.locket.ui.photo;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.locket.R;
import java.util.ArrayList;
import java.util.List;

public class FullPhotoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<Integer> imageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullphoto);

        recyclerView = findViewById(R.id.recyclerView);

        imageList = new ArrayList<>();
        imageList.add(R.drawable.ic_logo);
        imageList.add(R.drawable.ic_logo);
        imageList.add(R.drawable.ic_logo);
        imageList.add(R.drawable.ic_logo);
        imageList.add(R.drawable.ic_logo);
        imageList.add(R.drawable.ic_logo);
        imageList.add(R.drawable.ic_logo);
        imageList.add(R.drawable.ic_logo);
        imageList.add(R.drawable.ic_logo);
        imageList.add(R.drawable.ic_logo);
        imageList.add(R.drawable.ic_logo);
        imageList.add(R.drawable.ic_logo);
        imageList.add(R.drawable.ic_logo);
        imageList.add(R.drawable.ic_logo);
        imageList.add(R.drawable.ic_logo);
        imageList.add(R.drawable.ic_logo);
        imageList.add(R.drawable.ic_logo);

        imageAdapter = new ImageAdapter(this, imageList);
        recyclerView.setAdapter(imageAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {}

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int width = recyclerView.getWidth() / 3;
            for (int i = 0; i < recyclerView.getChildCount(); i++) {
                View child = recyclerView.getChildAt(i);
                ViewGroup.LayoutParams params = child.getLayoutParams();
                params.height = width;
                child.setLayoutParams(params);
            }
        });
    }
}