package com.hardcoders.groovy;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.mashape.unirest.http.exceptions.UnirestException;

import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int MUSIC_ID = 14;
    final public static Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    EditText searchEdit;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        searchEdit = (EditText) findViewById(R.id.main_edit);
        button = (Button) findViewById(R.id.main_button);
        String searchedText = searchEdit.getText().toString();
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
        searchEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});

    }

    public void startNewActivity(View view) {
        if (button.isEnabled()) {
            try {
                ArrayList<Track> tracks = Groovy.Search(searchEdit.getText().toString());
                searchEdit.setText(tracks.get(0).Album);
            } catch (UnirestException | TagException |
                    CannotWriteException | ReadOnlyFileException |
                    CannotReadException | IOException |
                    InvalidAudioFrameException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(this, LocalMusicActivity.class);
            intent.putExtra("SEARCHED_KEY", searchEdit.getText().toString());
            startActivity(intent);
        } else
            Toast.makeText(MainActivity.this, "Please input a name", Toast.LENGTH_SHORT).show();
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

    public static byte[] readFile(File file) throws IOException {
        // Open file

        try (RandomAccessFile f = new RandomAccessFile(file, "r")) {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength) throw new IOException("File size >= 2 GB");

            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        }
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
