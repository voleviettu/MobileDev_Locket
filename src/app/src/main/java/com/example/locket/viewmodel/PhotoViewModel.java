package com.example.locket.viewmodel;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.locket.data.PhotoRepository;
import com.example.locket.model.Photo;

import java.util.List;
import java.util.function.Consumer;

public class PhotoViewModel extends ViewModel {
    private final PhotoRepository photoRepository;
    private final MutableLiveData<List<Photo>> userPhotos = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUploading = new MutableLiveData<>();
    private final MutableLiveData<String> uploadError = new MutableLiveData<>(); // Thêm LiveData cho lỗi

    public PhotoViewModel() {
        photoRepository = new PhotoRepository();
        isUploading.setValue(false);
        uploadError.setValue(null); // Khởi tạo lỗi là null
    }

    public LiveData<List<Photo>> getUserPhotos() {
        return userPhotos;
    }

    public LiveData<Boolean> getIsUploading() {
        return isUploading;
    }

    public LiveData<String> getUploadError() {
        return uploadError; // Trả về LiveData để quan sát lỗi
    }

    public void loadUserPhotos(String userId) {
        photoRepository.getPhotosByUser(userId, new PhotoRepository.FirestoreCallback<List<Photo>>() {
            @Override
            public void onSuccess(List<Photo> photos) {
                userPhotos.postValue(photos);
            }

            @Override
            public void onFailure(Exception e) {
                userPhotos.postValue(null);
            }
        });
    }

    public void uploadPhoto(Context context, Uri fileUri, String userId, String caption, String musicUrl, String location, Consumer<String> onSuccessPhotoId) {
        isUploading.postValue(true);
        uploadError.postValue(null); // Reset lỗi trước khi upload

        photoRepository.uploadAndSavePhoto(context, fileUri, userId, caption, musicUrl, location, new PhotoRepository.FirestoreCallback<String>() {
            @Override
            public void onSuccess(String photoId) {
                isUploading.postValue(false);
                loadUserPhotos(userId);
                onSuccessPhotoId.accept(photoId);
            }

            @Override
            public void onFailure(Exception e) {
                isUploading.postValue(false);
                uploadError.postValue("Lỗi khi upload ảnh: " + e.getMessage()); // Gửi thông báo lỗi
            }
        });
    }
}