package com.hardcoders.groovy;

import android.content.Context;
import android.content.Intent;
import android.media.MediaActionSound;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.picasso.Picasso;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.cmc.music.common.ID3ReadException;
import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

/**
 * Created by shiraz on 1/5/17.
 */

public class ImageDownloader extends AsyncTask<String, Void, byte[]> {
    Track track;
    File audioFile;
    Context context;

    public ImageDownloader(Context context, Track track, File audioFile) {
        this.track = track;
        this.context = context;
        this.audioFile = audioFile;
    }

    @Override
    protected byte[] doInBackground(String... ignored) {
        DefaultHttpClient client = new DefaultHttpClient();
        try {
            HttpUriRequest request = new HttpGet(track.ImageURLShort);
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            int imageLength = (int) (entity.getContentLength());
            InputStream is = entity.getContent();

            byte[] imageBlob = new byte[imageLength];
            int bytesRead = 0;
            while (bytesRead < imageLength) {
                int n = is.read(imageBlob, bytesRead, imageLength - bytesRead);
                if (n <= 0)
                    return null;
                bytesRead += n;
            }
            return imageBlob;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(byte[] bytes) {
        try {
            MusicMetadataSet src_set = new MyID3().read(audioFile);
            MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
            ImageData imageData = new ImageData(bytes, "", "", 3);
            metadata.addPicture(imageData);
            metadata.setAlbum(track.Album);
            metadata.setArtist(TextUtils.join(", ", track.Artists));
            metadata.setSongTitle(track.Name);
            metadata.setGenre(track.Genres.get(0));
            File dst = new File(audioFile.getAbsolutePath());
            new MyID3().update(audioFile, src_set, metadata);
            Log.d("Image Download", "Done");
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(audioFile));
            context.sendBroadcast(intent);
        } catch (ID3WriteException | IOException e) {
            e.printStackTrace();
        }
    }
}