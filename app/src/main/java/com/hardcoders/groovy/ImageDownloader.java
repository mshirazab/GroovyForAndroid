package com.hardcoders.groovy;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
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

import static android.widget.Toast.LENGTH_SHORT;

/**
 * Created by shiraz on 1/5/17.
 */

public class ImageDownloader extends AsyncTask<String, Void, byte[]> {
    Track track;
    File audioFile;
    Context context;
    Snackbar snackbar;

    public ImageDownloader(Context context, Track track, File audioFile, Snackbar snackbar) {
        this.track = track;
        this.context = context;
        this.audioFile = audioFile;
        this.snackbar = snackbar;
    }

    @Override
    protected byte[] doInBackground(String... ignored) {
        DefaultHttpClient client = new DefaultHttpClient();
        try {
            HttpUriRequest request = new HttpGet(track.ImageURL);
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
            String[] fileArray = audioFile.getName().split("\\.");
            String newFileName = GetFileName(TextUtils.join(", ", track.Artists), track.Name) + "."
                    + fileArray[fileArray.length - 1];
            String directoryPath = audioFile.getParent();
            String newAudioFilePath = directoryPath + "/" + newFileName;
            Log.d("Image Download", newAudioFilePath);
            File newAudioFile = new File(newAudioFilePath);
            audioFile.renameTo(newAudioFile);
            MusicMetadataSet src_set = new MyID3().read(newAudioFile);
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
            new MyID3().update(newAudioFile, src_set, metadata);
            Log.d("Image Download", "Done");
            snackbar.setText("Song tags set");
            wait(2000);
            snackbar.dismiss();

            DeleteMP3FromMediaStore(context, audioFile.getAbsolutePath());
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(newAudioFile));
            context.sendBroadcast(intent);
        } catch (ID3WriteException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void DeleteMP3FromMediaStore(Context context, String path) {
        Uri rootUri = MediaStore.Audio.Media.getContentUriForPath(path);
        context.getContentResolver().delete(rootUri,
                MediaStore.MediaColumns.DATA + "=?", new String[]{path});
    }
}