package com.hardcoders.groovy;


import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mashape.unirest.http.exceptions.UnirestException;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * This searches for the songs in the background and
 * sets the listView.
 */

class CustomTask extends AsyncTask<String, Void, ArrayList<Track>> {
    private final TextView textView;
    private CustomAdapter adapter;
    private ProgressBar progressBar;

    CustomTask(CustomAdapter adapter, ProgressBar progressBar, TextView textView) {
        this.adapter = adapter;
        this.progressBar = progressBar;
        this.textView = textView;
    }

    @Override
    protected ArrayList<Track> doInBackground(String... params) {
        try {
            if (isCancelled())
                return null;
            if(params[0]!= null && !Objects.equals(params[0], ""))
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
        if (adapter.getCount() == 0) {
            textView.setText(R.string.no_internet_message);
        }
    }
}

