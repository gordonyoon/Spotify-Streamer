package com.example.gordonyoon.spotifystreamer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gordonyoon.spotifystreamer.R;
import com.example.gordonyoon.spotifystreamer.spotify.ArtistInfo;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Populate the list of artists with data from Spotify
 */
public class ArtistArrayAdapter extends ArrayAdapter<ArtistInfo> {

    private Context mContext;
    private List<ArtistInfo> mArtistInfos;
    private int mRowLayout;

    public ArtistArrayAdapter(Context context, int resource, List<ArtistInfo> artistInfos) {
        super(context, resource, artistInfos.toArray(new ArtistInfo[artistInfos.size()]));
        mContext = context;
        mArtistInfos = artistInfos;
        mRowLayout = resource;
    }

    public ArtistArrayAdapter(Context context, int resource) {
        super(context, resource);
    }

    public List<ArtistInfo> getArtistInfos() {
        return mArtistInfos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater)mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(mRowLayout, parent, false);
        }

        ArtistInfo artistInfo = mArtistInfos.get(position);

        // set the artist image
        ImageView imageView = (ImageView)v.findViewById(R.id.artist_image);
        String imageUrl = artistInfo.getUrl();
        if (imageUrl == null) {
            Picasso.with(mContext).load(R.drawable.greybox).into(imageView);
        } else {
            Picasso.with(mContext).load(imageUrl).into(imageView);
        }

        // set the artist name
        TextView textView = (TextView)v.findViewById(R.id.artist_name);
        textView.setText(artistInfo.getName());

        return v;
    }
}
