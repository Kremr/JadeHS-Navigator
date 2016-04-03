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

import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import de.jadehs.jadehsnavigator.R;
import de.jadehs.jadehsnavigator.adapter.InfoSysItemAdapter;
import de.jadehs.jadehsnavigator.database.InfoSysItemDataSource;
import de.jadehs.jadehsnavigator.model.InfoSysItem;
import de.jadehs.jadehsnavigator.response.InfoSysAsyncResponse;
import de.jadehs.jadehsnavigator.task.ParseInfoSysTask;
import de.jadehs.jadehsnavigator.util.CalendarHelper;
import de.jadehs.jadehsnavigator.util.Preferences;

public class InfoSysFragment extends Fragment implements InfoSysAsyncResponse {
    private static final String TAG = "InfoSysFragment";

    private SwipeRefreshLayout swipeLayout;
    private InfoSysItemDataSource datasource;
    private ParseInfoSysTask asyncTask;
    private Preferences preferences;
    private CalendarHelper calendarHelper = new CalendarHelper();

	public InfoSysFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_infosys, container, false);

        /* Sets up SwipeRefreshLayout */
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeInfoSys);
        swipeLayout.setColorSchemeColors(R.color.swipe_color_1, R.color.swipe_color_2, R.color.swipe_color_3);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.wtf(TAG, "View created!");
        super.onViewCreated(view, savedInstanceState);

        /* Set up a Refreshlistener on the swipelayout*/
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateInfoSys(true);
            }
        });

        initializeInfoSys();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_infosys, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_infosys:
                swipeLayout.setRefreshing(true);
                updateInfoSys(false);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    /**
     * Initial load of the InfoSys. This will try to load a number of InfoSys entries
     */
    public void initializeInfoSys(){
        swipeLayout.setRefreshing(true);
        Log.wtf(TAG, "Starting initializeInfoSys");
        try{
            /* Open datasource and create View */
            this.preferences = new Preferences(getActivity().getApplicationContext());
            this.datasource = new InfoSysItemDataSource(getActivity().getApplicationContext());
            this.datasource.open();
            ArrayList<InfoSysItem> infoSysItems = this.datasource.getInfoSysItemsFromFB(this.preferences.getFB());

            processFinish(infoSysItems); // create View

            this.datasource.close();

            // try to update
            updateInfoSys(false);
        }catch (Exception ex){
            Log.wtf(TAG,"DATABASE LOAD", ex);
        }
    }

    public void updateInfoSys(){
        // Is not a swipe refresh
        updateInfoSys(false);
    }

    /**
     * Updates the view for this fragment if a internet connection is available
     *
     * @param isSwipeRefresh
     */
    public void updateInfoSys(boolean isSwipeRefresh) {
        swipeLayout.setRefreshing(true);
        Log.wtf(TAG, "Starting updateInfoSys");
        this.preferences = new Preferences(getActivity().getApplicationContext());

        TextView txtLastUpdate = (TextView) getActivity().findViewById(R.id.txtLastUpdate);
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if(isConnected) {
            try {
                /* launches an asynchronus task that will fetch all infosys items for the current department */
                this.asyncTask = new ParseInfoSysTask(getActivity(), this.preferences.getInfoSysURL(), this.preferences.getFB(), isSwipeRefresh);
                this.asyncTask.delegate = this;
                this.asyncTask.execute();
            }catch (Exception ex){
                Log.wtf(TAG,"INTERNET LOAD", ex);
            }
        }else{
            txtLastUpdate.setText("Um neue Einträge abzurufen, bitte Internetverbindung herstellen");


        }
    }

    public void processFinish(ArrayList<InfoSysItem> items, boolean isSwipeRefresh) {

        processFinish(items);
    }

    @Override
    public void processFinish(ArrayList<InfoSysItem> items) {
        Log.wtf(TAG,"Starting processFinish");
        try {
            getActivity().findViewById(R.id.progressContainer).setVisibility(View.GONE); // Hides loading icon
            if(items.size() > 0) {
                ListView lv = (ListView) getActivity().findViewById(R.id.listInfoSys);

                InfoSysItemAdapter adapter = new InfoSysItemAdapter(getActivity(), items);

                lv.setAdapter(adapter);
            }
            TextView txtLastUpdate = (TextView) getActivity().findViewById(R.id.txtLastUpdate);
            txtLastUpdate.setText("Letzte Aktualisierung: " + calendarHelper.getDateRightNow(true));
            swipeLayout.setRefreshing(false);
        }catch (Exception ex){
            Log.wtf(TAG,"ERROR",ex);
        }
    }
}
