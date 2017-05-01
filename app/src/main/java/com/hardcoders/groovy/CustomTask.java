package com.hardcoders.groovy;


import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.mashape.unirest.http.exceptions.UnirestException;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This searches for the songs in the background and
 * sets the listView.
 */

class CustomTask extends AsyncTask<String, Void, ArrayList<Track>> {
    private CustomAdapter adapter;
    private ProgressBar progressBar;

    CustomTask(CustomAdapter adapter, ProgressBar progressBar) {
        this.adapter = adapter;
        this.progressBar = progressBar;
    }

    @Override
    protected ArrayList<Track> doInBackground(String... params) {
        try {
            if (isCancelled())
                return null;
            return Groovy.Search(params[0]);
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
        progressBar.setVisibility(View.GONE);
        if (s != null)
            adapter.addAll(s);
    }
}

