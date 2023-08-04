package com.example.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.myapplication.adapter.NoteAdapter;
import com.example.myapplication.dao.NoteFirebaseDAO;
import com.example.myapplication.decoration.SpacingItemDecoration;
import com.example.myapplication.models.Note;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView btnBack;
    List<Note> noteList = new ArrayList<>();
    Button btnCreateNote;
    NoteAdapter noteAdapter;
    private NoteFirebaseDAO noteFirebaseDAO;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        noteFirebaseDAO = new NoteFirebaseDAO(this);
        noteAdapter = new NoteAdapter(noteList, getApplicationContext(), noteFirebaseDAO);
        noteFirebaseDAO.listenNote(new NoteAdapter.OnDataChangeListener() {
            @Override
            public void onDataChanged(List<Note> notes) {
                noteList.clear();
                noteList.addAll(notes);
                noteAdapter.notifyDataSetChanged();
            }

        });
        RecyclerView rcNote = findViewById(R.id.rcNote);
        SpacingItemDecoration itemDecoration = new SpacingItemDecoration();
        rcNote.addItemDecoration(itemDecoration);
        rcNote.setAdapter(noteAdapter);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        Button btnCreateNote = findViewById(R.id.btnCreateNote);
        btnCreateNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
            }

        });

        rcNote.setLayoutManager(new LinearLayoutManager(this));
    }

}