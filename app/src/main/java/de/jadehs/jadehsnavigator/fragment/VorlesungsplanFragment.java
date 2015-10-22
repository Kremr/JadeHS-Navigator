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
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import de.jadehs.jadehsnavigator.R;
import de.jadehs.jadehsnavigator.adapter.VPlanPagerAdapter;
import de.jadehs.jadehsnavigator.database.VPlanItemDataSource;
import de.jadehs.jadehsnavigator.model.VPlanItem;
import de.jadehs.jadehsnavigator.response.VPlanAsyncResponse;
import de.jadehs.jadehsnavigator.task.ParseVPlanTask;
import de.jadehs.jadehsnavigator.util.CalendarHelper;
import de.jadehs.jadehsnavigator.util.Preferences;
import de.jadehs.jadehsnavigator.view.VPlanTabLayout;

public class VorlesungsplanFragment extends Fragment implements VPlanAsyncResponse {
    private final String TAG = "VorlesungsplanFragment";

    private ConnectivityManager connectivityManager;
    private NetworkInfo activeNetwork;
    private ParseVPlanTask vPlanTask;
    private Preferences preferences;
    private VPlanItemDataSource datasource;

    private String studiengangID = "";
    private String url;
    private String weekOfYear;

    private ViewPager viewpager;
    private VPlanTabLayout vPlanTabLayout;
    private CalendarHelper calendarHelper = new CalendarHelper();


    // Konstruktor
    public VorlesungsplanFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_vorlesungsplan, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        try {
            super.onViewCreated(view, savedInstanceState);

            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("JHSNAV_PREFS", Context.MODE_PRIVATE);

            this.studiengangID = sharedPreferences.getString("StudiengangID", "");

            Log.i("STUDIENGANG", studiengangID);

            setCurrentWeekNumber();

            if (studiengangID.startsWith("%")) {
                //this.weekOfYear = new SimpleDateFormat("w").format(new java.util.Date()).toString();
                //this.weekOfYear = calendarHelper.getWeekNumber();
                //setCurrentWeekNumber();
                // LADE AKTUELLEN VPLAN
                //updateVPlan();
                getVPlanFromDB();
            } else {
                //Toast.makeText(getActivity().getApplicationContext(), "Bitte wähle einen Studiengang in den Einstellungen aus!", Toast.LENGTH_LONG).show();
                // zeige fehler overlay
                getActivity().findViewById(R.id.progressVPlan).setVisibility(View.GONE);
                getActivity().findViewById(R.id.empty_sg).setVisibility(View.VISIBLE);
            }
        }catch (Exception ex){
            Log.wtf("VPlan", "Err", ex);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            // Eimstellungen aus dem Menü entfernen
            menu.findItem(R.id.action_settings).setVisible(false);
        }

