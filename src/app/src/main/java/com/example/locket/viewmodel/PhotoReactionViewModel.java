package com.example.locket.viewmodel;

import android.util.Log;
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
            Log.d("PhotoReactionViewModel", "Reaction added successfully: " + reaction.getReaction());
        }, e -> {
            Log.e("PhotoReactionViewModel", "Failed to add reaction: " + e.getMessage());
        });
    }

    public void removeReaction(String userId, String photoId) {
        repository.removeReaction(userId, photoId, unused -> {
            Log.d("PhotoReactionViewModel", "Reaction removed successfully for photoId: " + photoId);
        }, e -> {
            Log.e("PhotoReactionViewModel", "Failed to remove reaction: " + e.getMessage());
        });
    }

    public void fetchReactionsForPhoto(String photoId) {
        Log.d("PhotoReactionViewModel", "Fetching reactions for photoId: " + photoId);
        repository.getReactionsForPhoto(photoId, (QuerySnapshot snapshots, FirebaseFirestoreException e) -> {
            if (e != null) {
                Log.e("PhotoReactionViewModel", "Failed to fetch reactions for photoId: " + photoId + ", error: " + e.getMessage());
                reactionsLiveData.setValue(new ArrayList<>());
                return;
            }
            if (snapshots != null) {
                List<PhotoReaction> list = new ArrayList<>();
                for (var doc : snapshots.getDocuments()) {
                    PhotoReaction reaction = doc.toObject(PhotoReaction.class);
                    if (reaction != null) {
                        list.add(reaction);
                    }
                }
                Log.d("PhotoReactionViewModel", "Fetched " + list.size() + " reactions for photoId: " + photoId);
                reactionsLiveData.setValue(list);
            } else {
                Log.w("PhotoReactionViewModel", "No snapshots returned for photoId: " + photoId);
                reactionsLiveData.setValue(new ArrayList<>());
            }
        });
    }
}