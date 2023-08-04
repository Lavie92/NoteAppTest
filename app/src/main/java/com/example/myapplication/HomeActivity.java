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

import com.example.myapplication.adapter.NoteAdapter;
import com.example.myapplication.dao.NoteFirebaseDAO;
import com.example.myapplication.models.Note;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    Button btnBack, btnSave;
    EditText editTextContent;
    TextWatcher textWatcher;
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
        NoteAdapter noteAdapter = new NoteAdapter(noteList, getApplicationContext(), noteFirebaseDAO);
        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String idNote = String.valueOf(i++);
                String content = editTextContent.getText().toString();
                Note note = new Note(idNote, content);
                noteFirebaseDAO.Insert(note);
                finish();
            }
        });
        // Initialize the TextWatcher
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // This method is called before the text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // This method is called as the text changes
            }

            @Override
            public void afterTextChanged(Editable s) {
                // This method is called after the text changes
                handleTextChange(s);
            }
        };

        // Attach the TextWatcher to the EditText
        editTextContent.addTextChangedListener(textWatcher);
        // Set onClickListener for the back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void handleTextChange(Editable editable) {
        String text = editable.toString();
        int lineBreakIndex = text.indexOf("\n");

        if (lineBreakIndex != -1) {
            SpannableStringBuilder ssb = new SpannableStringBuilder(text);

            // Apply formatting to the first line (title)
            ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, lineBreakIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssb.setSpan(new RelativeSizeSpan(1.5f), 0, lineBreakIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Temporarily remove the TextWatcher to avoid the loop
            editTextContent.removeTextChangedListener(textWatcher);

            // Get the current cursor position
            int cursorPosition = editTextContent.getSelectionStart();

            // Set the formatted text back to the EditText
            editTextContent.setText(ssb);

            // Reattach the TextWatcher
            editTextContent.addTextChangedListener(textWatcher);

            // Restore cursor position
            editTextContent.setSelection(cursorPosition);
        }
    }

}