        inflater.inflate(R.menu.fragment_vorlesungsplan, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.change_kw_vplan:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Kalenderwoche wählen:");

                final String[] strings = new String[52];

                for (int i = 0; i <= 51; i++) {
                    strings[i] = "" + (i + 1);
                }
                builder.setSingleChoiceItems(strings, Integer.parseInt(this.weekOfYear)-1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //setWeekOfYear(strings[which]);
                        if (studiengangID.startsWith("%")) {
                            setCurrentWeekNumber(which + 1);
                            updateVPlan();
                        }
                        dialog.dismiss();
                    }
                }).create();

                builder.show();
                break;
            /*
            case R.id.show_semester:
                getActivity().findViewById(R.id.vplan_semester).setVisibility(View.VISIBLE);
                /*int currentWeek = Integer.parseInt(new SimpleDateFormat("w").format(new java.util.Date()));
                if (currentWeek < 34)
                    this.weekOfYear = "1-33";
                else
                    this.weekOfYear = "34-52";
                updateVPlan();
                setCurrentWeekNumber();
                break;
            */
            case R.id.refresh_vplan:
                updateVPlan();
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateVPlan() {
        try {

            // SharedPreference auslesen
            SharedPreferences sp = getActivity().getSharedPreferences("JHSNAV_PREFS", Context.MODE_PRIVATE);
            this.studiengangID = sp.getString("StudiengangID", "");

            this.connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            this.activeNetwork = connectivityManager.getActiveNetworkInfo();
            this.preferences = new Preferences(getActivity().getApplicationContext());

            url = this.preferences.getVPlanURL() + this.studiengangID + "&weeks=" + this.weekOfYear + "&days=";

            boolean isConnected = (activeNetwork != null) && activeNetwork.isConnectedOrConnecting();

            if (isConnected) {
                this.vPlanTask = new ParseVPlanTask(getActivity(), this.url, this.preferences.getFB(), this.weekOfYear);
                this.vPlanTask.delegate = this;
                this.vPlanTask.execute();
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "Aktualisierung fehlgeschlagen, bitte eine Internetverbindung herstellen.", Toast.LENGTH_LONG).show();
                getVPlanFromDB();
            }
        }catch (Exception ex){
            Log.wtf("VPlan", "Err",ex);
        }
    }

    public void getVPlanFromDB() {

        try {
            // Datenquelle öffnen und Einträge aufrufen
            this.datasource = new VPlanItemDataSource(getActivity().getApplicationContext());
            this.datasource.open();

            // SharedPreference auslesen
            SharedPreferences sp = getActivity().getSharedPreferences("JHSNAV_PREFS", Context.MODE_PRIVATE);
            this.studiengangID = sp.getString("StudiengangID", "");

            ArrayList<VPlanItem> vPlanItems = this.datasource.getVPlanItem(this.studiengangID, this.weekOfYear);

            if (!vPlanItems.isEmpty()) {
                VPlanPagerAdapter vPlanPagerAdapter = new VPlanPagerAdapter(getActivity(), vPlanItems, this.weekOfYear);
                viewpager = (ViewPager) getActivity().findViewById(R.id.vplan_viewpager);
                viewpager.setAdapter(vPlanPagerAdapter);
                viewpager.setCurrentItem(calendarHelper.getDay());

                vPlanTabLayout = (VPlanTabLayout) getActivity().findViewById(R.id.vplan_sliding_tabs);
                vPlanTabLayout.setmViewPager(viewpager);

                this.datasource.close();
            } else
                getActivity().findViewById(R.id.empty_vplan).setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
        }
        getActivity().findViewById(R.id.vplan_semester).setVisibility(View.GONE);
        getActivity().findViewById(R.id.progressVPlan).setVisibility(View.GONE);
    }

    @Override
    public void processFinished(ArrayList<VPlanItem> vPlanItems) {
        Log.wtf("ASYNC", "ASYNC TASK FINISHED");
        try {
            VPlanPagerAdapter vPlanPagerAdapter = new VPlanPagerAdapter(getActivity(), vPlanItems, weekOfYear);
            viewpager = (ViewPager) getActivity().findViewById(R.id.vplan_viewpager);
            /**
             * Ermitteln des heutigen Wochentages, damit auf entsprechenden Tab gewechselt werden kann
             */
            viewpager.setAdapter(vPlanPagerAdapter);
            viewpager.setCurrentItem(calendarHelper.getDay());

            vPlanTabLayout = (VPlanTabLayout) getActivity().findViewById(R.id.vplan_sliding_tabs);
            vPlanTabLayout.setmViewPager(viewpager);


            getActivity().findViewById(R.id.progressVPlan).setVisibility(View.GONE);
            getActivity().findViewById(R.id.vplan_semester).setVisibility(View.GONE);

            if (!vPlanItems.isEmpty())
                getActivity().findViewById(R.id.empty_vplan).setVisibility(View.GONE);
            else
                getActivity().findViewById(R.id.empty_vplan).setVisibility(View.VISIBLE);
        }catch (Exception ex) {
            Log.wtf(TAG, "Err", ex);
        }
    }

    public void setCurrentWeekNumber() {
        //this.weekOfYear = new SimpleDateFormat("w").format(new java.util.Date()).toString();
        this.weekOfYear = ""+calendarHelper.getWeekNumber();
        Log.wtf("weekOfYear", this.weekOfYear);
    }

    public void setCurrentWeekNumber(int which) {
        //this.weekOfYear = new SimpleDateFormat("w").format(new java.util.Date()).toString();
        this.weekOfYear = ""+which;
        Log.wtf("weekOfYear", this.weekOfYear);
    }

    public String getWeekOfYear() {
        return weekOfYear;
    }

    public void setWeekOfYear(String weekOfYear) {
        this.weekOfYear = weekOfYear;
    }
}
