package com.example.gordonyoon.spotifystreamer.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.gordonyoon.spotifystreamer.R;
import com.example.gordonyoon.spotifystreamer.adapter.ArtistArrayAdapter;
import com.example.gordonyoon.spotifystreamer.spotify.ArtistInfo;
import com.example.gordonyoon.spotifystreamer.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

/**
 * A fragment representing a list of Artists.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnArtistSelectedListener}
 * interface.
 */
public class ArtistFragment extends ListFragment {

    public final static String TAG = "TAG_ARTISTFRAGMENT";

    private Bundle mState;

    // used to highlight selected artist
    private int mPosition = ListView.INVALID_POSITION;

    private Context mContext;

    private boolean mTwoPane;

    private int mRowLayout;

    private final static String KEY_STATE_BUNDLE = "stateBundle";

    // fragment initialization parameter key
    private static final String ARG_ARTIST_TO_SEARCH = "artistToSearch";

    // key to save/retrieve current list info
    private static final String KEY_ARTIST_INFOS = "artistInfosKey";

    private static final String KEY_SELECTED_ARTIST = "selectedArtistKey";

    private OnArtistSelectedListener mSelectListener;
    private OnArtistSearchedListener mSearchListener;

    public static ArtistFragment newInstance() {
        ArtistFragment fragment = new ArtistFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArtistFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = getActivity();

        // allows the touchSelector to highlight a selected artist
        getListView().setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        // do not highlight selected artist in single pane mode
        mTwoPane = getResources().getBoolean(R.bool.large_layout);
        mRowLayout = mTwoPane ? R.layout.artist_row_two_pane : R.layout.artist_row;

        // on configuration change (i.e., orientation change) reload the list and position
        if (getArguments() != null) {
            restoreListView(getArguments());
        }

        initSearchView(view);
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
            mSelectListener = (OnArtistSelectedListener) activity;
            mSearchListener = (OnArtistSearchedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnArtistSelectedListener AND OnArtistSearchedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mSelectListener = null;
        mSearchListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        mPosition = position;

        if (mSelectListener != null) {
            ArtistInfo artistInfo = (ArtistInfo) getListAdapter().getItem(position);
            mSelectListener.onArtistSelected(artistInfo.getSpotifyId());
        }
    }

    private void saveState() {
        if (getView() != null) {
            mState = getArguments();
            ArtistArrayAdapter adapter = (ArtistArrayAdapter) getListAdapter();
            if (adapter != null && adapter.getArtistInfos() != null) {
                mState.putParcelableArrayList(KEY_ARTIST_INFOS,
                        new ArrayList<Parcelable>(adapter.getArtistInfos()));
                mState.putInt(KEY_SELECTED_ARTIST, mPosition);
            }
        } else if (mState != null) {
            // on a subsequent rotation, the view has not been recreated yet
            // so there is no data to save (except what we've intentionally saved in mState
            getArguments().putBundle(KEY_STATE_BUNDLE, mState);
        }
    }

    private void restoreListView(Bundle savedInstanceState) {
        List<ArtistInfo> artistInfoList = savedInstanceState
                .getParcelableArrayList(KEY_ARTIST_INFOS);
        if (artistInfoList != null) {
            ArtistArrayAdapter adapter =
                    new ArtistArrayAdapter(mContext, mRowLayout, artistInfoList);
            setListAdapter(adapter);

            mPosition = savedInstanceState.getInt(KEY_SELECTED_ARTIST);
            if (mTwoPane && mPosition != ListView.INVALID_POSITION) {
                // cannot set selection immediately after changing the data in the adapter
                getListView().post(new Runnable() {
                    @Override
                    public void run() {
                        getListView().setSelection(mPosition);
                    }
                });
            }
        }
    }

    private void initSearchView(View rootView) {
        // start searching for the artist
        SearchView searchView = (SearchView) rootView.findViewById(R.id.artist_search_bar);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint(getResources().getString(R.string.artist_search_text));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (mSearchListener != null) {
                    mSearchListener.onArtistSearched();
                }
                new getSpotifyArtistsTask().execute(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
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
    public interface OnArtistSelectedListener {
        public void onArtistSelected(String spotifyId);
    }

    public interface OnArtistSearchedListener {
        public void onArtistSearched();
    }

    /**
     * Search Spotify for artists with the search parameter in the name.
     * Set the ListAdapter with the returned results.
     */
    private class getSpotifyArtistsTask extends AsyncTask<String, Void, ArtistsPager> {

        @Override
        protected ArtistsPager doInBackground(String... params) {
            if (NetworkUtils.isNetworkAvailable(mContext)) {
                try {
                    SpotifyService spotify = new SpotifyApi().getService();
                    return spotify.searchArtists(params[0]);
                } catch (RetrofitError e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            super.onPostExecute(artistsPager);

            if (artistsPager != null) {
                if (artistsPager.artists.items.size() > 0) {
                    ArtistArrayAdapter adapter =
                            new ArtistArrayAdapter(mContext,
                                    mRowLayout,
                                    ArtistInfo.getArtistInfoList(artistsPager.artists.items));
                    setListAdapter(adapter);
                } else {
                    Toast.makeText(mContext,
                            R.string.toast_no_artists_found,
                            Toast.LENGTH_SHORT).show();
                    setListAdapter(new ArtistArrayAdapter(mContext, mRowLayout));
                }
            } else {
                int text = R.string.toast_no_network_connectivity;
                Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
                setListAdapter(new ArtistArrayAdapter(mContext, mRowLayout));
            }
        }
    }
}
