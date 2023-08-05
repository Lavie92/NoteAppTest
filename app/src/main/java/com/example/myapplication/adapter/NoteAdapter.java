package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.HomeActivity;
import com.example.myapplication.R;
import com.example.myapplication.dao.NoteFirebaseDAO;
import com.example.myapplication.models.Note;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder>  {
    List<Note> noteList;
    List<Boolean> isCheckBoxVisibleList;
    private SparseBooleanArray checkedStates;

    Context context;
    int color;
    NoteFirebaseDAO noteFirebaseDAO;
    private OnItemClickListener itemClickListener;

    public interface OnDataChangeListener {
        void onDataChanged(List<Note> notes);
    }
    public interface OnItemClickListener {
        void onItemClick(Note note);
    }
    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
    OnItemClickListener listener;
    public NoteAdapter(List<Note> noteList, Context context, NoteFirebaseDAO noteFirebaseDAO) {
        this.noteList = noteList;
        this.context = context;
        this.noteFirebaseDAO = noteFirebaseDAO;
        this.color = isDarkMode() ? getDarkColor() : getLightColor();
        checkedStates = new SparseBooleanArray();
    }

    private boolean isDarkMode() {
        int currentNightMode =
                context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                return false;
            case Configuration.UI_MODE_NIGHT_YES:
                return true;
            default:
                // default system mode
                return AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        }
    }

    private int getDarkColor() {
        return ContextCompat.getColor(context, R.color.item_bg_dark);
    }

    private int getLightColor() {
        return ContextCompat.getColor(context, R.color.item_bg_light);
    }

    @NonNull
    @Override
    public NoteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View noteView = inflater.inflate(R.layout.item_note, parent, false);
        return new ViewHolder(noteView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteAdapter.ViewHolder holder, int position) {
        Note note = noteList.get(position);
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.circle);
        drawable.setTint(color);
        holder.itemView.setBackground(drawable);
        String title = note.getContent();
        if (title != null) {
            int newlineIndex = title.indexOf("\n");
            if (newlineIndex != -1) {
                title = title.substring(0, newlineIndex); //
            }
            if (title.length() > 20) {
                title = title.substring(0, 20) + "...";
            }
        }
        holder.title.setText(title);

        Date date = note.getDateTime();
        if(date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String dateString = sdf.format(date);
            holder.time.setText(dateString);

        }
        int pos = holder.getAdapterPosition();
        if (checkedStates.indexOfKey(pos) < 0) {
            checkedStates.put(pos, false);
        }
        boolean checked = checkedStates.get(pos);
        if(checked) {
            holder.checkbox.setVisibility(View.VISIBLE);
            holder.viewCircle.setVisibility(View.GONE);
        } else {
            holder.checkbox.setVisibility(View.GONE);
            holder.viewCircle.setVisibility(View.VISIBLE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(note);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public void listenNoteFirestore(NoteFirebaseDAO noteFirebaseDAO,
            RecyclerView recyclerView) {
        noteFirebaseDAO.listenNote(new NoteAdapter.OnDataChangeListener() {
            @Override
            public void onDataChanged(List<Note> notes) {
                noteList.clear();
                noteList.addAll(notes);
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView time;
        CheckBox checkbox;
        View viewCircle;
        boolean isChecked;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvTitle);
            time = itemView.findViewById(R.id.tvTime);
            checkbox = itemView.findViewById(R.id.chkSelect);
            viewCircle = itemView.findViewById(R.id.viewCircle);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int pos = getAdapterPosition();

                    for(int i = 0; i < checkedStates.size(); i++) {
                        checkedStates.put(i, true);
                    }
                    notifyDataSetChanged();
                    return true;
                }
            });
        }

    }
}
