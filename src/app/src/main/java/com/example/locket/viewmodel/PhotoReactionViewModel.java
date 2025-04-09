package com.example.locket.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.locket.model.PhotoReaction;
import com.example.locket.data.PhotoReactionRepository;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class PhotoReactionViewModel extends ViewModel {

    private final PhotoReactionRepository repository;
    private final MutableLiveData<List<PhotoReaction>> reactionsLiveData = new MutableLiveData<>();

    public PhotoReactionViewModel() {
        repository = new PhotoReactionRepository();
    }

    public LiveData<List<PhotoReaction>> getReactionsLiveData() {
        return reactionsLiveData;
    }

    public void addReaction(PhotoReaction reaction) {
        repository.addReaction(reaction, unused -> {
        }, e -> {
        });
    }

    public void removeReaction(String userId, String photoId) {
        repository.removeReaction(userId, photoId, unused -> {}, e -> {});
    }

    public void fetchReactionsForPhoto(String photoId) {
        repository.getReactionsForPhoto(photoId, (QuerySnapshot snapshots, FirebaseFirestoreException e) -> {
            if (snapshots != null) {
                List<PhotoReaction> list = new ArrayList<>();
                for (var doc : snapshots.getDocuments()) {
                    PhotoReaction reaction = doc.toObject(PhotoReaction.class);
                    if (reaction != null) list.add(reaction);
                }
                reactionsLiveData.setValue(list);
            }
        });
    }
}
