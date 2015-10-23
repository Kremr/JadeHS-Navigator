package de.jadehs.jadehsnavigator.model;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.jadehs.jadehsnavigator.R;
import de.jadehs.jadehsnavigator.database.MensaplanDayDataSource;
import de.jadehs.jadehsnavigator.database.MensaplanMealDataSource;
import de.jadehs.jadehsnavigator.util.CalendarHelper;
import de.jadehs.jadehsnavigator.util.Preferences;

public class Mensaplan  {

    private Context context;
    private Preferences preference;
    private CalendarHelper calendarWeekHelper = new CalendarHelper();
    private String location;
    private long insertID;

    private Elements tableRows;
    private Elements mealsDay;
    private Elements icons;
    private Element meal;
    private Elements tables;
    private Document doc;
    private MensaplanDayDataSource mensaplanDayDataSource;
    private MensaplanMealDataSource mensaplanMealDataSource;
    private ArrayList<ArrayList> mensaplanArrayList = new ArrayList<>();
    private ArrayList<MensaplanDay> mensaplanDaysCurrentWeek;
    private ArrayList<MensaplanDay> mensaplanDaysNextWeek;
    private MensaplanDay mensaplanDay;
    private MensaplanMeal mensaplanMeal;
    private String mealText;
    private Element table;
    private String priceText;
    private String iconText;
    private String url;
    private Pattern pattern;
    private Matcher matcher;
    private String TAG = "Mensaplan";




    public Mensaplan(Context context) {
            this.context=context;
            preference = new Preferences(context);
            location = preference.getLocation();


    }

    public ArrayList<ArrayList> parseMensaplan(){
        doc = connectMensaplan();
        tables = doc.select("table[summary^=Wochenplan]");
        
        mensaplanDayDataSource = new MensaplanDayDataSource(this.context);
        mensaplanMealDataSource = new MensaplanMealDataSource(this.context);

        try {
            mensaplanDayDataSource.open();
            mensaplanMealDataSource.open();

              /*

        Bedeutung der einzelnen Table Rows und TD:

        tr:eq(0) = Tag
        tr:eq(1) = Hauptgerichte
        tr:eq(2) = Zusatzessen/Pasta
        tr:eq(3) = Beilagen
        tr:eq(4) = Gemüse
        tr:eq(5) = Salate
        tr:eq(6) = Suppen
        tr:eq(7) = Desserts

        td:eq(1) = Montag
        td:eq(2) = Dienstag
        td:eq(3) = Mittwoch
        td:eq(4) = Donnerstag
        td:eq(5) = Freitag*/

            mensaplanDaysCurrentWeek = new ArrayList<>();
            mensaplanDaysNextWeek = new ArrayList<>();




            for(int it = 0; it<2; it++) {
                table = tables.get(it);

                for (int x = 1; x <= 5; x++) {
                    if (it == 0) {
                        mensaplanDay = new MensaplanDay(x, calendarWeekHelper.getWeekNumber(), 0, location, calendarWeekHelper.getDateRightNow(false));
                        insertID = mensaplanDayDataSource.createMensaplanDAY(mensaplanDay);
                        mensaplanDaysCurrentWeek.add(mensaplanDay);
                        mensaplanDay.setId(insertID);

                    } else {
                        mensaplanDay = new MensaplanDay(x, calendarWeekHelper.getWeekNumber() + 1, 1, location, calendarWeekHelper.getDateRightNow(false));
                        insertID = mensaplanDayDataSource.createMensaplanDAY(mensaplanDay);
                        mensaplanDaysNextWeek.add(mensaplanDay);
                        mensaplanDay.setId(insertID);
                    }
                }
                int elems =(doc.select("table[summary^=Wochenplan] > tbody > tr").size())/tables.size();

                //int i -> Tabellen Zeile
                //int j -> Tabellen Spalte

                for (int i = 1; i < elems; i++) {

                    tableRows = table.select("tr:eq(" + i + ")");
                    // Iteration der Tage
                    for (int j = 0; j <= 5; j++) {
                        if(j==0) {
                            //TODO Price parsen
                            priceText= tableRows.select("td:eq(0)").text();
                            pattern = Pattern.compile("([\\d]+,[\\d]+)");
                            matcher = pattern.matcher(priceText);
                            if(matcher.find())
                            {
                                 priceText = matcher.group(1)+"€";
                            } else {
                                priceText = "";
                            }




                        } else {
                            mealsDay = tableRows.select("td:eq(" + j + ") .speise_eintrag");

                            // Iteration eines TDs/Divs
                            int breakPoint=0;
                            if(mealsDay.size()==2) {
                                breakPoint = 1;
                            }

                            for (int iter=0;iter<=breakPoint;iter++) {
                                //Log.wtf(TAG, location +" + " +i);
                                mealText = "";

                                //Log.wtf(TAG,mealsDay.get(iter).html());
                                if(mealsDay.size() != 0) {
                                    meal = mealsDay.get(iter);
                                    mealText = meal.text();
                                    icons = meal.select("img[title]");

                                    mensaplanMeal = new MensaplanMeal(mealText, i);
                                    //Log.wtf("MensaplanMeal","Item Created:"+mealText +" "+ i);
                                    if(!mensaplanMeal.isIconsSet()) {
                                    for (Element icon : icons) {
                                        iconText = icon.attr("title");
                                        mensaplanMeal.addToIconTitles(iconText);

                                    } }
                                    if(i>1) {

                                        mensaplanMeal.setPrice(priceText);
                                    }
                                    mensaplanMeal.setIconsToDescription();
                                } else {
                                    mensaplanMeal = new MensaplanMeal(mealText, i);
                                }

                                if (it == 0) {
                                    mensaplanMeal.setDayID(mensaplanDaysCurrentWeek.get(j - 1).getId());
                                    insertID = mensaplanMealDataSource.createMensaplanMeal(mensaplanMeal);
                                    mensaplanMeal.setId(insertID);
                                    mensaplanDaysCurrentWeek.get(j - 1).addToMeals(mensaplanMeal);


                                } else {
                                    mensaplanMeal.setDayID(mensaplanDaysNextWeek.get(j - 1).getId());
                                    insertID = mensaplanMealDataSource.createMensaplanMeal(mensaplanMeal);
                                    mensaplanMeal.setId(insertID);
                                    mensaplanDaysNextWeek.get(j - 1).addToMeals(mensaplanMeal);

                                }
                            }

                        }

                    }
                }

            }

            mensaplanArrayList.add(mensaplanDaysCurrentWeek);
            mensaplanArrayList.add(mensaplanDaysNextWeek);
        } catch (SQLException e) {
            Log.wtf("ParseMensaplan","Fehler aufgetreten");
            e.printStackTrace();
        }
        return mensaplanArrayList;
    }


    public Document connectMensaplan() {
        try {
            url = context.getString(R.string.mensaplan_base_url);
            switch(location) {
                case "Wilhelmshaven":
                    url= url+ context.getString(R.string.mensaplan_url_whv);
                    break;
                case "Elsfleth":
                    url= url + context.getString(R.string.mensaplan_url_els);
                    break;
                case "Oldenburg":
                    url = url + context.getString(R.string.mensaplan_url_olb);
                    break;
            }
            doc = Jsoup.connect(url).timeout(5000).get();
        } catch (SocketTimeoutException ex){
            Log.wtf("Connection:", "Timeout.",ex);
        } catch (IOException ex) {
            Log.wtf("Connection:", "Fehler beim verbinden zur Website.",ex);
        }
        return doc;
    }
}
