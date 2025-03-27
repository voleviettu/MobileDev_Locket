package com.example.locket.viewmodel;

import android.content.Context;
import android.net.Uri;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.locket.data.PhotoRepository;
import com.example.locket.model.Photo;
import com.google.firebase.firestore.GeoPoint;
import java.util.List;

public class PhotoViewModel extends ViewModel {
    private final PhotoRepository photoRepository;
    private final MutableLiveData<List<Photo>> userPhotos = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isUploading = new MutableLiveData<>();

    public PhotoViewModel() {
        photoRepository = new PhotoRepository();
        isUploading.setValue(false);
    }

    public LiveData<List<Photo>> getUserPhotos() {
        return userPhotos;
    }

    public LiveData<Boolean> getIsUploading() {
        return isUploading;
    }


    public void loadUserPhotos(String userId) {
        photoRepository.getPhotosByUser(userId, new PhotoRepository.FirestoreCallback<List<Photo>>() {
            @Override
            public void onSuccess(List<Photo> photos) {
                userPhotos.setValue(photos);
            }

            @Override
            public void onFailure(Exception e) {
                userPhotos.setValue(null);
            }
        });
    }


    public void uploadPhoto(Context context, Uri fileUri, String userId, String caption, String musicUrl, String location, java.util.function.Consumer<String> onSuccessPhotoId) {
        isUploading.setValue(true);

        photoRepository.uploadAndSavePhoto(context, fileUri, userId, caption, musicUrl, location, new PhotoRepository.FirestoreCallback<String>() {
            @Override
            public void onSuccess(String photoId) {
                isUploading.setValue(false);
                loadUserPhotos(userId);
                onSuccessPhotoId.accept(photoId);
            }

            @Override
            public void onFailure(Exception e) {
                isUploading.setValue(false);
            }
        });
    }
}
