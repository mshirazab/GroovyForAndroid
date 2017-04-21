package com.hardcoders.groovy;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.drawable.DrawableWrapper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
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
        listView = (ListView) findViewById(R.id.local_listview);
        try {
            fileManager(home);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fileManager(final String directory) {
        final ArrayList<MusicPackage> musicPackages = getFiles(directory);
        for(MusicPackage musicPackage : musicPackages)  {
            Log.d("Debug",musicPackage.Name);
        }
        Log.d("Debug","hello");
        CustomAdapter cs = new CustomAdapter(this, musicPackages);
        listView.setAdapter(cs);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                stack.push(directory);
                fileManager(directory + "/" + musicPackages.get(position).Name);
                Log.d("Debug", directory + "/" + musicPackages.get(position).Name);
            }
        });
    }

    private ArrayList<MusicPackage> getFiles(String directory) {
        File f = new File(directory);
        ArrayList<MusicPackage> musicPackages = new ArrayList<>();
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            File ff = files[i];
            if (ff.isDirectory()) {
                MusicPackage musicPackage = new MusicPackage();
                musicPackage.Name = ff.getName();
                musicPackages.add(musicPackage);
            } else {
                String path = ff.getPath();
                String[] arr = path.split("\\.");
                if (arr.length > 0) {
                    String type = arr[arr.length - 1];
                    if (types.contains(type)) {
                        musicPackages.add(getMusicPackage(ff));
                    }
                }
            }
        }
        return musicPackages;
    }

    public MusicPackage getMusicPackage(File src) {
        MusicMetadataSet src_set = null;
        MusicPackage musicPackage = new MusicPackage();
        try {
            src_set = new MyID3().read(src);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            assert src_set != null;
            IMusicMetadata metadata = src_set.getSimplified();
            musicPackage.Artist = metadata.getArtist();
            musicPackage.Album = metadata.getAlbum();
            musicPackage.Name = metadata.getSongTitle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return musicPackage;
    }
}
