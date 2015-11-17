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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.jadehs.jadehsnavigator.R;
import de.jadehs.jadehsnavigator.model.RSSItem;

public class NewsAdapter extends BaseAdapter {
    private static final String TAG = "NewsAdapter";

    private Date date;
    Calendar cal = Calendar.getInstance();
    private Context context;
    private ArrayList<RSSItem> rssItems;

    public NewsAdapter(Context context, ArrayList<RSSItem> rssItems) {
        this.context = context;
        this.rssItems = rssItems;
    }

    @Override
    public int getCount() {
        return this.rssItems.size();
    }

    @Override
    public Object getItem(int position) {
        return this.rssItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.news_list_item, null);
        }

        /* Set all texts for a single item */
        TextView title = (TextView) convertView.findViewById(R.id.txtTitle);
        TextView txtDate = (TextView) convertView.findViewById(R.id.txtDate);

        String timestamp = this.rssItems.get(position).getCreated();
        String dateStr = "";
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            date = sdf2.parse(timestamp);
            cal.setTime(date);

            dateStr = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)) + "." + String.format("%02d", cal.get(Calendar.MONTH) + 1) + "." + cal.get(Calendar.YEAR) + "   " +
                    String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", cal.get(Calendar.MINUTE)) + " Uhr";
        }catch (Exception ex){
            Log.wtf(TAG, "Err", ex);
        }

        title.setText(this.rssItems.get(position).getTitle());
        txtDate.setText(dateStr);

        convertView.setOnClickListener(new RSSOnClickListener(this.rssItems.get(position)));

        return convertView;
    }

    private class RSSOnClickListener implements View.OnClickListener{
        RSSItem rssItem;
        public RSSOnClickListener(RSSItem item){
            this.rssItem = item;
        }

        @Override
        public void onClick(View v) {
            try{
                final String url = this.rssItem.getLink();
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context.getApplicationContext());
                    builder.setMessage(context.getString(R.string.dialog_open_website))
                            .setCancelable(true)
                            .setPositiveButton(context.getString(R.string.positive),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(final DialogInterface dialog, final int id) {
                                            try {
                                                /* Sets up an intent to open the given URL in the default web browser*/
                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                intent.setData(Uri.parse(url));
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                                context.startActivity(intent);
                                            } catch (Exception ex) {
                                                Log.wtf(TAG, "Failed to open website", ex);
                                            }
                                        }
                                    })
                            .setNegativeButton(context.getString(R.string.negative),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                final DialogInterface dialog,
                                                final int id) {
                                            dialog.dismiss();
                                        }
                                    });
                    final AlertDialog alert = builder.create();
                    alert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT); // Won't work without this line for some reason...
                    alert.show();
                }else{
                    Toast.makeText(context, context.getString(R.string.failed_to_open_website), Toast.LENGTH_LONG).show();
                }
            }catch (Exception ex){
                Log.wtf(TAG, "Failed to create OnClickListener", ex);
            }
        }
    }
}
