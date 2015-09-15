package com.example.gordonyoon.spotifystreamer.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.gordonyoon.spotifystreamer.R;
import com.example.gordonyoon.spotifystreamer.adapter.ArtistArrayAdapter;
import com.example.gordonyoon.spotifystreamer.adapter.TrackArrayAdapter;
import com.example.gordonyoon.spotifystreamer.spotify.TrackInfo;
import com.example.gordonyoon.spotifystreamer.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

/**
 * A fragment representing a list of an artist's top tracks.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnTrackSelectedListener}
 * interface.
 */
public class TrackFragment extends ListFragment {

    public final static String TAG = "TAG_TRACKFRAGMENT";

    private boolean mTwoPane;
    private Context mContext;
    private Bundle mState;

    // the fragment initialization parameters
    private static final String ARG_SPOTIFY_ID = "spotifyId";

    // key to save/retrieve current list info
    private static final String KEY_TRACK_INFOS = "trackInfosKey";

    private final static String KEY_STATE_BUNDLE = "stateBundle";

    private OnTrackSelectedListener mListener;

    private int mPosition = ListView.INVALID_POSITION;

    public static TrackFragment newInstance(String spotifyId) {
        TrackFragment fragment = new TrackFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SPOTIFY_ID, spotifyId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TrackFragment() {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTwoPane = getResources().getBoolean(R.bool.large_layout);

        mContext = getActivity();

        if (!restoreListView(getArguments())) {
            if (savedInstanceState == null) {
                String spotifyId = getArguments().getString(ARG_SPOTIFY_ID);
                new getTopTracksTask().execute(spotifyId);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveState();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnTrackSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTrackSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (mListener != null) {
            startTrack(position);
        }
        mPosition = position;
    }

    private void saveState() {
        if (getView() != null) {
            mState = getArguments();
            TrackArrayAdapter adapter = (TrackArrayAdapter) getListAdapter();
            if (adapter.getTrackInfos() != null) {
                mState.putParcelableArrayList(KEY_TRACK_INFOS,
                        new ArrayList<Parcelable>(adapter.getTrackInfos()));
            }
        } else if (mState != null) {
            // on a subsequent rotation, the view has not been recreated yet
            // so there is no data to save (except what we've intentionally saved in mState
            getArguments().putBundle(KEY_STATE_BUNDLE, mState);
        }
    }

    private boolean restoreListView(Bundle bundle) {
        List<TrackInfo> trackInfoList = bundle.getParcelableArrayList(KEY_TRACK_INFOS);
        if (trackInfoList != null) {
            TrackArrayAdapter adapter =
                    new TrackArrayAdapter(mContext,
                            R.layout.track_row,
                            trackInfoList);
            setListAdapter(adapter);
            return true;
        }
        return false;
    }

    private void startTrack(int position) {
        List<TrackInfo> trackInfos = ((TrackArrayAdapter) getListAdapter()).getTrackInfos();
        mListener.onTrackSelected(trackInfos, position);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnTrackSelectedListener {
        public void onTrackSelected(List<TrackInfo> trackInfos, int position);
    }

    /**
     * Search Spotify for the artist's top tracks.
     * Set the ListAdapter with the results.
     */
    private class getTopTracksTask extends AsyncTask<String, Void, Tracks> {

        @Override
        protected Tracks doInBackground(String... params) {
            if (NetworkUtils.isNetworkAvailable(mContext)) {
                try {
                    SharedPreferences prefs = getActivity().getSharedPreferences(
                            getString(R.string.preference_file_key),
                            Context.MODE_PRIVATE);

                    HashMap<String, Object> map = new HashMap<String, Object>();
                    String country = prefs.getString(SelectCountryDialogFragment.KEY_COUNTRY, null);
                    if (country != null) {
                        String countryCode = country.substring(country.length() - 2);
                        map.put("country", countryCode);
                    } else {
                        map.put("country", Locale.getDefault().getCountry());
                    }

                    SpotifyService spotify = new SpotifyApi().getService();
                    return spotify.getArtistTopTrack(params[0], map);
                } catch (RetrofitError e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Tracks tracks) {
            super.onPostExecute(tracks);

            if (tracks != null) {
                if (tracks.tracks.size() > 0) {
                    TrackArrayAdapter adapter =
                            new TrackArrayAdapter(mContext,
                                    R.layout.track_row,
                                    TrackInfo.getTrackInfoList(tracks.tracks));
                    setListAdapter(adapter);
                } else {
                    Toast.makeText(mContext,
                            R.string.toast_no_tracks_found,
                            Toast.LENGTH_LONG).show();
                    setListAdapter(new TrackArrayAdapter(mContext, R.layout.track_row));
                }
            } else {
                int text = R.string.toast_no_network_connectivity;
                Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
                setListAdapter(new TrackArrayAdapter(mContext, R.layout.track_row));
            }
        }
    }
}
