package de.jadehs.jadehsnavigator.model;

import android.content.Context;
import android.util.Log;

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
    private String TAG = "Mensaplan";


    public Mensaplan(Context context) {
        this.context=context;
    }

    public ArrayList<ArrayList> parseMensaplan(){
        Long insertID;

        Preferences preference = new Preferences(context);
        CalendarHelper calendarWeekHelper = new CalendarHelper();
        Pattern pattern = Pattern.compile("([\\d]+,[\\d]+)");

        String priceText = "";
        String location = preference.getLocation();
        String iconText = "";



        Document doc = connect(location);
        if(doc == null) return null;
        Element table;
        Elements tables = doc.select("table[summary^=Wochenplan]");


        MensaplanDayDataSource mensaplanDayDataSource = new MensaplanDayDataSource(context);
        MensaplanMealDataSource mensaplanMealDataSource = new MensaplanMealDataSource(context);
        MensaplanMeal mensaplanMeal;
        MensaplanDay mensaplanDay;


        ArrayList<ArrayList> mensaplanArrayList = new ArrayList<>();
        ArrayList<MensaplanDay> mensaplanDaysCurrentWeek = new ArrayList<>();
        ArrayList<MensaplanDay> mensaplanDaysNextWeek = new ArrayList<>();

        try {
            mensaplanDayDataSource.open();
            mensaplanMealDataSource.open();

            //Bedeutung der einzelnen Table Rows und TD:
            //tr:eq(0 = Tag; 1 = Hauptgerichte; 2 = Zusatzessen/Pasta; 3 = Beilagen; 4 = Gemüse; 5 = Salate; 6 = Suppen; 7 = Desserts)
            //td:eq(1 = Montag; 2 = Dienstag; 3 = Mittwoch; 4 = Donnerstag; 5 = Freitag)


            for (int it = 0; it < 2; it++) {
                table = tables.get(it);

                for (int x = 1; x <= 5; x++) {
                    if (it == 0) {
                        mensaplanDay = new MensaplanDay(x, calendarWeekHelper.getWeekNumber() + it, it, location, calendarWeekHelper.getDateRightNow(false));
                        insertID = mensaplanDayDataSource.createMensaplanDAY(mensaplanDay);
                        mensaplanDaysCurrentWeek.add(mensaplanDay);
                        mensaplanDay.setId(insertID);

                    } else {
                        mensaplanDay = new MensaplanDay(x, calendarWeekHelper.getWeekNumber() + it, it, location, calendarWeekHelper.getDateRightNow(false));
                        insertID = mensaplanDayDataSource.createMensaplanDAY(mensaplanDay);
                        mensaplanDaysNextWeek.add(mensaplanDay);
                        mensaplanDay.setId(insertID);
                    }
                }
                int elems = (doc.select("table[summary^=Wochenplan] > tbody > tr").size()) / tables.size();

                //int i -> Tabellen Zeile
                //int j -> Tabellen Spalte

                for (int i = 1; i < elems; i++) {

                    Elements tableRows = table.select("tr:eq(" + i + ")");
                    // Iteration der Tage
                    for (int j = 0; j <= 5; j++) {
                        if (j == 0) {
                            priceText = parsePrice(tableRows.select("td:eq(" + j + ")").text(), pattern);
                        } else {
                            Elements mealsDay = tableRows.select("td:eq(" + j + ") .speise_eintrag");

                            // Iteration eines TDs/Divs
                            int breakPoint = 0;
                            if (mealsDay.size() == 2) {
                                breakPoint = 1;
                            }

                            for (int iter = 0; iter <= breakPoint; iter++) {
                                String mealText = "";
                                if (mealsDay.size() != 0) {
                                    Element meal = mealsDay.get(iter);
                                    mealText = meal.text();
                                    Elements icons = meal.select("img[title]");

                                    mensaplanMeal = new MensaplanMeal(mealText, i);
                                    if (!mensaplanMeal.isIconsSet()) {
                                        for (Element icon : icons) {
                                            iconText = icon.attr("title");
                                            mensaplanMeal.addToIconTitles(iconText);

                                        }
                                    }
                                    if (i > 1) {
                                        mensaplanMeal.setPrice(priceText);
                                    }
                                    mensaplanMeal.setIconsToDescription();

                                    if (it == 0) {
                                        mensaplanMeal.setDayID(mensaplanDaysCurrentWeek.get(j - 1).getId());
                                        insertID = mensaplanMealDataSource.createMensaplanMeal(mensaplanMeal);
                                        mensaplanMeal.setId(insertID);
                                        //TODO addToMeals ausgetauscht
                                        mensaplanDaysCurrentWeek.get(j - 1).addToMeals(mensaplanMeal);


                                    } else {
                                        mensaplanMeal.setDayID(mensaplanDaysNextWeek.get(j - 1).getId());
                                        insertID = mensaplanMealDataSource.createMensaplanMeal(mensaplanMeal);
                                        mensaplanMeal.setId(insertID);
                                        //TODO addToMeals ausgetauscht
                                        mensaplanDaysNextWeek.get(j - 1).addToMeals(mensaplanMeal);

                                    }
                                }
                            }

                        }

                    }
                }

            }

            mensaplanArrayList.add(mensaplanDaysCurrentWeek);
            mensaplanArrayList.add(mensaplanDaysNextWeek);
        } catch (SQLException e) {
            Log.wtf(TAG,"Methode: parseMensaplan",e);

        }
        Log.wtf(TAG,"parsing done");
        return mensaplanArrayList;
    }

    private String parsePrice(String data, Pattern pattern) {
        Matcher matcher = pattern.matcher(data);
        if(matcher.find())
        {
            return matcher.group(1)+"€";
        }
        return "";
    }
    private Document connect(String location) {
        Document doc = null;
        try {
            String url = context.getString(R.string.mensaplan_base_url);

            switch(location) {
                case "Wilhelmshaven":
                    url = url + context.getString(R.string.mensaplan_url_whv);
                    break;
                case "Elsfleth":
                    url = url + context.getString(R.string.mensaplan_url_els);
                    break;
                case "Oldenburg":
                    url = url+ context.getString(R.string.mensaplan_url_olb);
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
