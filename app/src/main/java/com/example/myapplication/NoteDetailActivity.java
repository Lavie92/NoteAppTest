
package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
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

import com.bumptech.glide.Glide;
import com.example.myapplication.auth.HomeActivity;
import com.example.myapplication.dao.NoteFirebaseDAO;
import com.example.myapplication.models.Note;
import com.example.myapplication.models.NoteSingleton;
import com.example.myapplication.woker.AlarmWorker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NoteDetailActivity extends AppCompatActivity {
    Button btnBack;
    EditText editTextContent;
    TextWatcher textWatcher;
    Button btnAlarm;
    FirebaseStorage storage;
    private String currentUserId;
    private Uri cameraImageUri;

    final Note newNote = NoteSingleton.getInstance().getNote();
    NoteFirebaseDAO noteFirebaseDAO;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_Pick_IMAGE = 2;

    private ImageView imageView;
    private Bitmap imageBitmap;
    protected static final int RESULT_SPEECH = 1;

    private FirebaseAuth mAuth;
    int i = 0;
    public int CHON_ANH = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);
        mAuth = FirebaseAuth.getInstance();
        newNote.setImageUrl(null);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(NoteDetailActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        } else {
            currentUserId = currentUser.getUid();
        }
        storage = FirebaseStorage.getInstance();

        Note note = getNoteToBeDisplayed();
        editTextContent = findViewById(R.id.editTextContent);
        btnBack = findViewById(R.id.btnBackToHome);
        noteFirebaseDAO = new NoteFirebaseDAO(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            note = (Note) bundle.getSerializable("note");
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
        Button btnSpeak = findViewById(R.id.btnSpeak);
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi");
                try {
                    startActivityForResult(intent, RESULT_SPEECH);
                    editTextContent.setText("");
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Your device doesn't support Speech to Text", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
        btnAlarm = findViewById(R.id.btnAlarm);
        btnAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog(view);
            }
        });
        findViewById(R.id.btnTakePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickerOptions();
            }
        });
        imageView = findViewById(R.id.imageView);
        if (note.getImageUrl() != null) {
            Glide.with(this).load(note.getImageUrl()).placeholder(R.drawable.logo).dontAnimate()
                    .into(imageView);
        }
    }

    private void showImagePickerOptions() {

        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                takeImageFromCamera();
            } else {
                pickImageFromGallery();
            }
        });

        builder.show();
    }

    private void pickImageFromGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_Pick_IMAGE);
    }

    private void takeImageFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }

            if (photoFile != null) {
                cameraImageUri = FileProvider.getUriForFile(this,
                        "com.example.myapplication.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case RESULT_SPEECH:
                if(resultCode == RESULT_OK && data != null){
                    ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    editTextContent.setText(text.get(0));
                }
                break;
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (cameraImageUri != null) {
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), cameraImageUri);
                    imageView.setImageBitmap(imageBitmap);
                    uploadImageToFirebase(imageBitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (requestCode == REQUEST_Pick_IMAGE && resultCode == RESULT_OK) {
            Uri pickedImage = data.getData();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), pickedImage);
                imageView.setImageBitmap(imageBitmap);
                uploadImageToFirebase(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void uploadImageToFirebase(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imagesRef = storageRef.child("images");
        String filename = "image_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = imagesRef.child(filename);

        UploadTask uploadTask = imageRef.putBytes(data);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                Note note = getNoteToBeDisplayed();
                note.setImageUrl(imageUrl);
                noteFirebaseDAO.Update(note);
            }).addOnFailureListener(exception -> {
            });
        }).addOnFailureListener(exception -> {
        });
    }



    public void showTimePickerDialog(View view) {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog =
                new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
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
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void setAlarm(long alarmTime, Note note) {
        Data inputData = new Data.Builder().putLong("alarm_time", alarmTime)
                .putString("note_id", note.getId()).putString("note_content", note.getContent())
                .build();

        OneTimeWorkRequest workRequest =
                new OneTimeWorkRequest.Builder(AlarmWorker.class).setInputData(inputData).build();

        WorkManager.getInstance(getApplicationContext()).enqueue(workRequest);
        note.setTimeAlarm(alarmTime);
        note.setUserId(currentUserId);
        noteFirebaseDAO.Update(note);
        Toast.makeText(NoteDetailActivity.this, "Đã đặt báo thức", Toast.LENGTH_SHORT).show();
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