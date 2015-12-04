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
package de.jadehs.jadehsnavigator.model;

import android.content.Context;
import android.util.Log;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.jadehs.jadehsnavigator.database.NewsItemDataSource;

public class RSS {
    private final String TAG = "RSS";

    private Context context;
    private String lastUpdate;
    private ArrayList<RSSItem> rssItems = null;

    private RSSOrigin origin;
    private ArrayList<RSSOrigin> origins;

    public RSS(Context context, RSSOrigin origin){
        this.context = context;
        this.origin = origin;
    }

    public RSS(Context context, ArrayList<RSSOrigin> origins){
        this.context = context;
        this.origins = origins;
    }

    public boolean readAll(){
        try {
            this.rssItems = new ArrayList<RSSItem>();
            NewsItemDataSource newsItemDataSource = new NewsItemDataSource(this.context);
            newsItemDataSource.open();


            for(RSSOrigin origin : this.origins) {
                Log.wtf(TAG, "PARSING: " + origin.getTitle());

                RSSItem rssItem;

                Document doc = Jsoup.connect(origin.getUrl())
                        .parser(Parser.xmlParser())
                        .get();

                for (Element item : doc.select("item")) {
                    String link = item.select("link").first().text();

                    if (newsItemDataSource.exists("link", link)) {
                        // Parsed entry already exists. Use that one.
                        //Log.wtf("ITEM", "ITEM: " + link + " ALREADY EXISTS!");
                        //infoSysItem = infoSysItemDataSource.loadInfoSysItemByTitle(title);
                        rssItem = newsItemDataSource.loadRSSItemByURL(link);
                    } else {
                        String title = item.select("title").first().text();
                        String description = item.select("description").first().text();
                        String pubDate = item.select("pubDate").first().text();

                        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
                        Date date;
                        String dateStr = "";
                        try {
                            date = sdf.parse(pubDate);
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            // Set date string
                            Timestamp timestamp = new Timestamp(cal.getTimeInMillis());
                            dateStr = timestamp.toString();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        rssItem = new RSSItem(title, description, link, origin, dateStr);
                        newsItemDataSource.createNewsItem(rssItem);
                    }
                    this.rssItems.add(rssItem);
                    }
                }
            } catch (HttpStatusException httpEx) {
                Log.wtf(TAG, "SERVER UNREACHABLE", httpEx);
            } catch (Exception ex) {
                Log.wtf(TAG, "PARSING FAILED", ex);
            }

        return true;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public ArrayList<RSSItem> getRssItems() {
        return rssItems;
    }

    public void setRssItems(ArrayList<RSSItem> rssItems) {
        this.rssItems = rssItems;
    }

    public RSSOrigin getOrigin() {
        return origin;
    }

    public void setOrigin(RSSOrigin origin) {
        this.origin = origin;
    }
}
