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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.jadehs.jadehsnavigator.database.InfoSysItemDataSource;
import de.jadehs.jadehsnavigator.util.CalendarHelper;

public class InfoSys {
    final String TAG = "InfoSys";

    private Context context;
    private String lastUpdate;
    private ArrayList<InfoSysItem> infoSysItems = null;
    private CalendarHelper calendarHelper;
    private  Date date;

    private String url;
    private int fb;

    public InfoSys(Context context, String url, int fb){
        this.context = context;
        this.url = url;
        this.fb = fb;
        this.calendarHelper= new CalendarHelper();
    }

    public ArrayList<InfoSysItem> parse(){
        this.infoSysItems = new ArrayList<InfoSysItem>();

        try {
            InfoSysItemDataSource infoSysItemDataSource = new InfoSysItemDataSource(this.context);
            infoSysItemDataSource.open();

            InfoSysItem infoSysItem;

            Document doc = Jsoup.connect(this.url)
                    .parser(Parser.xmlParser())
                    .get();
            Log.i("PARSING TAG", "Done Parsing");

            for (Element item : doc.select("item")) {
                String link = item.select("link").first().text();

                if(infoSysItemDataSource.exists("link", link)) {
                    // Parsed entry already exists. Use that one.
                    //Log.wtf("ITEM", "ITEM: " + link + " ALREADY EXISTS!");
                    infoSysItem = infoSysItemDataSource.loadInfoSysItemByURL(link);
                }else {
                    // Parsed entry doesn't already exists. Create a new one.
                    //Log.wtf("ITEM", "ITEM: " + link + " IS NEW!");
                    String title = item.select("title").first().text();
                    String description = item.select("description").first().text();
                    String detailDescription = "";
                    try {
                        Document detailDoc = Jsoup.connect(link).parser(Parser.htmlParser()).get();
                        detailDescription = detailDoc.body().getElementsByClass("newstext").get(1).html();
                    } catch (Exception ex) {
                        Log.wtf(TAG, "URL failed to load", ex);
                    }
                    String fullDescription = description + detailDescription;

                    String creator = item.select("dc|creator").first().text();
                    String created = item.select("dc|date").first().text();

                    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
                    String dateStr = "";
                    try {
                        date = sdf2.parse(created);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        // Set date string (oh and btw Calendar.MONTH returns 0-11 for some reason. because there is a 0th month I guess. fucking java)
                        Timestamp timestamp = new Timestamp(cal.getTimeInMillis());
                        dateStr = timestamp.toString();

                    } catch (Exception ex) {
                        Log.wtf(TAG, "Err", ex);
                    }

                    infoSysItem = new InfoSysItem(title, fullDescription, link, creator, dateStr, this.fb);
                    infoSysItemDataSource.createInfoSysItem(infoSysItem);
                }

                this.infoSysItems.add(infoSysItem);

                Log.wtf(TAG, "ADDED ITEM");
            }
            infoSysItemDataSource.close();
        }catch (IOException e){
            Log.wtf(TAG, "Parsing failed", e);
            e.printStackTrace();
        }catch (SQLException e){
            Log.wtf(TAG, "SQL failed", e);
            e.printStackTrace();
        }
        Log.wtf(TAG, "FINISHED ADDING ITEMS");

        return infoSysItems;
    }

    public List<InfoSysItem> getInfoSysItems(){
        return this.infoSysItems;
    }
}
