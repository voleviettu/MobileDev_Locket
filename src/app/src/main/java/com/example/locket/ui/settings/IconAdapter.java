package com.example.locket.ui.settings;

import android.content.Context;
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
    private int selectedPosition = -1; // Mặc định chưa chọn icon nào
    private LayoutInflater inflater;

    public IconAdapter(Context context, int[] icons) {
        this.context = context;
        this.icons = icons;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE); // 🔥 Đảm bảo inflater không null

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
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Gán hình ảnh icon
        holder.iconView.setImageResource(icons[position]);

        // Kiểm tra nếu đây là icon được chọn
        holder.iconRadio.setChecked(position == selectedPosition);

        // Khi người dùng chọn icon, cập nhật vị trí icon đã chọn
        convertView.setOnClickListener(v -> {
            selectedPosition = position;
            notifyDataSetChanged(); // Cập nhật giao diện để chỉ một Radio Button được chọn
        });

        return convertView;
    }

    // ViewHolder để tối ưu hiệu suất
    static class ViewHolder {
        ImageView iconView;
        RadioButton iconRadio;
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged(); // Cập nhật GridView để hiển thị trạng thái chọn
    }
}