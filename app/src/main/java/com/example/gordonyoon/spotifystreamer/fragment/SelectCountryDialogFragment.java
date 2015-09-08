package com.example.gordonyoon.spotifystreamer.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.example.gordonyoon.spotifystreamer.R;

public class SelectCountryDialogFragment extends DialogFragment {

    public static final String TAG = "SelectCountryDialogFragmentTag";
    public static final String KEY_COUNTRY = "country";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_select_country, null);

        // set autocomplete options
        final AutoCompleteTextView textView =
                (AutoCompleteTextView) view.findViewById(R.id.select_country);
        String[] countries = getResources().getStringArray(R.array.countries_array);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(
                        getActivity(),
                        android.R.layout.simple_list_item_1,
                        countries);
        textView.setAdapter(adapter);

        final SharedPreferences prefs = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE);
        String country = prefs.getString(KEY_COUNTRY, null);
        if (country != null) {
            textView.setText(country);
            textView.dismissDropDown();
            textView.selectAll();
        }

        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.dialog_button_select,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString(KEY_COUNTRY, textView.getText().toString()).commit();
                            }
                        })
                .setNegativeButton(R.string.dialog_button_default,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.remove(KEY_COUNTRY).commit();
                            }
                        });
        builder.setTitle(R.string.select_country);
        return builder.create();
    }
}
