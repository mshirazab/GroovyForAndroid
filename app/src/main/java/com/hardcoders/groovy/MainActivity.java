package com.hardcoders.groovy;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    ListView listView;
    private static final int permissionResult = 22;
    public static final String selected_song_name = "SELECTED_SONG_NAME";
    public static final String selected_song_location = "SELECTED_SONG_LOCATION";
    Track selectedTrack;
    TextView textView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        listView = (ListView) findViewById(R.id.local_listview);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.my_progressbar);
        progressBar.setVisibility(View.GONE);
        textView = (TextView) findViewById(R.id.no_results_text_view);
        requestPermission();
    }

    private ArrayList<Track> getAllSongs() {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
        };

        Cursor cursor = this.managedQuery(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                null);

        ArrayList<Track> songs = new ArrayList<>();
        while (cursor.moveToNext()) {
            Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, cursor.getLong(4));
            Track track = new Track(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    albumArtUri);
            songs.add(track);
        }
        Collections.sort(songs, new Comparator<Track>() {
            @Override
            public int compare(Track o1, Track o2) {
                return o1.Name.compareTo(o2.Name);
            }
        });
        return songs;
    }

    private void populateListview() {
        final ArrayList<Track> tracks = getAllSongs();
        if (tracks.size() == 0)
            textView.setText("No songs found");
        else {
            CustomAdapter adapter = new CustomAdapter(this, tracks);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    selectedTrack = tracks.get(position);
                    Intent intent = new Intent(getApplicationContext(), SongSearchActivity.class);
                    Log.d("MAinACtivity", selectedTrack.Location);
                    intent.putExtra(selected_song_location, selectedTrack.Location);
                    intent.putExtra(selected_song_name, selectedTrack.Name);
                    startActivity(intent);
                }
            });
        }
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
            populateListview();
        }
    }

    // If permissions are granted then show
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case permissionResult: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    populateListview();
                } else {
                    textView.setText("Allow permission to show songs to select");
                }
            }
        }

    }
}
