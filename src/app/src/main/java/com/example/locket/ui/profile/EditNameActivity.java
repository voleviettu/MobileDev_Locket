package com.example.locket.ui.profile;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.locket.R;

public class EditNameActivity extends AppCompatActivity {

    private EditText inputFirstName, inputLastName;
    private Button buttonSave;
    private ImageButton buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_name);

        // Ánh xạ các view
        inputFirstName = findViewById(R.id.input_first_name);
        inputLastName = findViewById(R.id.input_last_name);
        buttonSave = findViewById(R.id.button_save);
        buttonBack = findViewById(R.id.button_back);

        // Cập nhật trạng thái ban đầu của nút Lưu
        updateSaveButtonState();

        // Xử lý sự kiện nút Back
        buttonBack.setOnClickListener(v -> finish());

        // TextWatcher để theo dõi thay đổi trong EditText
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSaveButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        inputFirstName.addTextChangedListener(textWatcher);
        inputLastName.addTextChangedListener(textWatcher);

        // Xử lý sự kiện nút Lưu
        buttonSave.setOnClickListener(v -> {
            // Lấy dữ liệu từ EditText
            String firstName = inputFirstName.getText().toString();
            String lastName = inputLastName.getText().toString();

            // TODO: Xử lý lưu tên mới (ví dụ: lưu vào database, shared preferences, ...)
            // Bạn cần tự triển khai phần này tùy theo cách bạn lưu trữ thông tin người dùng

            // Ví dụ: In ra log
            System.out.println("Tên mới: " + firstName + " " + lastName);

            // Sau khi lưu, có thể quay lại ProfileActivity hoặc thông báo thành công
            finish();
        });
    }

    // Hàm kiểm tra tên có hợp lệ hay không
    private boolean isValidName(String name) {
        return name.matches("^[a-zA-ZÀ-Ỹà-ỹ\\s]{2,50}$"); // Điều kiện: 2-50 ký tự, chỉ chứa chữ cái và khoảng trắng
    }

    // Hàm cập nhật trạng thái của nút Lưu dựa trên dữ liệu nhập vào
    private void updateSaveButtonState() {
        String firstName = inputFirstName.getText().toString().trim();
        String lastName = inputLastName.getText().toString().trim();
        boolean isValid = isValidName(firstName) && isValidName(lastName);

        buttonSave.setEnabled(isValid);
        int color = isValid ? R.color.button : R.color.gray_light;
        buttonSave.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, color)));
    }
}