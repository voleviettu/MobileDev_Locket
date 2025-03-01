package com.example.locket.ui.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.locket.R;
import java.util.List;

public class SettingsAdapter extends BaseAdapter {
    private Context context;
    private List<SettingsItem> items;

    public SettingsAdapter(Context context, List<SettingsItem> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_settings, parent, false);
        }

        ImageView icon = convertView.findViewById(R.id.item_icon);
        TextView title = convertView.findViewById(R.id.item_title);

        SettingsItem item = items.get(position);
        icon.setImageResource(item.getIcon());
        title.setText(item.getTitle());

        return convertView;
    }
}