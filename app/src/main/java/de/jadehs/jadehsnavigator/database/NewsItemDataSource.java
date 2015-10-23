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
package de.jadehs.jadehsnavigator.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.SQLException;
import java.util.ArrayList;

import de.jadehs.jadehsnavigator.model.InfoSysItem;
import de.jadehs.jadehsnavigator.model.RSSItem;
import de.jadehs.jadehsnavigator.model.RSSOrigin;

/**
 * Created by re1015 on 22.07.2015.
 */
public class NewsItemDataSource {
    private static final String TAG = "NewsItemDataSource";

    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private String[] allColumns = {dbHelper.COLUMN_ID,
                                   dbHelper.COLUMN_TITLE, dbHelper.COLUMN_DESCRIPTION,
                                   dbHelper.COLUMN_LINK, dbHelper.COLUMN_ORIGIN,
                                   dbHelper.COLUMN_CREATED};

    public final String DB_TABLE = dbHelper.TABLE_NEWS;

    public NewsItemDataSource(Context context){
        dbHelper = new DBHelper(context);
    }

    public void open() throws SQLException{
        database = dbHelper.getWritableDatabase();
    }

    public void close(){
        dbHelper.close();
    }

    public void createNewsItem(RSSItem rssItem){
        try{
            ContentValues values = new ContentValues();
            /* Sets values */
            values.put(dbHelper.COLUMN_TITLE, rssItem.getTitle());
            values.put(dbHelper.COLUMN_DESCRIPTION, rssItem.getDescription());
            values.put(dbHelper.COLUMN_LINK, rssItem.getLink());
            values.put(dbHelper.COLUMN_ORIGIN, rssItem.getOrigin().getTitle());
            values.put(dbHelper.COLUMN_CREATED, rssItem.getCreated());

            this.database.insert(DB_TABLE, null, values);
            Log.wtf(TAG, "Created item in DB!");
        }catch (Exception ex){
            Log.wtf(TAG, "Couldn't create item!", ex);
        }
    }

    /*
    public void deleteInfoSysItem(InfoSysItem infoSysItem){
        long id = infoSysItem.getID();
        // @todo: debug-msg löschen
        //System.out.println("DEBUG: Deleted InfoSysItem: " + id);
        this.database.delete(DB_TABLE, dbHelper.COLUMN_ID + " = " + id, null);
    }
    */

    public ArrayList<RSSItem> getAllRSSItems(){
        ArrayList<RSSItem> rssItems = new ArrayList<RSSItem>();

        // limit the entries by 200
        Cursor cursor = database.query(DB_TABLE, allColumns, null, null, null, null, "created DESC", "200");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            RSSItem rssItem = cursorToRSSItem(cursor);
            rssItems.add(rssItem);
            cursor.moveToNext();
        }
        cursor.close();

        return rssItems;
    }

    public ArrayList<RSSItem>  getRSSItemsFromOrigin(String origin){
        ArrayList<RSSItem> rssItems = new ArrayList<RSSItem>();

        Cursor cursor = database.query(DB_TABLE, allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            RSSItem rssItem = cursorToRSSItem(cursor);
            if(rssItem.getOrigin().getTitle().equals(origin)){
                rssItems.add(rssItem);
            }
            cursor.moveToNext();
        }
        cursor.close();

        return rssItems;
    }

    public boolean exists(String fieldName, String fieldValue) {
        String Query = "Select title from " + this.DB_TABLE + " where " + fieldName + " = '" + fieldValue + "'";
        Cursor cursor = this.database.rawQuery(Query, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();

        return true;
    }


    public RSSItem loadRSSItemByTitle(String title){
        String Query = "Select * from " + this.DB_TABLE + " where " + dbHelper.COLUMN_TITLE + " = '" + title + "'";
        Cursor cursor = this.database.rawQuery(Query, null);
        cursor.moveToFirst();
        if(cursor.getCount() <= 0){
            cursor.toString();
        }

        return cursorToRSSItem(cursor);
    }

    public RSSItem loadRSSItemByURL(String link){
        String Query = "Select * from " + this.DB_TABLE + " where " + dbHelper.COLUMN_LINK + " = '" + link + "'";
        Cursor cursor = this.database.rawQuery(Query, null);
        cursor.moveToFirst();
        if(cursor.getCount() <= 0){
            cursor.toString();
        }

        return cursorToRSSItem(cursor);
    }

    /*
    public InfoSysItem loadInfoSysItemByURL(String url){
        String Query = "Select * from " + this.DB_TABLE + " where " + dbHelper.COLUMN_LINK + " = '" + url + "'";
        Cursor cursor = this.database.rawQuery(Query, null);
        cursor.moveToFirst();
        if(cursor.getCount() <= 0){
            cursor.toString();
        }

        return cursorToRSSItem(cursor);
    }
    */

    /**
     * Creates a InfoSysItem from the given database cursor
     *
     * @param cursor Database cursor
     * @return InfoSysItem Created Item
     */
    private RSSItem cursorToRSSItem(Cursor cursor){
        RSSItem rssItem = new RSSItem();
        try {
            rssItem.setID(cursor.getLong(0));
            rssItem.setTitle(cursor.getString(1));
            rssItem.setDescription(cursor.getString(2));
            rssItem.setLink(cursor.getString(3));
            // ehhhh
            rssItem.setOrigin(new RSSOrigin(0,cursor.getString(4),null));
            rssItem.setCreated(cursor.getString(5));
        }catch (Exception ex){
            Log.wtf(TAG, "Err", ex);
        }

        return  rssItem;
    }
}
