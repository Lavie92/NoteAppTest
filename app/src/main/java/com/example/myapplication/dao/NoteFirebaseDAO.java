package com.example.myapplication.dao;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.adapter.NoteAdapter;
import com.example.myapplication.models.Note;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class NoteFirebaseDAO {
    FirebaseFirestore db;
    Context context;
    public NoteFirebaseDAO(Context context)
    {
//kết nối với DB hiện tại
        db = FirebaseFirestore.getInstance();
        this.context = context;
    }
    public interface LoadNotesCallback {
        void onNotesLoaded(List<Note> notes);
    }

    public void listenNote(NoteAdapter.OnDataChangeListener listener) {
        db.collection("Note").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable
            FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("FireBase", "listen:error", e);
                    return;
                }

                List<Note> noteList = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    Note note = doc.toObject(Note.class);
                    noteList.add(note);
                }

                listener.onDataChanged(noteList);
            }
        });
    }
    public void Insert(Note p) {
// Add a new document with a generated ID
        p.setId(UUID.randomUUID().toString());
        HashMap<String, Object> mapNote = p.convertHashMap();
        db.collection("Note").document(p.getId())
                .set(mapNote)

                .addOnSuccessListener(new OnSuccessListener<Void>() {

                    @Override
                    public void onSuccess(Void unused) {
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }
    public void Update(Note note) {
        if (note.getId() != null) {
            db.collection("Note").document(note.getId()).set(note);
        }
    }
    public void deleteNote(String id) {
        db.collection("Note").document(id)
                .delete();
    }
}