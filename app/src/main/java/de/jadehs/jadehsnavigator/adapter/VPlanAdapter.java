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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import de.jadehs.jadehsnavigator.R;
import de.jadehs.jadehsnavigator.database.CustomVPlanDataSource;
import de.jadehs.jadehsnavigator.model.RSSItem;
import de.jadehs.jadehsnavigator.model.VPlanItem;

/**
 * Created by Nico on 11.08.2015.
 */
public class VPlanAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<VPlanItem> vPlanItems;
    boolean isCustomVPlanShown;

    List<Long> activeElements = new ArrayList<>();

    public VPlanAdapter (Context context, ArrayList<VPlanItem> vPlanItems, boolean isCustomVPlanShown) {
        this.context = context;
        this.vPlanItems = vPlanItems;
        this.isCustomVPlanShown = isCustomVPlanShown;
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
        // use holder pattern to recreate layouts
        View row = convertView;
        ViewHolder holder = new ViewHolder();

        VPlanItem vPlanItem = this.vPlanItems.get(position);

        if(row == null) {
            // new layout
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.vplan_list_item, parent, false);

            holder.start = (TextView) row.findViewById(R.id.startVorlesung);
            holder.end = (TextView) row.findViewById(R.id.endeVorlesung);
            holder.name = (TextView) row.findViewById(R.id.veranstaltung);
            holder.prof = (TextView) row.findViewById(R.id.dozent);
            holder.room = (TextView) row.findViewById(R.id.raum);

            row.setTag(holder);
        }else{
            // old layout
            holder = (ViewHolder) row.getTag();
        }

        if(vPlanItem != null){
            holder.start.setText(vPlanItem.getStartTime());
            holder.end.setText(vPlanItem.getEndTime());
            holder.prof.setText(vPlanItem.getProfName());
            holder.name.setText(vPlanItem.getModulName());
            holder.room.setText(vPlanItem.getRoom());
            row.setBackgroundResource(R.color.white);
            if(isActive(vPlanItem.getId())){
                row.setBackgroundResource(R.color.jadehs_grey_muffled);
            }

            row.setOnLongClickListener(new VPlanOnLongClickListener(vPlanItem, isCustomVPlanShown));
        }

        return row;
    }

    public class ViewHolder{
        TextView start;
        TextView end;
        TextView name;
        TextView prof;
        TextView room;
    }

    public boolean isActive(long id){
        for(int i = 0; i<activeElements.size();i++)
            if(activeElements.get(i) == id){
                return true;
            }
        return false;
    }

    public ArrayList<VPlanItem> getvPlanItems() {
        return vPlanItems;
    }

    private class VPlanOnLongClickListener implements View.OnLongClickListener{
        VPlanItem vplanItem;
        boolean isCustomVPlanShown;

        public VPlanOnLongClickListener(VPlanItem item, boolean isCustomVPlanShown){
            this.vplanItem = item;
            this.isCustomVPlanShown = isCustomVPlanShown;;
        }

        @Override
        public boolean onLongClick(View v) {
            try {
                if (!this.isCustomVPlanShown) {
                    if(!v.isActivated()){
                        CustomVPlanDataSource customVPlanDataSource = new CustomVPlanDataSource(context);
                        customVPlanDataSource.open();

                        customVPlanDataSource.createCustomVPlanItem(this.vplanItem);
                        customVPlanDataSource.close();

                        v.setActivated(true);
                        activeElements.add(this.vplanItem.getId());

                        v.setBackgroundResource(R.color.jadehs_grey_muffled);
                        Toast.makeText(context, context.getString(R.string.added_to_vplan), Toast.LENGTH_LONG).show();
                    }else{
                        // item has not been touched yet, delete it
                        CustomVPlanDataSource customVPlanDataSource = new CustomVPlanDataSource(context);
                        customVPlanDataSource.open();

                        // remove from custom vplan
                        customVPlanDataSource.deleteCustomVPlanItem(this.vplanItem);
                        customVPlanDataSource.close();

                        v.setActivated(false);
                        activeElements.remove(this.vplanItem.getId());

                        v.setBackgroundResource(R.color.white);
                        Toast.makeText(context, context.getString(R.string.removed_from_vplan), Toast.LENGTH_LONG).show();
                    }
                } else {
                    if(!v.isActivated()){
                        Log.wtf("ERR", "Not activated yet!");
                        // item has not been touched yet, delete it
                        CustomVPlanDataSource customVPlanDataSource = new CustomVPlanDataSource(context);
                        customVPlanDataSource.open();

                        // remove from custom vplan
                        customVPlanDataSource.deleteCustomVPlanItem(this.vplanItem);
                        customVPlanDataSource.close();

                        v.setActivated(true);
                        activeElements.add(this.vplanItem.getId());

                        v.setBackgroundResource(R.color.jadehs_grey_muffled);
                        Toast.makeText(context, context.getString(R.string.removed_from_vplan), Toast.LENGTH_LONG).show();
                    }else{
                        Log.wtf("ERR", "Already activated!");
                        // item has already been deleted, reverse decision?
                        CustomVPlanDataSource customVPlanDataSource = new CustomVPlanDataSource(context);
                        customVPlanDataSource.open();

                        customVPlanDataSource.createCustomVPlanItem(this.vplanItem);
                        customVPlanDataSource.close();

                       v.setActivated(false);
                        activeElements.remove(this.vplanItem.getId());

                        v.setBackgroundResource(R.color.white);
                        Toast.makeText(context, context.getString(R.string.added_to_vplan), Toast.LENGTH_LONG).show();
                }
             }
            }catch (Exception ex){
                Log.wtf("ERR", "Something, Somewhere went horribly wrong D:!"); // @todoo edit this
            }
            return true;
        }
    }
}
