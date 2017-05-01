package com.hardcoders.groovy;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.util.ArrayList;

import static com.hardcoders.groovy.MainActivity.sArtworkUri;

public class LocalMusicActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<Track> tracks;
    private static final int MUSIC_ID = 968;
    CustomTask customTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_music);
        String s = getIntent().getStringExtra("SEARCHED_KEY");
        listView = (ListView) findViewById(R.id.local_listview);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.my_progressbar);
        tracks = new ArrayList<>();
        CustomAdapter adapter = new CustomAdapter(this, tracks);
        customTask = new CustomTask(adapter, progressBar);
        customTask.execute(s);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    @Override
    protected void onPause() {
        customTask.cancel(true);
        super.onPause();
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
        String[] projection = {MediaStore.Audio.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public long getAlbumID(Uri uri) {
        String[] projection = {MediaStore.Audio.Media.ALBUM_ID};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
        cursor.moveToFirst();
        return cursor.getLong(column_index);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MUSIC_ID && resultCode == RESULT_OK) {
            if ((data != null) && (data.getData() != null)) {
                Uri audioFileUri = data.getData();
                File audioFile = new File(getPath(audioFileUri));
                try {
                    MusicMetadataSet src_set = new MyID3().read(audioFile);
                    MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
                    //print artist,album,song name
                    String artist = metadata.getArtist();
                    String album = metadata.getAlbum();
                    String song_title = metadata.getSongTitle();
                    Log.d("Music Selected", artist + "\t" + album + "\t" + song_title + "\t");

                    //show album art
                    Uri uri = ContentUris.withAppendedId(sArtworkUri, getAlbumID(audioFileUri));
                    //Picasso.with(this).load(uri).into((ImageView) findViewById(R.id.my_image));


                    //TODO : set image source
                    /*File img;
                    ImageData imageData = new ImageData(readFile(img), "", "", 3);
                    metadata.addPicture(imageData);
                    metadata.setArtist("Bob Marley");

                    File dst = new File(audioFile.getAbsolutePath() + " (Edited)");
                    new MyID3().write(audioFile, dst, src_set, metadata);*/
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
