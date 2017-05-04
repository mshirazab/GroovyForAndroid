package com.hardcoders.groovy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.util.Objects;


public class SongSearchActivity extends AppCompatActivity {

    EditText searchEdit;
    Button button;
    String selectedSong = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_search);
        Intent intent = getIntent();
        selectedSong = intent.getExtras().getString(MainActivity.selected_song_location);
        Log.d("SongSearch", selectedSong);
        if (selectedSong == null)
            finish();
        // Set the custom Actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        // Initialize the view on the activity
        searchEdit = (EditText) findViewById(R.id.main_edit);
        searchEdit.setText(
                intent.getExtras().getString(MainActivity.selected_song_name));
        button = (Button) findViewById(R.id.main_button);
        buttonDisableIfEmpty(button, searchEdit, 100);
    }

    private void buttonDisableIfEmpty(final Button button, EditText searchEdit, int size) {
        if (Objects.equals(
                searchEdit.getText().toString(), ""))
            button.setEnabled(false);
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    button.setEnabled(true);
                } else {
                    button.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        searchEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(size)});
    }

    //show list of songs which has the name which you searched for
    public void startNewActivity(View view) {
        Intent intent = new Intent(this, LocalMusicActivity.class);
        intent.putExtra(MainActivity.selected_song_name, searchEdit.getText().toString());
        Log.d("SongSearchActivity", selectedSong);
        intent.putExtra(MainActivity.selected_song_location, selectedSong);
        startActivity(intent);
        finish();
    }
}
