# MobileDev_Locket

## Thành phần chính

### **Model**
- Chỉ chứa dữ liệu (POJO), ánh xạ với dữ liệu trong Firestore.
- Ví dụ:
  - `User`: uid, email, firstname, lastname, username, isPremium...
  - `Friend`: userId, friendId, status, createdAt

### **Data**
- Chịu trách nhiệm giao tiếp với Firebase (Auth / Firestore)
- Truy vấn, thêm, cập nhật, xóa dữ liệu
- Ví dụ:
  - `UserRepository`: thao tác với collection `"users"`
  - `FriendRepository`: thao tác với `"friend_relationships"`

### **ViewModel**
- Xử lý logic trung gian giữa View (UI) và Repository
- Sử dụng `LiveData` để cập nhật (method `observe`) dữ liệu.
- Ví dụ:
  - `UserViewModel`
  - `FriendViewModel`

### **View (UI)**
- giao diện

### **MyApplication**
- giúp khởi tạo UserViewModel duy nhất toàn app, dùng lại dễ dàng qua:
```java
userViewModel = ((MyApplication) getApplication()).getUserViewModel();
```

### Using AI to generate caption

copy `HUGGINGFACE_API_KEY` ở dưới rồi paste vào phần value ở line 27 và 30 trong `build.gradle.kts (Module: app)`

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

## Luồng hoạt động
- View (Activity / Fragment) sẽ gọi các hàm trong ViewModel để xử lý logic hoặc lấy dữ liệu.

- ViewModel sẽ gọi tới Data (Repository) tương ứng để truy vấn hoặc cập nhật dữ liệu.

- Data (Repository) sẽ giao tiếp trực tiếp với Firebase (Firestore / Auth) để thực hiện thao tác thật.

- Kết quả sẽ được trả về lại ViewModel, sau đó thông qua LiveData để cập nhật lại giao diện (View).

- Model chỉ đơn thuần là các lớp dữ liệu ánh xạ với document trong Firestore.

- MyApplication giúp khởi tạo sẵn một số ViewModel (như UserViewModel) để dùng lại nhiều nơi trong app mà không phải khởi tạo lại.