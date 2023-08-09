package com.example.myapplication;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.myapplication.auth.HomeActivity;
import com.example.myapplication.dao.NoteFirebaseDAO;
import com.example.myapplication.models.Note;
import com.example.myapplication.models.NoteSingleton;
import com.example.myapplication.woker.AlarmWorker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class NoteDetailActivity extends AppCompatActivity {
    Button btnBack;
    EditText editTextContent;
    TextWatcher textWatcher;
    Button btnAlarm;
    private String currentUserId;
    final Note newNote = NoteSingleton.getInstance().getNote();
    NoteFirebaseDAO noteFirebaseDAO;
    private FirebaseAuth mAuth;
    int i = 0;
    public int CHON_ANH = 0;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(NoteDetailActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            currentUserId = currentUser.getUid();
        }
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
                newNote.setUserId(currentUserId);
                noteFirebaseDAO.Update(newNote);
                if (intent.hasExtra("note")) {
                    Note note = (Note) intent.getSerializableExtra("note");
                    note.setDateTime(date);
                    note.setContent(s.toString());
                    note.setUserId(currentUserId);
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
        btnAlarm = findViewById(R.id.btnAlarm);
        btnAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog(view);
            }
        });
        Button btnSave = findViewById(R.id.btnSave);

        imageView = findViewById(R.id.imageView);
        findViewById(R.id.btnTakePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence[] optionsMenu = {"Chụp ảnh", "Chọn ảnh", "Thoát"};
                AlertDialog.Builder builder = new AlertDialog.Builder(NoteDetailActivity.this);
                builder.setItems(optionsMenu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (optionsMenu[i].equals("Chụp ảnh")) {
                            // Mở camera để chụp ảnh
                            CHON_ANH = 1;
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            getData.launch(takePicture);
                        } else if (optionsMenu[i].equals("Chọn ảnh")) {
                            // Chọn ảnh từ bộ sưu tập
                            CHON_ANH = 2;
                            Intent pickPhoto = new Intent(Intent.ACTION_GET_CONTENT);
                            pickPhoto.setType("image/*");
                            getData.launch(pickPhoto);
                        } else if (optionsMenu[i].equals("Thoát")) {
                            dialogInterface.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });
    }
    ActivityResultLauncher<Intent> getData = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Bitmap selectedImage = null;
                    if (CHON_ANH == 1) {
                        // Chụp ảnh từ camera
                        selectedImage = (Bitmap) data.getExtras().get("data");
                    } else if (CHON_ANH == 2) {
                        // Chọn ảnh từ bộ sưu tập
                        Uri selectedImageUri = data.getData();
                        try {
                            selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    imageView.setImageBitmap(selectedImage);
                }
            });
    public void showTimePickerDialog(View view) {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar alarmCalendar = Calendar.getInstance();
                        alarmCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        alarmCalendar.set(Calendar.MINUTE, minute);
                        long alarmTime = alarmCalendar.getTimeInMillis();

                        long currentTimeMillis = System.currentTimeMillis();
                        if (alarmTime <= currentTimeMillis) {
                            Toast.makeText(NoteDetailActivity.this, "Thời gian báo thức đã qua",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Note note = getNoteToBeDisplayed();
                        setAlarm(alarmTime, note);
                    }
                },
                hour,
                minute,
                true
        );
        timePickerDialog.show();
    }

    private void setAlarm(long alarmTime, Note note) {
        Data inputData = new Data.Builder()
                .putLong("alarm_time", alarmTime)
                .putString("note_id", note.getId())
                .putString("note_content", note.getContent())
                .build();

        OneTimeWorkRequest workRequest =
                new OneTimeWorkRequest.Builder(AlarmWorker.class)
                        .setInputData(inputData)
                        .build();

        WorkManager.getInstance(getApplicationContext()).enqueue(workRequest);
        note.setTimeAlarm(alarmTime);
        note.setUserId(currentUserId);
        noteFirebaseDAO.Update(note);
        Toast.makeText(NoteDetailActivity.this, "Đã đặt báo thức", Toast.LENGTH_SHORT)
                .show();
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
        } else if (content.isEmpty() && newNote != null) {
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

    @SuppressLint("SuspiciousIndentation")
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

    public Note getNoteToBeDisplayed() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Note note = (Note) bundle.getSerializable("note");
            return note;
        }
        return newNote;
    }
}