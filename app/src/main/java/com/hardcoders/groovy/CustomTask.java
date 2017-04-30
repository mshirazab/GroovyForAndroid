package com.hardcoders.groovy;

/**
 * Created by shiraz on 30/4/17.
 */

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.mashape.unirest.http.exceptions.UnirestException;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

class CustomTask extends AsyncTask<String, Void, ArrayList<Track>> {

    private static final String TAG = "CustomTask";
    EditText editText;

    public CustomTask(EditText editText) {
        this.editText = editText;
    }

    @Override
    protected ArrayList<Track> doInBackground(String... params) {
        try {
            ArrayList<Track> tracks = Groovy.Search(params[0]);
            return tracks;
        } catch (UnirestException | TagException |
                CannotWriteException | ReadOnlyFileException |
                CannotReadException | IOException |
                InvalidAudioFrameException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Track> s) {
        editText.setText(s.get(0).Album);
    }
}

