package com.example.gordonyoon.spotifystreamer.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gordonyoon.spotifystreamer.R;
import com.example.gordonyoon.spotifystreamer.spotify.TrackInfo;
import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Populate the list of Top Tracks for the selected artist.
 */
public class TrackArrayAdapter extends ArrayAdapter<TrackInfo> {

    private Context mContext;
    private List<TrackInfo> mTrackInfos;
    private int mRowLayout;

    public TrackArrayAdapter(Context context, int resource, List<TrackInfo> trackInfos) {
        super(context, resource, trackInfos.toArray(new TrackInfo[trackInfos.size()]));
        mContext = context;
        mTrackInfos = trackInfos;
        mRowLayout = resource;
    }

    public TrackArrayAdapter(Context context, int resource) {
        super(context, resource);
    }

    public List<TrackInfo> getTrackInfos() {
        return mTrackInfos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater)mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(mRowLayout, parent, false);
        }

        TrackInfo trackInfo = mTrackInfos.get(position);

        // set the album image
        ImageView imageView = (ImageView)v.findViewById(R.id.top_track_image);
        String smallImageUrl = trackInfo.getSmallImageUrl();
        if (smallImageUrl == null) {
            Picasso.with(mContext).load(R.drawable.greybox).into(imageView);
        } else {
            Picasso.with(mContext).load(smallImageUrl).into(imageView);
        }

        // set the track title
        TextView trackText = (TextView)v.findViewById(R.id.track);
        trackText.setText(trackInfo.getTrackName());

        // set the album title
        TextView albumText = (TextView)v.findViewById(R.id.album);
        albumText.setText(trackInfo.getAlbumName());

        return v;
    }
}
