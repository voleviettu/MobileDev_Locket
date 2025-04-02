package com.example.locket.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.locket.data.PhotoRepository;
import com.example.locket.data.SharedPhotoRepository;
import com.example.locket.model.Photo;
import com.example.locket.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SharedPhotoViewModel extends ViewModel {
    private final SharedPhotoRepository repository;
    private final PhotoRepository photoRepo;
    private final MutableLiveData<List<Photo>> sharedPhotos = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public SharedPhotoViewModel(SharedPhotoRepository repository, PhotoRepository photoRepo) {
        this.repository = repository;
        this.photoRepo = photoRepo;
    }

    public LiveData<List<Photo>> getSharedPhotos(String userId) {
        repository.getSharedPhotos(userId, new PhotoRepository.FirestoreCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> photoIds) {
                if (photoIds == null || photoIds.isEmpty()) {
                    sharedPhotos.setValue(Collections.emptyList());
                } else {
                    photoRepo.getPhotosByIds(photoIds, new PhotoRepository.FirestoreCallback<List<Photo>>() {
                        @Override
                        public void onSuccess(List<Photo> photos) {
                            photos.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
                            sharedPhotos.setValue(photos);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            errorMessage.setValue("Không thể tải danh sách ảnh: " + e.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.setValue("Không thể lấy danh sách ảnh được chia sẻ: " + e.getMessage());
            }
        });
        return sharedPhotos;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void sharePhoto(String photoId, String senderId, List<User> allFriends, List<String> selectedFriendIds) {
        List<String> receivers = selectedFriendIds != null && !selectedFriendIds.isEmpty()
                ? selectedFriendIds
                : new ArrayList<>();

        if (receivers.isEmpty()) {
            for (User friend : allFriends) {
                if (!friend.getUid().equals(senderId)) {
                    receivers.add(friend.getUid());
                }
            }
        }

        for (String receiverId : receivers) {
            repository.sharePhotoToUser(photoId, senderId, receiverId);
        }
    }
}
