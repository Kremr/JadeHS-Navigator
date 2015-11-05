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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.jadehs.jadehsnavigator.R;
import de.jadehs.jadehsnavigator.adapter.VPlanAdapter;
import de.jadehs.jadehsnavigator.adapter.VPlanPagerAdapter;
import de.jadehs.jadehsnavigator.database.CustomVPlanDataSource;
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
    private CustomVPlanDataSource custom_vplan_datasource;
    private boolean isCustomVPlanShown;
    private Menu menu;

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
            this.preferences = new Preferences(getActivity().getApplicationContext());

            setCurrentWeekNumber();

            if (studiengangID.startsWith("%")) {
                getVPlanFromDB();
            } else {
                getActivity().findViewById(R.id.progressVPlan).setVisibility(View.GONE);
                getActivity().findViewById(R.id.empty_sg).setVisibility(View.VISIBLE);
            }
        }catch (Exception ex){
            Log.wtf("VPlan", "Err", ex);
        }

        if (!this.preferences.getBoolean("vplan_instructions_read", false)) {

            try {
                this.preferences.save("vplan_instructions_read", true);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setMessage("Durch einen langen Klick fügst du eine Vorlesung deinem eigenen Vorlesungsplan hinzu.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            } catch (Exception ex) {
                Log.wtf("EXXX", "EX", ex);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.wtf(TAG, "Created");
        if (menu != null) {
            // Eimstellungen aus dem Menü entfernen
            menu.findItem(R.id.action_settings).setVisible(false);
        }

        this.menu = menu;

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
                        setCustomVPlanShown(false);
                        switchVPlanSymbol();
                        if (studiengangID.startsWith("%")) {
                            setCurrentWeekNumber(which + 1);
                            updateVPlan();
                        }
                        dialog.dismiss();
                    }
                }).create();

                builder.show();
                break;
            case R.id.refresh_vplan:
                this.isCustomVPlanShown = false;

                updateVPlan();
                break;

            case R.id.show_custom_vplan:
                try {
                    if (!item.isChecked()) {
                        this.isCustomVPlanShown = true;
                        getCustomVPlan();

                        //ListView lv = (ListView) getView().findViewById(R.id.list_studiengang);
                        //VPlanAdapter vPlanAdapter = (VPlanAdapter) lv.getAdapter();

                        Toast.makeText(getActivity().getApplicationContext(), "Eigenen Vorlesungsplan ausgewählt.", Toast.LENGTH_LONG);

                    } else {
                        this.isCustomVPlanShown = false;
                        getVPlanFromDB();
                        getActivity().findViewById(R.id.empty_custom_vplan).setVisibility(View.GONE);

                        //ListView lv = (ListView) getView().findViewById(R.id.list_studiengang);
                        //VPlanAdapter vPlanAdapter = (VPlanAdapter) lv.getAdapter();
                    }
                    break;
                }catch (Exception ex){
                    Log.wtf(TAG, "Err", ex);
                }
        }
        switchVPlanSymbol();

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
                vPlanPagerAdapter.setIsCustomVPlanShown(false);
                viewpager = (ViewPager) getActivity().findViewById(R.id.vplan_viewpager);
                viewpager.setAdapter(vPlanPagerAdapter);
                viewpager.setCurrentItem(calendarHelper.getDay());
                viewpager.setOffscreenPageLimit(5);

                vPlanTabLayout = (VPlanTabLayout) getActivity().findViewById(R.id.vplan_sliding_tabs);
                vPlanTabLayout.setmViewPager(viewpager);

                this.datasource.close();
            } else
                getActivity().findViewById(R.id.empty_vplan).setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
        }
        //getActivity().findViewById(R.id.vplan_semester).setVisibility(View.GONE);
        getActivity().findViewById(R.id.progressVPlan).setVisibility(View.GONE);
    }

    public void getCustomVPlan() {
        try {
            // Datenquelle öffnen und Einträge aufrufen
            this.custom_vplan_datasource = new CustomVPlanDataSource(getActivity().getApplicationContext());
            this.custom_vplan_datasource.open();

            ArrayList<VPlanItem> vPlanItems = this.custom_vplan_datasource.getAllCustomVPlanItems();

            Collections.sort(vPlanItems, new Comparator<VPlanItem>() {
                @Override
                public int compare(VPlanItem lhs, VPlanItem rhs) {
                    return lhs.getStartTime().compareTo(rhs.getStartTime());
                }
            });

            if (!vPlanItems.isEmpty()) {
                VPlanPagerAdapter vPlanPagerAdapter = new VPlanPagerAdapter(getActivity(), vPlanItems, this.weekOfYear);
                vPlanPagerAdapter.setIsCustomVPlanShown(true);
                viewpager = (ViewPager) getActivity().findViewById(R.id.vplan_viewpager);
                viewpager.setAdapter(vPlanPagerAdapter);
                viewpager.setCurrentItem(calendarHelper.getDay());

                vPlanTabLayout = (VPlanTabLayout) getActivity().findViewById(R.id.vplan_sliding_tabs);
                vPlanTabLayout.setmViewPager(viewpager);

                this.custom_vplan_datasource.close();
            } else
                getActivity().findViewById(R.id.empty_custom_vplan).setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!this.preferences.getBoolean("custom_vplan_instructions_read", false)) {

            try {
                this.preferences.save("custom_vplan_instructions_read", true);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setMessage("Durch einen langen Klick kannst du eine Vorlesung aus deinem Vorlesungsplan entfernen.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            } catch (Exception ex) {
                Log.wtf("EXXX", "EX", ex);
            }
        }
        //getActivity().findViewById(R.id.vplan_semester).setVisibility(View.GONE);
        getActivity().findViewById(R.id.progressVPlan).setVisibility(View.GONE);
    }

    public void switchVPlanSymbol(){
        MenuItem vplanItem = menu.findItem(R.id.show_custom_vplan);
        if(this.isCustomVPlanShown){
            vplanItem.setChecked(true);
            vplanItem.setIcon(android.R.drawable.btn_star_big_on);
        }else{
            vplanItem.setChecked(false);
            vplanItem.setIcon(android.R.drawable.btn_star_big_off);
        }
    }

    @Override
    public void processFinished(ArrayList<VPlanItem> vPlanItems) {
        Log.wtf("ASYNC", "ASYNC TASK FINISHED");
        try {
            VPlanPagerAdapter vPlanPagerAdapter = new VPlanPagerAdapter(getActivity(), vPlanItems, weekOfYear);
            vPlanPagerAdapter.setIsCustomVPlanShown(false);
            viewpager = (ViewPager) getActivity().findViewById(R.id.vplan_viewpager);
            /**
             * Ermitteln des heutigen Wochentages, damit auf entsprechenden Tab gewechselt werden kann
             */
            viewpager.setAdapter(vPlanPagerAdapter);
            if (calendarHelper.getWeekNumber() == Integer.parseInt(weekOfYear))
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

    public void setCustomVPlanShown(boolean isCustomVPlanShown){
        this.isCustomVPlanShown = isCustomVPlanShown;
    }

    public String getWeekOfYear() {
        return weekOfYear;
    }

    public void setWeekOfYear(String weekOfYear) {
        this.weekOfYear = weekOfYear;
    }
}
