package com.hardcoders.groovy;

/**
 * Created by nithin on 4/30/17.
 */

import com.google.gson.Gson;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.json.JSONException;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Future;

/**
 * Created by nithin on 4/15/17.
 */
class Track
{

    public String Name;
    public ArrayList<String> Artists;
    public int TrackNumber ;
    public String Album;
    public String AlbumArtist;
    public ArrayList<String> Genres  = new ArrayList<String>();
    public String ImageURL;
    public String ImageURLShort;
    public int Year;

    public Track(Item item)
    {
        Name = item.Name;
        for(ArtistC artist : item.Artists)
        {
            Artists.add(artist.Artist.Name);
        }
        TrackNumber = item.TrackNumber;
        Album = item.Album.Name;
        AlbumArtist = Artists.get(0);
        for(String genre : item.Genres)
        {
            Genres.add(genre);
        }
        ImageURL = item.ImageUrl;
        ImageURLShort = ImageURL + "&w=250&h=250";
        Year = Integer.decode(item.ReleaseDate.substring(0,4));
    }
}

class Album
{
    public String Id ;
    public String Name ;
    public String ImageUrl ;
    public String Link ;
    public String Source ;
    public String CompatibleSources ;
}

class ArtistC2
{
    public String Id ;
    public String Name ;
    public String ImageUrl ;
    public String Link ;
    public String Source ;
    public String CompatibleSources ;
}

class ArtistC
{
    public String Role ;
    public ArtistC2 Artist ;
}

class OtherIds
{
    public String isrc ;
}

class Item
{
    public String ReleaseDate ;
    public String Duration ;
    public int TrackNumber ;
    public boolean IsExplicit ;
    public ArrayList<String> Genres = new ArrayList<String>();
    public ArrayList<String> Subgenres = new ArrayList<String>() ;
    public ArrayList<String> Rights = new ArrayList<String>();
    public Album Album ;
    public ArrayList<ArtistC> Artists = new ArrayList<ArtistC>();
    public String Id ;
    public String Name ;
    public String ImageUrl ;
    public String Link ;
    public OtherIds OtherIds ;
    public String Source ;
    public String CompatibleSources ;
    public String Subtitle ;
}

class Tracks
{
    public ArrayList<Item> Items = new ArrayList<Item>();
    public String ContinuationToken ;
    public int TotalItemCount ;
}

class RootObject
{
    public Tracks Tracks ;
    public String Culture ;
}

public class Groovy {

    private static String EncodeQuery(String query)
    {
        query = query.trim();
        query = query.replace(' ', '+');
        return query;
    }

    public static RootObject Search(String query) throws UnirestException, TagException, CannotWriteException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException, IOException {

        String service = "https://login.live.com/accesstoken.srf";
        String clientId = "fcb2b041-fe82-4c06-8a30-d28a9ddc805d";
        String clientSecret = "uqBChgpbtw2ToiHiTUCk3fN";
        String scope = "app.music.xboxlive.com";
        String grantType = "client_credentials";

        HttpResponse<String> jsonResponse = Unirest.post(service)
                .header("accept", "application/json")
                .header("client_id", clientId)
                .header("client_secret", clientSecret)
                .header("scope", scope)
                .header("grant_type",grantType)
                .asString();

        String token = null;
        token = jsonResponse.getHeaders().get("access_token").toString();
        System.out.println(token);


        String url ="https://music.xboxlive.com/1/content/music/search?q=" + EncodeQuery(query) + "&filters=Tracks";
        String myString = Unirest.get(url)
                .header("accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .asString().getBody();

        System.out.println(myString);

        Gson gson = new Gson();
        RootObject rootObject = gson.fromJson(myString, RootObject.class);
        return rootObject;
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

