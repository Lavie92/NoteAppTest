package com.example.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import com.example.myapplication.models.NoteSingleton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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
        noteAdapter.setItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                if (note != null) {
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("note", note);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
        RecyclerView rcNote = findViewById(R.id.rcNote);
        SpacingItemDecoration itemDecoration = new SpacingItemDecoration();
        rcNote.addItemDecoration(itemDecoration);
        rcNote.setAdapter(noteAdapter);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        btnCreateNote = findViewById(R.id.btnCreateNote);
        btnCreateNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NoteSingleton.getInstance().getNote().setContent(""); // Không cần thiết lúc này
                noteFirebaseDAO.Insert(NoteSingleton.getInstance().getNote());

                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
            }

        });
        wipeToRemove(rcNote);
        rcNote.setLayoutManager(new LinearLayoutManager(this));
    }
    private void wipeToRemove(RecyclerView rcNote) {
        ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
                    }
                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        Note note = noteList.get(position);
                        noteFirebaseDAO.deleteNote(note.getId());
                        noteAdapter.notifyItemRemoved(position);
                    }
                });
        helper.attachToRecyclerView(rcNote);
    }
}