package com.example.locket.ui.settings;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.example.locket.R;

public class IconAdapter extends BaseAdapter {
    private Context context;
    private int[] icons;
    private int selectedPosition = -1;
    private LayoutInflater inflater;

    public IconAdapter(Context context, int[] icons) {
        this.context = context;
        this.icons = icons;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Log.d("IconAdapter", "Adapter initialized with " + icons.length + " icons");
    }

    @Override
    public int getCount() {
        return icons.length;
    }

    @Override
    public Object getItem(int position) {
        return icons[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_item_icon, parent, false);
            holder = new ViewHolder();
            holder.iconView = convertView.findViewById(R.id.icon_view);
            holder.iconRadio = convertView.findViewById(R.id.icon_radio);
            convertView.setTag(holder);
            Log.d("IconAdapter", "Created new view for position: " + position);
        } else {
            holder = (ViewHolder) convertView.getTag();
            Log.d("IconAdapter", "Reusing view for position: " + position);
        }

        // Gán hình ảnh icon
        holder.iconView.setImageResource(icons[position]);
        Log.d("IconAdapter", "Set icon resource for position " + position + ": " + icons[position]);

        // Kiểm tra nếu đây là icon được chọn
        holder.iconRadio.setChecked(position == selectedPosition);

        return convertView;
    }

    static class ViewHolder {
        ImageView iconView;
        RadioButton iconRadio;
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
        Log.d("IconAdapter", "Selected position set to: " + position);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }
}