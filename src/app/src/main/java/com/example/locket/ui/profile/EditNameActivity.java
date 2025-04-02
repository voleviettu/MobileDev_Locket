package com.example.locket.ui.profile;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log; // *** Thêm Log ***
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast; // *** Thêm Toast ***

import androidx.annotation.NonNull; // *** Thêm NonNull ***
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer; // *** Thêm Observer ***

import com.example.locket.MyApplication; // *** Thêm MyApplication ***
import com.example.locket.R;
import com.example.locket.data.UserRepository; // *** Thêm UserRepository ***
import com.example.locket.model.User; // *** Thêm User model ***
import com.example.locket.viewmodel.UserViewModel; // *** Thêm UserViewModel ***
import com.google.android.gms.tasks.OnCompleteListener; // *** Thêm OnCompleteListener ***
import com.google.android.gms.tasks.Task; // *** Thêm Task ***
import com.google.firebase.auth.FirebaseAuth; // *** Thêm FirebaseAuth ***
import com.google.firebase.auth.FirebaseUser; // *** Thêm FirebaseUser ***

public class EditNameActivity extends AppCompatActivity {

    private static final String TAG = "EditNameActivity"; 

    private EditText inputFirstName, inputLastName;
    private Button buttonSave;
    private ImageButton buttonBack;

    private UserViewModel userViewModel;
    private UserRepository userRepository;
    private FirebaseAuth mAuth;
    private String currentUserUid; // Lưu UID để dùng khi lưu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_name);

        // --- Khởi tạo ---
        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository(); // Khởi tạo repo
        try {
            userViewModel = ((MyApplication) getApplication()).getUserViewModel();
        } catch (ClassCastException e) {
            Log.e(TAG, "Application không phải là instance của MyApplication hoặc ViewModel chưa được khởi tạo.", e);
            Toast.makeText(this, "Lỗi khởi tạo dữ liệu người dùng.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // --- Lấy người dùng hiện tại ---
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Log.e(TAG, "Không có người dùng nào đăng nhập trong EditNameActivity!");
            Toast.makeText(this, "Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        currentUserUid = firebaseUser.getUid(); // Lưu UID

        // --- Ánh xạ các view ---
        inputFirstName = findViewById(R.id.input_first_name);
        inputLastName = findViewById(R.id.input_last_name);
        buttonSave = findViewById(R.id.button_save);
        buttonBack = findViewById(R.id.button_back);

        // --- Lấy và hiển thị tên hiện tại (từ ViewModel) ---
        observeCurrentUser(); // Gọi hàm để lấy và hiển thị

        // Cập nhật trạng thái ban đầu của nút Lưu
        updateSaveButtonState();

        // Xử lý sự kiện nút Back
        buttonBack.setOnClickListener(v -> finish());

        // TextWatcher để theo dõi thay đổi trong EditText
        TextWatcher textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSaveButtonState();
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        inputFirstName.addTextChangedListener(textWatcher);
        inputLastName.addTextChangedListener(textWatcher);

        // --- Xử lý sự kiện nút Lưu ---
        buttonSave.setOnClickListener(v -> {
            saveChanges(); // Gọi hàm lưu thay đổi
        });
    }

    // Hàm lắng nghe và hiển thị dữ liệu người dùng hiện tại
    private void observeCurrentUser() {
        if (userViewModel != null) {
            userViewModel.getCurrentUser().observe(this, user -> {
                if (user != null) {
                    Log.d(TAG, "Hiển thị tên hiện tại: " + user.getFirstname() + " " + user.getLastname());
                    // Chỉ điền nếu EditText đang trống (tránh ghi đè khi người dùng đang sửa)
                    if(inputFirstName.getText().toString().isEmpty()){
                        inputFirstName.setText(user.getFirstname() != null ? user.getFirstname() : "");
                    }
                    if(inputLastName.getText().toString().isEmpty()){
                        inputLastName.setText(user.getLastname() != null ? user.getLastname() : "");
                    }
                    // Cập nhật lại trạng thái nút Save sau khi điền dữ liệu
                    updateSaveButtonState();
                } else {
                    Log.w(TAG, "User null từ ViewModel khi cố gắng hiển thị tên.");
                    // Có thể thử load lại nếu cần, nhưng lý tưởng là ViewModel đã có dữ liệu
                    // userViewModel.loadUser(currentUserUid);
                }
            });
            // Đảm bảo ViewModel có dữ liệu (nếu cần)
            if (userViewModel.getCurrentUser().getValue() == null && currentUserUid != null) {
                userViewModel.loadUser(currentUserUid);
            }
        }
    }


    // Hàm xử lý lưu thay đổi
    private void saveChanges() {
        String firstName = inputFirstName.getText().toString().trim();
        String lastName = inputLastName.getText().toString().trim();

        // Kiểm tra lại validation trước khi lưu
        if (!isValidName(firstName) || !isValidName(lastName)) {
            Toast.makeText(this, "Vui lòng nhập họ và tên hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Vô hiệu hóa nút và hiển thị trạng thái đang lưu
        buttonSave.setEnabled(false);
        buttonSave.setText("Đang lưu...");

        userRepository.updateName(currentUserUid, firstName, lastName)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Cập nhật tên thành công lên Firestore.");
                    Toast.makeText(EditNameActivity.this, "Đã cập nhật tên!", Toast.LENGTH_SHORT).show();

                    User currentUser = userViewModel.getCurrentUser().getValue();
                    if (currentUser != null) {
                        currentUser.setFirstname(firstName);
                        currentUser.setLastname(lastName);
                        // Nếu có hàm update trong ViewModel: userViewModel.updateLocalUser(currentUser);
                        // Hoặc set lại value: userViewModel.getCurrentUser().setValue(currentUser); // Cần MutableLiveData
                    }

                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi cập nhật tên:", e);
                    Toast.makeText(EditNameActivity.this, "Lỗi: Không thể cập nhật tên. " + e.getMessage(), Toast.LENGTH_LONG).show();
                    updateSaveButtonState(); // Cập nhật lại trạng thái dựa trên text hiện tại
                    buttonSave.setText("Lưu");
                });
                /* // Xóa code cũ
                 System.out.println("Tên mới: " + firstName + " " + lastName);
                 finish();
                */
    }

    // Hàm kiểm tra tên có hợp lệ hay không (Giữ nguyên)
    private boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) return false; // Thêm kiểm tra null/rỗng
        // Cho phép chữ cái (có dấu tiếng Việt), khoảng trắng, độ dài hợp lý
        // Bạn có thể điều chỉnh regex nếu cần chặt chẽ hơn
        return name.trim().matches("^[\\p{L}\\s]{2,50}$"); // \\p{L} khớp với mọi chữ cái unicode
    }

    // Hàm cập nhật trạng thái của nút Lưu dựa trên dữ liệu nhập vào (Giữ nguyên)
    private void updateSaveButtonState() {
        String firstName = inputFirstName.getText().toString().trim();
        String lastName = inputLastName.getText().toString().trim();
        // Nút chỉ bật khi cả hai tên đều hợp lệ
        boolean isValid = isValidName(firstName) && isValidName(lastName);

        buttonSave.setEnabled(isValid);
        int color = isValid ? R.color.button : R.color.gray_light;
        buttonSave.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, color)));
    }
}