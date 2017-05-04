package com.hardcoders.groovy;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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

import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class LocalMusicActivity extends AppCompatActivity {
    ListView listView;
    private static final int MUSIC_ID = 968;
    CustomTask customTask;
    Track selectedTrack = null;
    private static final int permissionResult = 281;
    ProgressBar progressBar;
    String filePath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        String s = getIntent().getStringExtra(MainActivity.selected_song_name);
        String filePath = getIntent().getStringExtra(MainActivity.selected_song_location);
        if (filePath != null)
            this.filePath = filePath;
        else {
            finish();
        }
        listView = (ListView) findViewById(R.id.local_listview);
        progressBar = (ProgressBar) findViewById(R.id.my_progressbar);
        TextView textView = (TextView) findViewById(R.id.no_results_text_view);

        final ArrayList<Track> tracks = new ArrayList<>();
        CustomAdapter adapter = new CustomAdapter(this, tracks);
        customTask = new CustomTask(adapter, progressBar, textView);
        customTask.execute(s);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedTrack = tracks.get(position);
                checkPermission();
            }
        });
    }

    void checkPermission() {

        int writePermissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (writePermissionCheck == PackageManager.PERMISSION_DENIED ||
                readPermissionCheck == PackageManager.PERMISSION_DENIED) {
            finish();
        } else
            changeSong(new File(filePath));
    }

    @Override
    protected void onPause() {
        customTask.cancel(true);
        super.onPause();
    }

    void changeSong(File audioFile) {
        Log.d("changeFile", audioFile.getAbsolutePath());
        ProgressDialog progressDialog = ProgressDialog.show(this,null,"Downloading albumart for song");
        progressDialog.setProgressStyle(R.style.DarkTheme);
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        try {
            MusicMetadataSet src_set = new MyID3().read(audioFile);
            MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
            //print artist,album,song name
            String artist = metadata.getArtist();
            String album = metadata.getAlbum();
            String song_title = metadata.getSongTitle();
            Log.d("Music Selected", artist + "\t" + album + "\t" + song_title + "\t");
            // Here we download the songs albumart and set all the tags
            ImageDownloader imageDownloader = new ImageDownloader(this, selectedTrack,
                    audioFile, progressDialog);
            imageDownloader.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
