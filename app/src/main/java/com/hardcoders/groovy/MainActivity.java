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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.BufferedReader;
import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int MUSIC_ID = 14;
    EditText searchEdit;
    Button button;
    String selectedSong = "";
    private static final int permissionResult = 281;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the custom Actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        // Initialize the view on the activity
        searchEdit = (EditText) findViewById(R.id.main_edit);
        button = (Button) findViewById(R.id.main_button);
        buttonDisableIfEmpty(button, searchEdit, 100);

        Button button1 = (Button) findViewById(R.id.main_song_button);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });
    }

    private void buttonDisableIfEmpty(final Button button, EditText searchEdit, int size) {
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
        intent.putExtra("SEARCHED_KEY", searchEdit.getText().toString());
        intent.putExtra("SELECTED_SONG", selectedSong);
        startActivity(intent);
    }

    // Select a local song which can be used to search
    public void startPopup() {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_PICK);
        intent.setData(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, MUSIC_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MUSIC_ID && resultCode == RESULT_OK) {
            if ((data != null) && (data.getData() != null)) {
                Uri audioFileUri = data.getData();
                File audioFile = new File(getPath(audioFileUri));
                selectedSong = audioFile.getAbsolutePath();
                try {
                    MusicMetadataSet src_set = new MyID3().read(audioFile);
                    MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
                    String song_title = metadata.getSongTitle();
                    searchEdit.setText(song_title);
                } catch (Exception e) {
                    Toast.makeText(this, "An error occured", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Audio.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    void requestPermission() {

        int writePermissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (writePermissionCheck == PackageManager.PERMISSION_DENIED ||
                readPermissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, permissionResult);
        } else {
            startPopup();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case permissionResult: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startPopup();
                } else {
                    Toast.makeText(this, "Allow Permission", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

}
