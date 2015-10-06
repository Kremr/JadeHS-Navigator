/**
 * This file is part of JadeHS-Navigator.
 *
 * JadeHS-Navigator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * JadeHS-Navigator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with JadeHS-Navigator.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.jadehs.jadehsnavigator.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import de.jadehs.jadehsnavigator.R;
import de.jadehs.jadehsnavigator.StudiengangActivity;
import de.jadehs.jadehsnavigator.database.DBHelper;
import de.jadehs.jadehsnavigator.util.Preferences;

public class SettingsFragment extends PreferenceFragment {
    final String TAG = "SettingsFragment";

    private DBHelper dbHelper;
    private Preferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager prefMngr = getPreferenceManager();
        prefMngr.setSharedPreferencesName("JHSNAV_PREFS");
        prefMngr.setSharedPreferencesMode(Context.MODE_PRIVATE);

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            dbHelper = new DBHelper(getActivity().getApplicationContext());

            this.preferences = new Preferences(getActivity().getApplicationContext());

            /**
             * Reset Button deklarieren
             */
            final Preference reset = findPreference("reset");
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            // Reaktion der Betätigung wird programmiert
            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(final Preference preference) {
                    try {
                        // Der Dialog wird erzeugt
                        builder.setMessage(getString(R.string.preference_reset))
                                .setCancelable(true)
                                        // Positiv-Button wird deklariert
                                .setPositiveButton(getString(R.string.positive),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    final DialogInterface dialog,
                                                    final int id) {
                                                Preferences pref = new Preferences(getActivity().getApplicationContext());
                                                pref.clearSharedPreference();

                                                // Reset DB
                                                dbHelper.reset();

                                                // set setup done flag
                                                preferences.save("setupDone", true);

                                                // Nachricht wird eingeblendet, dass
                                                // alle Daten gelöscht wurden
                                                Toast.makeText(getActivity().getApplicationContext(),
                                                        getString(R.string.preferences_deleted),
                                                        Toast.LENGTH_LONG).show();

                                                // Der Dialog wird geschlossen
                                                dialog.dismiss();


                                                // Refresh Fragment
                                                getActivity().finish();
                                                getActivity().startActivity(getActivity().getIntent());
                                            }
                                        })
                                        // Negativ-Button wird deklariert
                                .setNegativeButton(getString(R.string.negative),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    final DialogInterface dialog,
                                                    final int id) {
                                                dialog.dismiss();
                                            }
                                        });
                        final AlertDialog alert = builder.create();
                        alert.show();
                        return true;
                    } catch (Exception ex) {
                        Log.wtf(TAG, "ERROR", ex);
                        return false;
                    }
                }
            });

            /**
             * Preference zur Auswahl des Studiengangs
             */
            Preference studiengang = findPreference("studiengang_wahl");

            studiengang.setSummary(this.preferences.get("StudiengangName", ""));

            studiengang.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Intent intent = new Intent(getActivity().getApplicationContext(), StudiengangActivity.class);
                    //startActivity(intent);
                    startActivityForResult(intent, 1);

                    return true;
                }
            });

            /**
             * Feedback button
             */
            final Preference feedback = findPreference("feedback");

            feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.feedbackURL))));

                    return true;
                }
            });

            /**
             * Rate button
             */
            final Preference rate = findPreference("rate");

            rate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final String packageName = getActivity().getPackageName();
                    // try to open the play store page. if it fails, fall back to webbrowser
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                    } catch (Exception ex){
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                    }
                    return true;
                }
            });

            /**
             * Lizenzen button
             */
            final Preference licenses = findPreference("licenses");

            licenses.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    try {
                        builder.setTitle(getString(R.string.licenses));

                        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View layout = inflater.inflate(R.layout.alert_dialog, null);

                        TextView text = (TextView) layout.findViewById(R.id.text);
                        text.setMovementMethod(LinkMovementMethod.getInstance());
                        text.setText(Html.fromHtml(getActivity().getResources().getString(R.string.license_text)));

                        builder.setView(layout);

                        final AlertDialog alert = builder.show();
                    } catch (Exception ex) {
                        Log.wtf("PREFERENCE LICENSE", "ERROR", ex);
                        return false;
                    }
                    return true;
                }
            });

            //
            final Preference fb = findPreference("FBPreference_list");
            String fbname = "";
            switch (preferences.get("FBPreference_list","")){
                case "1": fbname = "Management, Information, Technologie";break;
                case "2": fbname = "Ingenieurwissenschaften"; break;
                case "3": fbname = "Wirtschaft"; break;
                case "4": fbname = "Architektur"; break;
                case "5": fbname = "Bauwesen und Geoinformation"; break;
                case "6": fbname = "Seefahrt"; break;
            }
            fb.setSummary(fbname);

            fb.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String fbname = "";
                    switch (newValue.toString()){
                        case "1": fbname = "Management, Information, Technologie";break;
                        case "2": fbname = "Ingenieurwissenschaften"; break;
                        case "3": fbname = "Wirtschaft"; break;
                        case "4": fbname = "Architektur"; break;
                        case "5": fbname = "Bauwesen und Geoinformation"; break;
                        case "6": fbname = "Seefahrt"; break;
                    }
                    fb.setSummary(fbname);

                    resetSG();

                    return true;
                }
            });

            final Preference version = findPreference("version");
            version.setSummary("Version: " + getString(R.string.version));
            version.setSelectable(false);
        }catch (Exception ex){
            Log.wtf(TAG,"ERR",ex);
        }
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        getActivity().finish();
        getActivity().startActivity(getActivity().getIntent());
    }

    public void resetSG(){
        //
        Preference studiengang = findPreference("studiengang_wahl");
        studiengang.setSummary("");
        preferences.save("StudiengangID","Bitte wählen einen Studiengang");
    }
}
