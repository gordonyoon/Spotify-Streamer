package com.example.gordonyoon.spotifystreamer.spotify;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.gordonyoon.spotifystreamer.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

public class ArtistInfo implements Parcelable {

    private String mName;
    private String mSpotifyId;
    private String mImageUrl;

    public ArtistInfo(Artist artist) {
        mName = artist.name;
        mSpotifyId = artist.id;
        Image image = ImageUtils.selectClosestImage(artist.images, ImageUtils.SMALL_IMAGE_SIZE_TARGET);
        mImageUrl = (image != null) ? image.url : null;
    }

    public String getName() {
        return mName;
    }

    public String getSpotifyId() {
        return mSpotifyId;
    }

    public String getUrl() {
        return mImageUrl;
    }

    public static List<ArtistInfo> getArtistInfoList(List<Artist> artists) {
        List<ArtistInfo> artistInfos = new ArrayList<>();
        for (Artist artist : artists) {
            artistInfos.add(new ArtistInfo(artist));
        }
        return artistInfos;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mName);
        dest.writeString(this.mSpotifyId);
        dest.writeString(this.mImageUrl);
    }

    private ArtistInfo(Parcel in) {
        this.mName = in.readString();
        this.mSpotifyId = in.readString();
        this.mImageUrl = in.readString();
    }

    public static final Parcelable.Creator<ArtistInfo> CREATOR = new Parcelable.Creator<ArtistInfo>() {
        public ArtistInfo createFromParcel(Parcel source) {
            return new ArtistInfo(source);
        }

        public ArtistInfo[] newArray(int size) {
            return new ArtistInfo[size];
        }
    };
}
