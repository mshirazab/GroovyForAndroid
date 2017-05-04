package com.hardcoders.groovy;

/**
 * Created by nithin on 4/30/17.
 */

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.w3c.dom.NameList;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nithin on 4/15/17.
 */
class Track {

    String Name;
    ArrayList<String> Artists = new ArrayList<>();
    int TrackNumber;
    String Album;
    //String AlbumArtist;
    ArrayList<String> Genres = new ArrayList<String>();
    String ImageURL;
    String ImageURLShort;
    Uri ImageURI = null;
    int Year;
    String Location;

    Track(Item item) {
        Name = item.Name;
        for (int i = 0; i < item.Artists.size(); i++) {
            Artists.add(item.Artists.get(i).Artist.Name);
        }
        TrackNumber = item.TrackNumber;
        Album = item.Album.Name;
        //AlbumArtist = Artists.get(0);
        for (int i = 0; i < item.Genres.size(); i++) {
            Genres.add(item.Genres.get(i));
        }
        ImageURL = item.ImageUrl;
        ImageURLShort = ImageURL + "&w=150&h=150";
        ImageURL += "&w=1000&h=1000";
        Year = Integer.decode(item.ReleaseDate.substring(0, 4));
    }

    Track(String Name, String Artist, String Album, String Location, Uri ImageURI) {
        this.Name = Name;
        this.Artists.add(Artist);
        this.Album = Album;
        this.Location = Location;
        this.ImageURI = ImageURI;
    }
}

class Album {
    String Id;
    String Name;
    String ImageUrl;
    String Link;
    String Source;
    String CompatibleSources;
}

class ArtistC2 {
    String Id;
    String Name;
    String ImageUrl;
    String Link;
    String Source;
    String CompatibleSources;
}

class ArtistC {
    String Role;
    ArtistC2 Artist;
}

class OtherIds {
    String isrc;
}

class Item {
    String ReleaseDate;
    String Duration;
    int TrackNumber;
    boolean IsExplicit;
    ArrayList<String> Genres = new ArrayList<String>();
    ArrayList<String> Subgenres = new ArrayList<String>();
    ArrayList<String> Rights = new ArrayList<String>();
    Album Album;
    ArrayList<ArtistC> Artists = new ArrayList<ArtistC>();
    String Id;
    String Name;
    String ImageUrl;
    String Link;
    OtherIds OtherIds;
    String Source;
    String CompatibleSources;
    String Subtitle;
}

class Tracks {
    ArrayList<Item> Items = new ArrayList<Item>();
    String ContinuationToken;
    int TotalItemCount;
}

class RootObject {
    Tracks Tracks;
    String Culture;
}

class AccessTokenObject {
    String token_type;
    String access_token;
    int expires_in;
}

class Groovy {

    private static String EncodeQuery(String query) {
        query = query.trim();
        query = query.replace(' ', '+');
        return query;
    }

    static ArrayList<Track> Search(String query) throws UnirestException, TagException, CannotWriteException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException, IOException {

        String service = "https://login.live.com/accesstoken.srf";
        String clientId = "fcb2b041-fe82-4c06-8a30-d28a9ddc805d";
        String clientSecret = "uqBChgpbtw2ToiHiTUCk3fN";
        String scope = "app.music.xboxlive.com";
        String grantType = "client_credentials";

        HttpResponse<String> jsonResponse = Unirest.post(service)
                .header("accept", "application/json")
                .field("client_id", clientId)
                .field("client_secret", clientSecret)
                .field("scope", scope)
                .field("grant_type", grantType)
                .asString();

        Gson gson = new Gson();
        AccessTokenObject accessTokenObject = gson.fromJson(jsonResponse.getBody(), AccessTokenObject.class);
        String token = accessTokenObject.access_token;


        String url = "https://music.xboxlive.com/1/content/music/search?q=" + EncodeQuery(query) + "&filters=Tracks";
        String myString = Unirest.get(url)
                .header("accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .asString().getBody();


        RootObject rootObject = gson.fromJson(myString, RootObject.class);
        ArrayList<Track> tracks = new ArrayList<>();
        for (Item item : rootObject.Tracks.Items) {
            Track track = new Track(item);
            tracks.add(track);
        }
        return tracks;
        /*
        System.out.println(rootObject.Tracks.Items.get(0).Album.Name);

        File file = new File("/home/yeetesh/Music/1.mp3");
        AudioFile f = AudioFileIO.read(file);
        Tag tag = f.getTag();
        tag.setField(FieldKey.ARTIST,rootObject.Tracks.Items.get(0).Artists.get(0).Artist.Name);
        tag.setField(FieldKey.ARTIST,rootObject.Tracks.Items.get(0).Artists.get(0).Artist.Name);
        f.commit();
        */
    }
}

