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


import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.jadehs.jadehsnavigator.R;
import de.jadehs.jadehsnavigator.adapter.MensaplanPagerAdapter;
import de.jadehs.jadehsnavigator.database.MensaplanDayDataSource;
import de.jadehs.jadehsnavigator.database.MensaplanMealDataSource;
import de.jadehs.jadehsnavigator.response.MensaPlanAsyncResponse;
import de.jadehs.jadehsnavigator.task.ParseMensaplanTask;
import de.jadehs.jadehsnavigator.util.CalendarHelper;
import de.jadehs.jadehsnavigator.util.Preferences;

public class MensaplanFragment extends Fragment implements MensaPlanAsyncResponse{

    private Preferences preferences;
    private SwipeRefreshLayout swipeLayout;
    private int week = 0;
    private ArrayList<ArrayList> mensaplanWeeks;
    private final String TAG = "MensaplanFragment";
    private boolean secondTime = false;
    private ViewPager mViewPager;


    public MensaplanFragment () {

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        preferences = new Preferences(getActivity().getApplicationContext());









    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null){
            // Eimstellungen aus dem Menü entfernen
            menu.findItem(R.id.action_settings).setVisible(false);
        }

        inflater.inflate(R.menu.fragment_mensaplan, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.refresh_mensaplan:
                swipeLayout.setRefreshing(true);
                update(true);
                break;
            case R.id.changeWeek_mensaplan:
                switch (week) {
                    case 0:
                        week = 1;
                        item.setTitle("Zurück zur aktuellen Woche");
                        break;
                    case 1:
                        week = 0;
                        item.setTitle("Nächste Woche");
                        break;
                }
                processFinish(mensaplanWeeks);
                Toast.makeText(getActivity().getApplicationContext(), "Woche gewechselt.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.mensaplan_action_info:
                showDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mensaplan, container, false);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeMensaplan);
        swipeLayout.setColorSchemeColors(R.color.swipe_color_1, R.color.swipe_color_2, R.color.swipe_color_3);

