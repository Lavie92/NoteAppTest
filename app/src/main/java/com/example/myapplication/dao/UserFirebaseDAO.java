package com.example.myapplication.dao;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.adapter.NoteAdapter;
import com.example.myapplication.models.Note;
import com.example.myapplication.models.UserInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class UserFirebaseDAO {
    FirebaseFirestore db;
    Context context;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    public UserFirebaseDAO(Context context)
    {
        db = FirebaseFirestore.getInstance();
        this.context = context;
    }
    public interface LoadNotesCallback {
        void onNotesLoaded(List<Note> notes);
    }



    public void Update(UserInfo userInfo) {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser.getUid() != null) {
            db.collection("users").document(currentUser.getUid()).set(userInfo);
        }
    }
    public void deleteNote(String id) {
        db.collection("users").document(id)
                .delete();
    }
}