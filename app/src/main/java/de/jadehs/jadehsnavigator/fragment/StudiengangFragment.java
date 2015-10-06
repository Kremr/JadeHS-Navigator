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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import de.jadehs.jadehsnavigator.R;
import de.jadehs.jadehsnavigator.adapter.StudiengangAdapter;
import de.jadehs.jadehsnavigator.model.StudiengangItem;
import de.jadehs.jadehsnavigator.response.StudiengangAsyncResponse;
import de.jadehs.jadehsnavigator.task.ParseStudiengangTask;
import de.jadehs.jadehsnavigator.util.Preferences;

/**
 * Created by Nico on 14.08.2015.
 */
public class StudiengangFragment extends Fragment implements StudiengangAsyncResponse {
    private final String TAG = "StudiengangFragment";

    private ConnectivityManager connectivityManager;
    private NetworkInfo activeNetwork;
    private ParseStudiengangTask studiengangTask;
    private Preferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_studiengang, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        chooseStudiengang();
    }

    @Override
    public void processFinishedStudiengang(ArrayList<StudiengangItem> studiengangItems) {
        try {
            if (studiengangItems != null) {
                Log.wtf("ASYNC", "ASYNC TASK FINISHED");

                ListView lv = (ListView) getActivity().findViewById(R.id.list_studiengang);

                StudiengangAdapter studiengangAdapter = new StudiengangAdapter(getActivity(), studiengangItems);

                lv.setAdapter(studiengangAdapter);
            } else {
                Log.wtf("Fragment", "is null");
                Toast.makeText(getActivity().getApplicationContext(), "Keine Studiengänge gefunden! Bitte versuche es später erneut!", Toast.LENGTH_LONG).show();
            }
        }catch (Exception ex){
            Log.wtf(TAG, "Err", ex);
        }
    }

    public void chooseStudiengang() {
        this.connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        this.activeNetwork = connectivityManager.getActiveNetworkInfo();
        this.preferences = new Preferences(getActivity().getApplicationContext());

        boolean isConnected = (activeNetwork != null) && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            Toast.makeText(getActivity().getApplicationContext(), "Lade Studiengänge", Toast.LENGTH_LONG).show();

            this.studiengangTask = new ParseStudiengangTask(getActivity(), this.preferences.getFB());
            this.studiengangTask.delegate = this;
            this.studiengangTask.execute();
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Die Studiengänge können nur mit einer Internetverbindung abgerufen werden.", Toast.LENGTH_LONG).show();
        }
    }
}
