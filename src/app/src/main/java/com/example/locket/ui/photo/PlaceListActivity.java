package com.example.locket.ui.photo;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locket.R;

import java.util.ArrayList;

public class PlaceListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText searchInput;

    private ArrayList<String[]> placeList;
    private ArrayList<String[]> filteredList;
    private RecyclerView.Adapter<RecyclerView.ViewHolder> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_list);

        searchInput = findViewById(R.id.et_search);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        placeList = (ArrayList<String[]>) getIntent().getSerializableExtra("places");
        if (placeList == null) placeList = new ArrayList<>();

        filteredList = new ArrayList<>(placeList);

        adapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @Override
            public int getItemCount() {
                return filteredList.size();
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
                android.view.View itemView = android.view.LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_place, parent, false);
                return new RecyclerView.ViewHolder(itemView) {};
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                String[] item = filteredList.get(position);
                String name = item[0];
                String address = item[1];

                TextView text1 = holder.itemView.findViewById(R.id.tv_name);
                TextView text2 = holder.itemView.findViewById(R.id.tv_detail_address);

                text1.setText(name);
                text2.setText(address);

                holder.itemView.setOnClickListener(v -> {
                    Intent result = new Intent();
                    result.putExtra("selected_name", name);
                    setResult(RESULT_OK, result);
                    finish();
                });
            }
        };

        recyclerView.setAdapter(adapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPlaces(s.toString());
            }
        });
    }

    private void filterPlaces(String keyword) {
        filteredList.clear();
        String lowerKeyword = keyword.toLowerCase();

        for (String[] item : placeList) {
            String name = item[0].toLowerCase();
            String address = item[1].toLowerCase();

            if (name.contains(lowerKeyword) || address.contains(lowerKeyword)) {
                filteredList.add(item);
            }
        }

        adapter.notifyDataSetChanged();
    }
}
