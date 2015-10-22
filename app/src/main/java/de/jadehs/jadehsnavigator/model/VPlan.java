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
import android.content.SharedPreferences;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.util.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.jadehs.jadehsnavigator.database.VPlanItemDataSource;

public class VPlan {

    private Context context;
    private ArrayList<VPlanItem> vPlanItems = null;

    private String url;
    private int fb;
    private String weekOfYear;

    public VPlan(Context context, String url, int fb, String weekOfYear) {
        this.context = context;
        this.url = url;
        this.fb = fb;
        this.weekOfYear = weekOfYear;
    }

    public ArrayList<VPlanItem> parseVPlan() {

        this.vPlanItems = new ArrayList<VPlanItem>();
        int dayOfWeek = 1;
        String weekday = "";

        while (dayOfWeek <= 6) {
            try {
                VPlanItemDataSource vPlanItemDataSource = new VPlanItemDataSource(this.context);
                vPlanItemDataSource.open();

                VPlanItem vPlanItem;

                switch (dayOfWeek) {
                    case 1:
                        weekday = "MO";
                        break;
                    case 2:
                        weekday = "DI";
                        break;
                    case 3:
                        weekday = "MI";
                        break;
                    case 4:
                        weekday = "DO";
                        break;
                    case 5:
                        weekday = "FR";
                        break;
                    case 6:
                        weekday = "SA";
                        break;
                }

                String finalURL = this.url + dayOfWeek;
                Log.i("PARSING", "Connecting to URL " + finalURL);

                Document doc = Jsoup.connect(finalURL).timeout(8000).parser(Parser.xmlParser()).get();
                Log.i("PARSING", "Done Parsing");

                int counter = 0;

                for (Element item : doc.select("dict")) {

                    if (counter == 0 || counter == 1) {
                        counter++;
                    } else {
                        String start = item.select("date").get(1).text();
                        String end = item.select("date").get(2).text();
                        String title = item.select("string").get(1).text();
                        String prof = item.select("string").get(2).text();
                        String room = item.select("string").last().text();

                        // TODO Bessere Loesung implementieren
                        if (start.contains("08:14")) {
                            start = "08:15";
                            end = "09:45";
                        } else if (start.contains("10:14")) {
                            start = "10:15";
                            end = "11:45";
                        } else if (start.contains("12:14")) {
                            start = "12:15";
                            end = "13:45";
                        } else if (start.contains("14:14")) {
                            start = "14:15";
                            end = "15:45";
                        } else if (start.contains("15:59")) {
                            start = "16:00";
                            end = "17:30";
                        } else if (start.contains("17:44")) {
                            start = "17:45";
                            end = "19:15";
                        } else {
                            Date startDate = null;
                            Date endDate = null;
                            try {
                                startDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(start);
                                endDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(end);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            start = new SimpleDateFormat("HH:mm").format(startDate).toString();
                            end = new SimpleDateFormat("HH:mm").format(endDate).toString();
                        }

                        SharedPreferences sp = context.getSharedPreferences("JHSNAV_PREFS", Context.MODE_PRIVATE);
                        String studiengangID = sp.getString("StudiengangID", "");

                        vPlanItem = new VPlanItem(title, prof, room, start, end, weekday, studiengangID, this.fb, this.weekOfYear);

                        vPlanItemDataSource.createVPlanItem(vPlanItem);
                        this.vPlanItems.add(vPlanItem);

                        Log.i("ITEM", "ADDED ITEM");
                    }
                }
                dayOfWeek++;
                vPlanItemDataSource.close();
            } catch (IOException ioExceptione) {
                Log.e("Parse", "Parsing failed");
                ioExceptione.printStackTrace();
            } catch (SQLException e) {
                Log.wtf("DATABASE ERROR", e.getMessage());
                e.printStackTrace();
            }
            Log.i("LIST", "FINISHED ADDING ITEMS");
        }
        return vPlanItems;
    }

    public List<VPlanItem> getVPlanItem() {
        return this.vPlanItems;
    }
}