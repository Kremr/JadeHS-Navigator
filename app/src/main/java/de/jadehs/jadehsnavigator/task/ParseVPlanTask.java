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
import android.view.View;

import java.util.ArrayList;

import de.jadehs.jadehsnavigator.R;
import de.jadehs.jadehsnavigator.model.VPlan;
import de.jadehs.jadehsnavigator.model.VPlanItem;
import de.jadehs.jadehsnavigator.response.VPlanAsyncResponse;

public class ParseVPlanTask extends AsyncTask<Void, Void, ArrayList<VPlanItem>> {

    public VPlanAsyncResponse delegate = null;

    private Activity activity = null;
    private String url = null;
    private int fb;
    private String weekOfYear;
    private VPlan vPlan = null;

    public ParseVPlanTask(Activity activity, String url, int fb, String weekOfYear) {
        this.activity = activity;
        this.url = url;
        this.fb = fb;
        this.weekOfYear = weekOfYear;
    }

    @Override
    protected ArrayList<VPlanItem> doInBackground(Void... params) {

        this.vPlan = new VPlan(this.activity.getApplicationContext(), this.url, this.fb, this.weekOfYear);
        ArrayList<VPlanItem> vPlanItems = this.vPlan.parseVPlan();

        return vPlanItems;
    }

    @Override
    protected void onPreExecute() {

        try {
            activity.findViewById(R.id.progressVPlan).setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(ArrayList<VPlanItem> items) {
        delegate.processFinished(items);
    }

    public VPlan getvPlan() {
        return vPlan;
    }

    public void setVPlan(VPlan vPlan) {
        this.vPlan = vPlan;
    }
}