        return rootView;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        update(false);
         /* Set up a Refreshlistener on the swipelayout*/
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update(true);
            }
        });
        if(!preferences.getBoolean("readInstruction", false)) {
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // don't show this dialog again
                preferences.save("readInstruction", true);
                String instructionMsg = String.format(getActivity().getResources().getString(R.string.mensaplan_belehrung), preferences.getLocation());

                builder.setTitle(getActivity().getResources().getString(R.string.mensaplan_belehrung_title));
                builder.setMessage(instructionMsg)
                        // Positiv-Button wird deklariert
                        .setPositiveButton(getActivity().getResources().getString(R.string.mensaplan_belehrung_positivebutton),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(
                                            final DialogInterface dialog,
                                            final int id) {
                                        dialog.dismiss();
                                    }
                                });



                AlertDialog alert = builder.create();

                alert.setCanceledOnTouchOutside(false);
                alert.show();
            }catch (Exception ex){
                Log.wtf("EXXX", "EX", ex);
            }
        }

    }


    public void update(Boolean refreshButtonClicked) {
        swipeLayout.setRefreshing(true);

        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        CalendarHelper calendarWeekHelper = new CalendarHelper();
        int weekNumber = calendarWeekHelper.getWeekNumber();


        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        MensaplanMealDataSource mensaplanMealDataSource = new MensaplanMealDataSource(getActivity().getApplicationContext());
        MensaplanDayDataSource mensaplanDayDataSource = new MensaplanDayDataSource(getActivity().getApplicationContext());

        try {
            mensaplanDayDataSource.open();
            mensaplanMealDataSource.open();
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Fehler: Zugriff auf Datenbank nicht möglich.", Toast.LENGTH_SHORT).show();
        }

        Boolean needToRefresh = mensaplanDayDataSource.needToRefresh(weekNumber,preferences.getLocation());
        if (isConnected && (needToRefresh || refreshButtonClicked))
        {

            if(refreshButtonClicked || needToRefresh){
                mensaplanDayDataSource.deleteMensaplanDay();
                mensaplanMealDataSource.deleteMensaplanDay();
            }
            ParseMensaplanTask asyncTask = new ParseMensaplanTask(getActivity());
            asyncTask.delegate = this;
            asyncTask.execute();

        } else if (!needToRefresh) {
            try {
                Log.wtf(TAG, "update: Aus Datenbank abrufen.");
                // Datenquelle öffnen und Einträge abrufen
                mensaplanWeeks= mensaplanDayDataSource.getMensaplanDays(preferences.getLocation());
                processFinish(mensaplanWeeks);


            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(getActivity(), "Fehler beim Abrufen des Mensaplans", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity(), "Keine Internetverbindung vorhanden, Daten konnten nicht aktualisiert werden.", Toast.LENGTH_LONG).show();
           // getActivity().findViewById(R.id.progressMensaplan).setVisibility(View.GONE);
            getActivity().findViewById(R.id.errorOverlay).setVisibility(View.VISIBLE);

        }
        mensaplanMealDataSource.close();
        mensaplanDayDataSource.close();

    }

    @Override
    public void processFinish(ArrayList<ArrayList> items) {

        mensaplanWeeks = items;


        CalendarHelper calendarWeekHelper = new CalendarHelper();
        try{
            //getActivity().findViewById(R.id.progressMensaplan).setVisibility(View.GONE);
            //getActivity().findViewById(R.id.swipeMensaplan).setVisibility(View.INVISIBLE);
            mViewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
            MensaplanPagerAdapter mensaplanPagerAdapter = new MensaplanPagerAdapter(getActivity(), items.get(week));
            mViewPager.setAdapter(mensaplanPagerAdapter);
            if(week!=1) {
                mViewPager.setCurrentItem(calendarWeekHelper.getDay());
            }


            //mTabLayout.setTabTextColors(R.color.white,R.color.list_divider);
            TabLayout mTabLayout    = (TabLayout) getActivity().findViewById(R.id.tabs);
            mTabLayout.setupWithViewPager(mViewPager);
        } catch (Exception e) {
            e.printStackTrace();
            Log.wtf("processFinish", "Aktualisierung unterbrochen, View gewechselt");
        }
        swipeLayout.setRefreshing(false);
        secondTime = true;
    }


    public void showDialog () {
        String [] tmp;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String iconText = "";
        String [] stringList  = getActivity().getResources().getStringArray(R.array.mensaplan_additivies);

        // WorkAround wegen eines Bugs im Android XML-Parser https://code.google.com/p/androidsvg/issues/detail?id=29
        List <String> list = Arrays.asList(stringList);
        ArrayList<String> arrayList = new ArrayList<>(list);
        for(int i = 0; i<arrayList.size(); i++) {
            String zusatzstoff = arrayList.get(i);
            //TODO vegan# und co. auslagern
            if(zusatzstoff.startsWith("vegan#")) {
                tmp = zusatzstoff.split("#");

                iconText = "\uD83C\uDF31" + tmp[1];
                arrayList.set(i,iconText);
            }
            if(zusatzstoff.startsWith("rind#")) {
                tmp = zusatzstoff.split("#");
                iconText = "\uD83D\uDC2E" + tmp[1];
                //iconText = "\uD83D\uDC04" + tmp[1];
                arrayList.set(i,iconText);
            }
            if(zusatzstoff.startsWith("gefluegel#")) {
                tmp = zusatzstoff.split("#");
                iconText = "\uD83D\uDC14" + tmp[1];
                arrayList.set(i,iconText);

            }
            if(zusatzstoff.startsWith("schwein#")) {
                tmp = zusatzstoff.split("#");
                //D8 3D DC 37
                iconText = "\uD83D\uDC37" + tmp[1];
                //iconText = "\uD83D\uDC16" + tmp[1];
                arrayList.set(i,iconText);

            }
            if(zusatzstoff.startsWith("vegetarisch#")) {
                tmp = zusatzstoff.split("#");
                iconText = "\uD83C\uDF3D" + tmp[1];
                arrayList.set(i,iconText);

            }
            if(zusatzstoff.startsWith("lamm#")) {
                tmp = zusatzstoff.split("#");
                iconText = "\uD83D\uDC11" + tmp[1];
                arrayList.set(i,iconText);
            }

        }
        stringList = arrayList.toArray(new String[list.size()]);


        ListView modeList = new ListView(getActivity().getApplicationContext());
        modeList.setVerticalScrollBarEnabled(true);
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(),R.layout.mensaplan_dialog_list_item,R.id.txtMensaplan_dialog_list_item, stringList);
        modeList.setAdapter(modeAdapter);

        builder.setView(modeList);
        builder.setTitle(getActivity().getResources().getString(R.string.mensaplan_zusatzstoffe_title));
        builder.setPositiveButton(getActivity().getResources().getString(R.string.mensaplan_zusatzstoffe_positivebutton), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }

        });
        builder.setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

}