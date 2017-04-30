package com.hardcoders.groovy;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.drawable.DrawableWrapper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.IMusicMetadata;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class LocalMusicActivity extends AppCompatActivity {

    final String home = Environment.getExternalStorageDirectory().toString();
    final ArrayList<String> types = new ArrayList<>(Arrays.asList(
            new String[]{"mp3"}));
    ListView listView;
    Stack<String> stack = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_music);
        String s = getIntent().getStringExtra("SEARCHED_KEY");
        listView = (ListView) findViewById(R.id.local_listview);
        ArrayList<Track> tracks = new ArrayList<>();
        CustomAdapter adapter = new CustomAdapter(this, tracks);
        CustomTask customTask = new CustomTask(adapter);
        customTask.execute(s);
        listView.setAdapter(adapter);
    }
}
