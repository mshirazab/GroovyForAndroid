package com.hardcoders.groovy;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

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

class ImageDownloader extends AsyncTask<String, Void, byte[]> {
    private ListView listView;
    private Track track;
    private File audioFile;
    private Context context;

    ImageDownloader(Context context, Track track, File audioFile, ListView listView) {
        this.track = track;
        this.context = context;
        this.audioFile = audioFile;
        this.listView = listView;
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

    @Override
    protected void onPostExecute(byte[] bytes) {
        try {

            // Sending message that the song has been downloaded
            Snackbar snackbar = Snackbar.make(listView, "Download done", Snackbar.LENGTH_LONG);
            View view = snackbar.getView();
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            snackbar.show();

            // Changing filename to "<artists> - <song-name>.mp3"
            String[] fileArray = audioFile.getName().split("\\.");
            String newFileName = GetFileName(TextUtils.join(", ", track.Artists), track.Name) + "."
                    + fileArray[fileArray.length - 1];
            String directoryPath = audioFile.getParent();
            String newAudioFilePath = directoryPath + "/" + newFileName;
            File newAudioFile = new File(newAudioFilePath);
            audioFile.renameTo(newAudioFile);

            // Changing the tags of the song
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

            // Removing the old file and adding the new file
            DeleteFromMediaStore(context, audioFile);
            AddToMediaStore(context, newAudioFile);
        } catch (ID3WriteException | IOException e) {
            e.printStackTrace();
        }
    }

    private void DeleteFromMediaStore(Context context, File file) {
        String path = file.getAbsolutePath();
        Uri rootUri = MediaStore.Audio.Media.getContentUriForPath(path);
        context.getContentResolver().delete(rootUri,
                MediaStore.MediaColumns.DATA + "=?", new String[]{path});
    }

    private void AddToMediaStore(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        context.sendBroadcast(intent);
    }

    private static String GetFileName(String artist, String title) {
        String fileName = artist + " - " + title;
        char forbiddenCharacters[] = new char[]{'/', '\\', ':', '?', '*', '+', '%'};
        for (char forbiddenCharacter : forbiddenCharacters) {
            fileName = fileName.replace(forbiddenCharacter, ' ');
        }
        return fileName;
    }
}