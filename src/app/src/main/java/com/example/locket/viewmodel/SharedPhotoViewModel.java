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

    public SharedPhotoViewModel() {
        this.repository = new SharedPhotoRepository();
        this.photoRepo = new PhotoRepository();
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

    public LiveData<List<Photo>> getPhotosSharedWithMe(String friendId, String myId) {
        MutableLiveData<List<Photo>> liveData = new MutableLiveData<>();

        repository.getSharedPhotos(friendId, new PhotoRepository.FirestoreCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> photoIds) {
                if (photoIds == null || photoIds.isEmpty()) {
                    liveData.setValue(Collections.emptyList());
                } else {
                    List<String> sharedWithMe = new ArrayList<>();

                    for (String photoId : photoIds) {
                        // Kiểm tra nếu ảnh đã được chia sẻ với currentUser
                        repository.isPhotoSharedWithUser(photoId, myId, new PhotoRepository.FirestoreCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean isShared) {
                                if (isShared) {
                                    sharedWithMe.add(photoId);
                                }

                                if (sharedWithMe.size() == photoIds.size()) {
                                    if (!sharedWithMe.isEmpty()) {
                                        photoRepo.getPhotosByIds(sharedWithMe, new PhotoRepository.FirestoreCallback<List<Photo>>() {
                                            @Override
                                            public void onSuccess(List<Photo> photos) {
                                                photos.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
                                                liveData.setValue(photos);
                                            }

                                            @Override
                                            public void onFailure(Exception e) {
                                                liveData.setValue(Collections.emptyList());
                                            }
                                        });
                                    } else {
                                        liveData.setValue(Collections.emptyList());
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                liveData.setValue(Collections.emptyList());
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                liveData.setValue(Collections.emptyList());
            }
        });

        return liveData;
    }
}
