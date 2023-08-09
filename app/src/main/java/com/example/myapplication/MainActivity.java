package com.example.myapplication;

import androidx.annotation.NonNull;
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
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.adapter.NoteAdapter;
import com.example.myapplication.auth.HomeActivity;
import com.example.myapplication.models.UserInfo;
import com.example.myapplication.dao.NoteFirebaseDAO;
import com.example.myapplication.decoration.SpacingItemDecoration;
import com.example.myapplication.models.Note;
import com.example.myapplication.models.NoteSingleton;
import com.example.myapplication.woker.ConstantsManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<Note> noteList = new ArrayList<>();
    List<Note> filteredNotes = new ArrayList<>();
    Button btnCreateNote;
    EditText editTextSearch;
    private GoogleSignInClient mGoogleSignInClient;

    NoteAdapter noteAdapter;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    Button btnWeather;
    private NoteFirebaseDAO noteFirebaseDAO = new NoteFirebaseDAO(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel"; // Tên kênh
            String description = "Channel description"; //
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel
                    channel =
                    new NotificationChannel(ConstantsManager.CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        TextView tvAccountSetting = findViewById(R.id.tvAccountSetting);
        tvAccountSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String currentEmail = currentUser.getEmail();
                db.collection("users")
                        .whereEqualTo("email", currentEmail)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                    DocumentSnapshot
                                            documentSnapshot =
                                            task.getResult().getDocuments().get(0);
                                    UserInfo userInfo = documentSnapshot.toObject(UserInfo.class);

                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("userInfo", userInfo);

                                    Intent intent =
                                            new Intent(MainActivity.this, AccountSetting.class);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(MainActivity.this,
                                            "Không tìm thấy thông tin người dùng",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        Button btnSignOut = findViewById(R.id.btnSignOut);

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                GoogleSignInOptions gso =
                        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(getString(R.string.default_web_client_id))
                                .requestEmail()
                                .build();
                mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);
                mGoogleSignInClient.signOut();

                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btnWeather = findViewById(R.id.btnWeather);
        btnWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
                startActivity(intent);
            }
        });
        noteAdapter = new NoteAdapter(filteredNotes, getApplicationContext(), noteFirebaseDAO);
        noteFirebaseDAO.listenNote(new NoteAdapter.OnDataChangeListener() {
            @Override
            public void onDataChanged(List<Note> notes) {
                noteList.clear();
                noteList.addAll(notes);
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    filterUserNotes("");
                }
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
                NoteSingleton.getInstance().setNoteUserId(currentUser.getUid());
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
                filterUserNotes(charSequence.toString());
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

    private void filterUserNotes(String searchText) {
        filteredNotes.clear();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            for (Note note : noteList) {
                if (note.getContent().toLowerCase().contains(searchText.toLowerCase())
                        && note.getUserId() != null &&
                        note.getUserId().equals(user.getUid())) {
                    filteredNotes.add(note);
                }
            }
        }
        noteAdapter.notifyDataSetChanged();
    }


}
