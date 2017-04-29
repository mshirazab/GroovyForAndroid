package com.hardcoders.groovy;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Debug;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ExpandedMenuView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import org.cmc.music.metadata.IMusicMetadata;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int MUSIC_ID = 14;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
    }

    public void startPopup(View view) {
        /*Intent intent = new Intent(this, LocalMusicActivity.class);
        startActivity(intent);*/
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_PICK);
        intent.setData(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, MUSIC_ID);
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Audio.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MUSIC_ID && resultCode == RESULT_OK) {
            if ((data != null) && (data.getData() != null)) {
                Uri audioFileUri = data.getData();
                File audioFile = new File(getPath(audioFileUri));
                try {
                    MusicMetadataSet src_set = new MyID3().read(audioFile);
                    IMusicMetadata metadata = src_set.getSimplified();
                    String artist = metadata.getArtist();
                    String album = metadata.getAlbum();
                    String song_title = metadata.getSongTitle();
                    Number track_number = metadata.getTrackNumber();
                    Log.d("Music Selected", artist + "\t" + album + "\t" + song_title + "\t" + track_number);
                } catch (Exception e) {
                    e.printStackTrace();
                }
/*
                metadata.setArtist("Bob Marley");

                File dst = ...
                new MyID3().write(src, dst, src_set, metadata);*/
            }
        }
    }
}
