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

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import de.jadehs.jadehsnavigator.R;
import de.jadehs.jadehsnavigator.model.MensaplanDay;
import de.jadehs.jadehsnavigator.model.MensaplanMeal;
import de.jadehs.jadehsnavigator.model.RSSItem;
import de.jadehs.jadehsnavigator.util.CalendarHelper;
import de.jadehs.jadehsnavigator.util.Preferences;

public class NewsPagerAdapter extends PagerAdapter {
    private static final String TAG = "NEWSPAGERADAPTER";

    private Context context;
    private static final int NUM_OF_TABS = 5;
    private CalendarHelper calendarHelper;

    private ArrayList<RSSItem> rssItems;

    public NewsPagerAdapter(Context context, ArrayList<RSSItem> rssItems) {
        this.context = context;
        this.rssItems = rssItems;

        calendarHelper = new CalendarHelper();
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
        String originID = "";
        switch (position + 1) {
            case 1:
                originID = context.getString(R.string.origin_1);
                break;
            case 2:
                originID = context.getString(R.string.origin_2);
                break;
            case 3:
                originID = context.getString(R.string.origin_3);
                break;
            case 4:
                originID = context.getString(R.string.origin_4);
                break;
            case 5:
                originID = context.getString(R.string.origin_5);
                break;
        }
        return originID;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view;

        /* Sets up content of the current page according to the position */
        ArrayList<RSSItem> newsItems = new ArrayList<RSSItem>();
        if(getPageTitle(position).equals(context.getString(R.string.origin_1))){
            for(RSSItem item : this.rssItems){
                if(item.getOrigin().getTitle().equals(context.getString(R.string.origin_1))){
                    newsItems.add(item);
                }
            }
        }else if(getPageTitle(position).equals(context.getString(R.string.origin_2))){
            for(RSSItem item : this.rssItems){
                if(item.getOrigin().getTitle().equals(context.getString(R.string.origin_2))){
                    newsItems.add(item);
                }
            }
        }else if(getPageTitle(position).equals(context.getString(R.string.origin_3))){
            for(RSSItem item : this.rssItems){
                if(item.getOrigin().getTitle().equals(context.getString(R.string.origin_3))){
                    newsItems.add(item);
                }
            }
        }else if(getPageTitle(position).equals(context.getString(R.string.origin_4))){
            for(RSSItem item : this.rssItems){
                if(item.getOrigin().getTitle().equals(context.getString(R.string.origin_4))){
                    newsItems.add(item);
                }
            }
        }else if(getPageTitle(position).equals(context.getString(R.string.origin_5))){
            for(RSSItem item : this.rssItems){
                if(item.getOrigin().getTitle().equals(context.getString(R.string.origin_5))){
                    newsItems.add(item);
                }
            }
        }

        view = layoutInflater.inflate(R.layout.news_list, container, false);
        container.addView(view);

        /* Creates a ListView with the correct News Items */
        ListView lv = (ListView) view.findViewById(R.id.list_news);
        NewsAdapter newsAdapter = new NewsAdapter(this.context, newsItems);

        lv.setAdapter(newsAdapter);

        TextView lastUpdateVPlan = (TextView) view.findViewById(R.id.textViewFooter);

        lastUpdateVPlan.setText("Abgerufen am: " + calendarHelper.getDateRightNow(true));

        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}