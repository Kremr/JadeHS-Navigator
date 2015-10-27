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
package de.jadehs.jadehsnavigator.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.jadehs.jadehsnavigator.R;
import de.jadehs.jadehsnavigator.model.VPlanItem;

/**
 * Created by Nico on 11.08.2015.
 */
public class VPlanAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<VPlanItem> vPlanItems;

    public VPlanAdapter (Context context, ArrayList<VPlanItem> vPlanItems) {
        this.context = context;
        this.vPlanItems = vPlanItems;
    }

    @Override
    public int getCount() {
        return this.vPlanItems.size();
    }

    @Override
    public Object getItem(int position) {
        return this.vPlanItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.vplan_list_item, null);
        }

        TextView txtStart = (TextView) convertView.findViewById(R.id.startVorlesung);
        TextView txtEnd = (TextView) convertView.findViewById(R.id.endeVorlesung);
        TextView txtName = (TextView) convertView.findViewById(R.id.veranstaltung);
        TextView txtProf = (TextView) convertView.findViewById(R.id.dozent);
        TextView txtRoom = (TextView) convertView.findViewById(R.id.raum);

        txtStart.setText(this.vPlanItems.get(position).getStartTime());
        txtEnd.setText(this.vPlanItems.get(position).getEndTime());
        txtName.setText(this.vPlanItems.get(position).getModulName());
        txtProf.setText(this.vPlanItems.get(position).getProfName());
        txtRoom.setText(this.vPlanItems.get(position).getRoom());

        return convertView;
    }

    public ArrayList<VPlanItem> getvPlanItems() {
        return vPlanItems;
    }
}
