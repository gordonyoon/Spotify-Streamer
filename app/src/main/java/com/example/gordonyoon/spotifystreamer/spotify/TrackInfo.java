package com.example.gordonyoon.spotifystreamer.spotify;


import android.os.Parcel;
import android.os.Parcelable;

import com.example.gordonyoon.spotifystreamer.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class TrackInfo implements Parcelable {

    private String mTrackName;
    private String mAlbumName;
    private String mSmallImageUrl;
    private String mLargeImageUrl;
    private String mArtist;
    private String mPreview;

    public TrackInfo(Track track) {
        if (track.artists.size() > 0) {
            mArtist = track.artists.get(0).name;
        } else {
            mArtist = "";
        }

        mTrackName = track.name;
        mAlbumName = track.album.name;
        Image image = ImageUtils.selectClosestImage(track.album.images, ImageUtils.SMALL_IMAGE_SIZE_TARGET);
        mSmallImageUrl = (image != null) ? image.url : null;
        image = ImageUtils.selectClosestImage(track.album.images, ImageUtils.LARGE_IMAGE_SIZE_TARGET);
        mLargeImageUrl = (image != null) ? image.url : null;
        mPreview = track.preview_url;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getTrackName() {
        return mTrackName;
    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public String getSmallImageUrl() {
        return mSmallImageUrl;
    }

    public String getLargeImageUrl() {
        return mLargeImageUrl;
    }

    public String getPreview() {
        return mPreview;
    }

    public static List<TrackInfo> getTrackInfoList(List<Track> tracks) {
        List<TrackInfo> trackInfos = new ArrayList<>();
        for (Track track : tracks) {
            trackInfos.add(new TrackInfo(track));
        }
        return trackInfos;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mTrackName);
        dest.writeString(this.mAlbumName);
        dest.writeString(this.mSmallImageUrl);
        dest.writeString(this.mLargeImageUrl);
        dest.writeString(this.mArtist);
        dest.writeString(this.mPreview);
    }

    protected TrackInfo(Parcel in) {
        this.mTrackName = in.readString();
        this.mAlbumName = in.readString();
        this.mSmallImageUrl = in.readString();
        this.mLargeImageUrl = in.readString();
        this.mArtist = in.readString();
        this.mPreview = in.readString();
    }

    public static final Creator<TrackInfo> CREATOR = new Creator<TrackInfo>() {
        public TrackInfo createFromParcel(Parcel source) {
            return new TrackInfo(source);
        }

        public TrackInfo[] newArray(int size) {
            return new TrackInfo[size];
        }
    };
}
