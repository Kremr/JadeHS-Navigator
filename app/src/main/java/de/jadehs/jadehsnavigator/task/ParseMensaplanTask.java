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
package de.jadehs.jadehsnavigator.task;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import de.jadehs.jadehsnavigator.R;
import de.jadehs.jadehsnavigator.model.Mensaplan;
import de.jadehs.jadehsnavigator.response.MensaPlanAsyncResponse;

public class ParseMensaplanTask extends AsyncTask<Void, Void, ArrayList<ArrayList>> {
    public MensaPlanAsyncResponse delegate=null;
    private final String TAG = "ParseMensaplanTask";

    private Activity activity = null;

    private Mensaplan mensaplan = null;


    public ArrayList<ArrayList> getGerichte() {
        return gerichte;
    }

    private ArrayList<ArrayList> gerichte;

    public ParseMensaplanTask(Activity activity){
        this.activity = activity;

    }

    @Override
    protected ArrayList<ArrayList> doInBackground(Void... params) {

        this.mensaplan = new Mensaplan(this.activity.getApplicationContext());
        this.gerichte = this.mensaplan.parseMensaplan();
        Log.wtf(TAG,""+gerichte.size());
        return gerichte;
    }

    @Override
    protected void onPreExecute() {
        try {

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    protected void onPostExecute(ArrayList<ArrayList> items) {
        delegate.processFinish(items);
    }


}

