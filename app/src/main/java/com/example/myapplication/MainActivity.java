package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.example.myapplication.adapter.NoteAdapter;
import com.example.myapplication.dao.NoteFirebaseDAO;
import com.example.myapplication.decoration.SpacingItemDecoration;
import com.example.myapplication.models.Note;
import com.example.myapplication.models.NoteSingleton;
import com.example.myapplication.woker.ConstantsManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<Note> noteList = new ArrayList<>();
    List<Note> filteredNotes = new ArrayList<>();

    Button btnCreateNote;
    EditText editTextSearch; // Changed from SearchView to EditText
    NoteAdapter noteAdapter;
    private NoteFirebaseDAO noteFirebaseDAO = new NoteFirebaseDAO(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "My Channel"; // Tên kênh
                String description = "Channel description"; // Mô tả kênh
                int importance = NotificationManager.IMPORTANCE_HIGH; // Độ quan trọng
                NotificationChannel
                        channel = new NotificationChannel(ConstantsManager.CHANNEL_ID, name, importance);
                channel.setDescription(description);

                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }

        noteAdapter = new NoteAdapter(filteredNotes, getApplicationContext(), noteFirebaseDAO);
        noteFirebaseDAO.listenNote(new NoteAdapter.OnDataChangeListener() {
            @Override
            public void onDataChanged(List<Note> notes) {
                noteList.clear();
                noteList.addAll(notes);
                filterNotes("");
                noteAdapter.notifyDataSetChanged();
            }
        });

        RecyclerView rcNote = findViewById(R.id.rcNote);
        SpacingItemDecoration itemDecoration = new SpacingItemDecoration();

        btnCreateNote = findViewById(R.id.btnCreateNote);
        btnCreateNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NoteSingleton.getInstance().getNote().setContent("");
                noteFirebaseDAO.Insert(NoteSingleton.getInstance().getNote());
                Intent intent = new Intent(MainActivity.this, NoteDetailActivity.class);
                startActivity(intent);
            }
        });
        noteAdapter.setItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                if (note != null) {
                    Intent intent = new Intent(MainActivity.this, NoteDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("note", note);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });

        editTextSearch = findViewById(R.id.searchView); // Find EditText by ID

        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterNotes(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        rcNote.addItemDecoration(itemDecoration);
        rcNote.setAdapter(noteAdapter);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        wipeToRemove(rcNote);
        rcNote.setLayoutManager(new LinearLayoutManager(this));
    }

    private void wipeToRemove(RecyclerView rcNote) {
        ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
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

    private void filterNotes(String searchText) {
        filteredNotes.clear();
        for (Note note : noteList) {
            if (note.getContent().toLowerCase().contains(searchText.toLowerCase())) {
                filteredNotes.add(note);
            }
        }
        noteAdapter.notifyDataSetChanged();
    }
}
