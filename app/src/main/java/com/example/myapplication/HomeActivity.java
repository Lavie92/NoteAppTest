package com.example.myapplication;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.example.myapplication.dao.NoteFirebaseDAO;
import com.example.myapplication.models.Note;
import com.example.myapplication.models.NoteSingleton;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    Button btnBack;
    EditText editTextContent;
    TextWatcher textWatcher;
    final Note newNote = NoteSingleton.getInstance().getNote();

    List<Note> noteList = new ArrayList<>();
    NoteFirebaseDAO noteFirebaseDAO;
    int i = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        editTextContent = findViewById(R.id.editTextContent);
        btnBack = findViewById(R.id.btnBackToHome);
        noteFirebaseDAO = new NoteFirebaseDAO(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Note note = (Note) bundle.getSerializable("note");
            editTextContent.setText(note.getContent());
        }
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                long currentTimeMillis = System.currentTimeMillis();
                Date date = new Date(currentTimeMillis);
                newNote.setDateTime(date);
                newNote.setContent(s.toString());

                    noteFirebaseDAO.Update(newNote);
                if (intent.hasExtra("note")) {
                    Note note = (Note) intent.getSerializableExtra("note");
                    note.setDateTime(date);
                    note.setContent(s.toString());
                    noteFirebaseDAO.Update(note);
                }
                handleTextChange(s);
            }
        };
        editTextContent.addTextChangedListener(textWatcher);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        String content = editTextContent.getText().toString().trim();
        Intent intent = getIntent();
        if (intent.hasExtra("note")) {
            Note note = (Note) intent.getSerializableExtra("note");
            if (content.isEmpty() && note != null) {
                noteFirebaseDAO.deleteNote(note.getId());
            }
        }
        else if (content.isEmpty() && newNote != null) {
            noteFirebaseDAO.deleteNote(newNote.getId());
        }
    }
    @Override
    protected void onPause() {
        super.onPause();

        String content = editTextContent.getText().toString().trim();

        if (content.isEmpty()) {
            noteFirebaseDAO.deleteNote(newNote.getId());
        }
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Note note = (Note) bundle.getSerializable("note");
            if (note.getContent().toString().trim().isEmpty())
                noteFirebaseDAO.deleteNote(note.getId());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String content = editTextContent.getText().toString().trim();
        if (content.isEmpty()) {
            noteFirebaseDAO.deleteNote(newNote.getId());
        }
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Note note = (Note) bundle.getSerializable("note");
            if (note.getContent().toString().trim().isEmpty())
            noteFirebaseDAO.deleteNote(note.getId());
        }
    }

    private void handleTextChange(Editable editable) {

        String text = editable.toString();
        int lineBreakIndex = text.indexOf("\n");
        if (lineBreakIndex != -1) {
            SpannableStringBuilder ssb = new SpannableStringBuilder(text);
            ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, lineBreakIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new RelativeSizeSpan(1.5f), 0, lineBreakIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            editTextContent.removeTextChangedListener(textWatcher);
            int cursorPosition = editTextContent.getSelectionStart();
            editTextContent.setText(ssb);
            editTextContent.addTextChangedListener(textWatcher);
            editTextContent.setSelection(cursorPosition);
        }
    }

}
