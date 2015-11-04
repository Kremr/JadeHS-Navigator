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

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.channels.CancelledKeyException;
import java.util.ArrayList;

import de.jadehs.jadehsnavigator.R;
import de.jadehs.jadehsnavigator.database.CustomVPlanDataSource;
import de.jadehs.jadehsnavigator.fragment.VorlesungsplanFragment;
import de.jadehs.jadehsnavigator.model.VPlanItem;
import de.jadehs.jadehsnavigator.util.CalendarHelper;

/**
 * Created by Nico on 14.08.2015.
 */
public class VPlanPagerAdapter extends PagerAdapter {

    private static final int NUM_OF_TABS = 6;
    private Context context;
    private ArrayList<VPlanItem> vPlanItems;
    private CalendarHelper calendarHelper = new CalendarHelper();
    private String kw;
    private boolean isCustomVPlanShown = false;

    public VPlanPagerAdapter(Context context, ArrayList<VPlanItem> vPlanItems, String kw) {
        this.context = context;
        this.vPlanItems = vPlanItems;
        this.kw = kw;
        //int kw = vPlanItems.get(0).get
    }

    @Override
    public int getCount() {
        return NUM_OF_TABS;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return object == view;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String dayOfWeek = "";
        switch (position + 1) {
            case 1:
                dayOfWeek = context.getString(R.string.strWeekdayMonday);
                break;
            case 2:
                dayOfWeek = context.getString(R.string.strWeekdayTuesday);
                break;
            case 3:
                dayOfWeek = context.getString(R.string.strWeekdayWednesday);
                break;
            case 4:
                dayOfWeek = context.getString(R.string.strWeekdayThursday);
                break;
            case 5:
                dayOfWeek = context.getString(R.string.strWeekdayFriday);
                break;
            case 6:
                dayOfWeek = context.getString(R.string.strWeekdaySaturday);
                break;
        }
        return dayOfWeek;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = null;

        final ArrayList<VPlanItem> vPlanItemsWeekday = new ArrayList<VPlanItem>();

        if (getPageTitle(position).equals(context.getString(R.string.strWeekdayMonday))) {
            for (VPlanItem item : this.vPlanItems) {
                if (item.getDayOfWeek().equals(context.getString(R.string.strWeekdayMonday)))
                    vPlanItemsWeekday.add(item);
            }
        } else if (getPageTitle(position).equals(context.getString(R.string.strWeekdayTuesday))) {
            for (VPlanItem item : this.vPlanItems) {
                if (item.getDayOfWeek().equals(context.getString(R.string.strWeekdayTuesday)))
                    vPlanItemsWeekday.add(item);
            }
        } else if (getPageTitle(position).equals(context.getString(R.string.strWeekdayWednesday))) {
            for (VPlanItem item : this.vPlanItems) {
                if (item.getDayOfWeek().equals(context.getString(R.string.strWeekdayWednesday)))
                    vPlanItemsWeekday.add(item);
            }
        } else if (getPageTitle(position).equals(context.getString(R.string.strWeekdayThursday))) {
            for (VPlanItem item : this.vPlanItems) {
                if (item.getDayOfWeek().equals(context.getString(R.string.strWeekdayThursday)))
                    vPlanItemsWeekday.add(item);
            }
        } else if (getPageTitle(position).equals(context.getString(R.string.strWeekdayFriday))) {
            for (VPlanItem item : this.vPlanItems) {
                if (item.getDayOfWeek().equals(context.getString(R.string.strWeekdayFriday)))
                    vPlanItemsWeekday.add(item);
            }
        } else if (getPageTitle(position).equals(context.getString(R.string.strWeekdaySaturday))) {
            for (VPlanItem item : this.vPlanItems) {
                if (item.getDayOfWeek().equals(context.getString(R.string.strWeekdaySaturday)))
                    vPlanItemsWeekday.add(item);
            }
        }
        
        /*
            Innheralb des Tabs wird zunaechst die Listview erstellt.
            Die Listviewitems werden dann mit Hilfe des VPlanAdapters erstellt.
         */
        view = layoutInflater.inflate(R.layout.vplan_list, container, false);
        container.addView(view);

        ListView lv = (ListView) view.findViewById(R.id.list_studiengang);
        TextView lastUpdateVPlan = (TextView) view.findViewById(R.id.textViewFooter);

        if (!isCustomVPlanShown)
            lastUpdateVPlan.setText("Plan für KW: " + this.kw + "  |  Abgerufen am: " + calendarHelper.getDateRightNow(true));
        else
            lastUpdateVPlan.setText("Eigener Vorlesungsplan");

        final VPlanAdapter vPlanAdapter = new VPlanAdapter(context, vPlanItemsWeekday);

        lv.setAdapter(vPlanAdapter);
        lv.setLongClickable(true);

        // if(!vPlanAdapter.isCustomVPlanShown()) {
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                if (!isCustomVPlanShown) {
                    VPlanItem vPlanItem = new VPlanItem(vPlanItemsWeekday.get(position).getModulName(),
                            vPlanItemsWeekday.get(position).getProfName(),
                            vPlanItemsWeekday.get(position).getRoom(),
                            vPlanItemsWeekday.get(position).getStartTime(),
                            vPlanItemsWeekday.get(position).getEndTime(),
                            vPlanItemsWeekday.get(position).getDayOfWeek(),
                            vPlanItemsWeekday.get(position).getStudiengangID(),
                            vPlanItemsWeekday.get(position).getFb(),
                            vPlanItemsWeekday.get(position).getWeekOfYear());

                    try {
                        CustomVPlanDataSource customVPlanDataSource = new CustomVPlanDataSource(context);
                        customVPlanDataSource.open();

                        customVPlanDataSource.createCustomVPlanItem(vPlanItem);
                        customVPlanDataSource.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(context, "Zu eigenem Vorlesungsplan hinzugefügt!", Toast.LENGTH_LONG).show();
                } else {
                    VPlanItem vPlanItem = vPlanItemsWeekday.get(position);

                    try {
                        CustomVPlanDataSource customVPlanDataSource = new CustomVPlanDataSource(context);
                        customVPlanDataSource.open();

                        customVPlanDataSource.deleteCustomVPlanItem(vPlanItem);
                        customVPlanDataSource.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    parent.getChildAt(position).setBackgroundResource(R.color.jadehs_grey_muffled);
                    parent.getChildAt(position).setLongClickable(false);
                    Toast.makeText(context, "Aus eigenem Vorlesungsplan entfernt!", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });

        //lastUpdateMensa.setText("Plan für KW: " + mensaplanDays.get(position).getWeekNumber() + "  |  Abgerufen am: " + mensaplanDays.get(position).getCreated());

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public boolean isCustomVPlanShown() {
        return isCustomVPlanShown;
    }

    public void setIsCustomVPlanShown(boolean isCustomVPlanShown) {
        this.isCustomVPlanShown = isCustomVPlanShown;
    }
}
