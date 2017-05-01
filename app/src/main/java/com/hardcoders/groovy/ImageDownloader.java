package com.hardcoders.groovy;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

    public static String GetFileName(String artist, String title) {
        String fileName = artist + " - " + title;
        char forbiddenCharacters[] = new char[]{'/', '\\', ':', '?', '*', '+', '%'};
        for (int i = 0; i < forbiddenCharacters.length; i++) {
            fileName = fileName.replace(forbiddenCharacters[i], ' ');
        }
        return fileName;
    }

    @Override
    protected void onPostExecute(byte[] bytes) {
        try {
            String newFileName = GetFileName(TextUtils.join(", ", track.Artists), track.Name);
            String directoryPath = audioFile.getParent();
            String newAudioFilePath = directoryPath + "/" + newFileName;
            Log.d("Image Download", newAudioFilePath);
            File newAudioFile = new File(newAudioFilePath);
            MusicMetadataSet src_set = new MyID3().read(audioFile);
            MusicMetadata metadata = (MusicMetadata) src_set.getSimplified();
            ImageData imageData = new ImageData(bytes, "", "", 3);
            metadata.clear();
            metadata.addPicture(imageData);
            metadata.setSongTitle(track.Name);
            metadata.setArtist(TextUtils.join(", ", track.Artists));
            metadata.setTrackNumber(track.TrackNumber);
            metadata.setAlbum(track.Album);
            metadata.setGenre(TextUtils.join(", ", track.Genres));
            metadata.setYear(String.valueOf(track.Year));

            File dst = new File(audioFile.getAbsolutePath());
            new MyID3().update(audioFile, src_set, metadata);
            Log.d("Image Download", "Done");
            Toast.makeText(context, "Music details changed", Toast.LENGTH_SHORT).show();


            //TODO audioFile.renameTo(newAudioFile);


            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(audioFile));
            context.sendBroadcast(intent);
        } catch (ID3WriteException | IOException e) {
            e.printStackTrace();
        }
    }
}