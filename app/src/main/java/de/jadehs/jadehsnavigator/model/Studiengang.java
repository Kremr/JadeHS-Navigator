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
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import java.io.IOException;
import java.util.ArrayList;

import de.jadehs.jadehsnavigator.R;

public class Studiengang {
    private Context context;
    private ArrayList<StudiengangItem> studiengangList = null;

    private String url;
    private String baseUrl = "http://team.jade-hs.de/plan/index.php?action=ipc_courses&version=1.1&department=";
    private int fb;

    public Studiengang(Context context, int fb) {
        this.context = context;
        this.fb = fb;
        setURL(fb);
    }

    public ArrayList<StudiengangItem> parseStudiengang() {

        this.studiengangList = new ArrayList<StudiengangItem>();

        try {

            StudiengangItem studiengangItem;


            Log.i("PARSING", "Connecting to URL: " + this.url);

            Document doc = Jsoup.connect(url).timeout(5000).parser(Parser.xmlParser()).get();
            Log.i("PARSING", "Done Parsing");

            try {
                for (Element item : doc.select("dict")) {
                    String name = item.select("string").last().text();
                    // skip "Beginn KW...."
                    if(name.startsWith("Beginn")){
                        continue;
                    }
                    String id = item.select("string").first().text();
                    id = id.replace("#", "%23");
                    studiengangItem = new StudiengangItem(name, id);
                    this.studiengangList.add(studiengangItem);
                    Log.i("ITEM", "ADDED ITEM");
                }
            } catch (NullPointerException ex){
                Log.wtf("PARSING", "NPE", ex);
                //Toast.makeText(context, "Keine Studiengänge gefunden! Bitte versuche es später erneut!", Toast.LENGTH_LONG);
                // force abort
                return null;
            } catch (Exception e) {
                Log.wtf("PARSING", "ERR", e);
                //Toast.makeText(context, "Keine Studiengänge gefunden! Bitte versuche es später erneut!", Toast.LENGTH_LONG);
                // force abort
                return null;
            }


        } catch (IOException e) {
            Log.e("PARSE", "PARSING FAILED");
            e.printStackTrace();
        }
        Log.i("LIST", "FINISHED ADDING ITEMS");

        return studiengangList;
    }

    public String setURL (int fb) {
        /*
        switch(fb) {
            case 1: this.url = baseUrl + "%23SPLUS698158";
                    break;
            case 2: this.url = baseUrl + "%23SPLUS698157";
                break;
            case 3: this.url = baseUrl + "%23SPLUS698157";
                break;
            case 4: this.url = baseUrl + "%23SPLUS69815A";
                break;
            case 5: this.url = baseUrl + "%23SPLUS698154";
                break;
            case 6: this.url = baseUrl + "%23SPLUSB6A7F0";
                break;
        }
        */

        switch (fb){
            case 1:
                // MIT
                this.url = baseUrl + this.context.getString(R.string.infosys_url_MIT);
                break;
            case 2:
                // I
                this.url = baseUrl + this.context.getString(R.string.infosys_url_I);
                break;
            case 3:
                // W
                this.url = baseUrl + this.context.getString(R.string.infosys_url_W);
                break;
            case 4:
                // A
                this.url = baseUrl + this.context.getString(R.string.infosys_url_A);
                break;
            case 5:
                // BUG
                this.url = baseUrl + this.context.getString(R.string.infosys_url_BUA);
                break;
            case 6:
                // S
                this.url = baseUrl + this.context.getString(R.string.infosys_url_S);
                break;
        }

        return url;
    }
}
