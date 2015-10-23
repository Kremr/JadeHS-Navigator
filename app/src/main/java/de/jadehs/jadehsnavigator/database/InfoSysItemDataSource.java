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
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.ArrayList;

import de.jadehs.jadehsnavigator.model.InfoSysItem;

/**
 * Created by re1015 on 22.07.2015.
 */
public class InfoSysItemDataSource {
    private static final String TAG = "INFOSYSITEMDATASOURCE";

    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private String[] allColumns = {dbHelper.COLUMN_ID,
                                   dbHelper.COLUMN_TITLE, dbHelper.COLUMN_DESCRIPTION,
                                   dbHelper.COLUMN_LINK, dbHelper.COLUMN_CREATOR,
                                   dbHelper.COLUMN_CREATED, dbHelper.COLUMN_FB};

    public final String DB_TABLE = dbHelper.TABLE_INFOSYSITEMS;

    public InfoSysItemDataSource(Context context){
        dbHelper = new DBHelper(context);
    }

    public void open() throws SQLException{
        database = dbHelper.getWritableDatabase();
    }

    public void close(){
        dbHelper.close();
    }

    public void createInfoSysItem(InfoSysItem infoSysItem){
        try{
            ContentValues values = new ContentValues();
            /* Sets values */
            values.put(dbHelper.COLUMN_TITLE, infoSysItem.getTitle());
            values.put(dbHelper.COLUMN_DESCRIPTION, infoSysItem.getDescription());
            values.put(dbHelper.COLUMN_LINK, infoSysItem.getLink());
            values.put(dbHelper.COLUMN_CREATOR, infoSysItem.getCreator());
            values.put(dbHelper.COLUMN_CREATED, infoSysItem.getCreated());
            values.put(dbHelper.COLUMN_FB, infoSysItem.getFB());

            this.database.insert(dbHelper.TABLE_INFOSYSITEMS, null, values);
            Log.wtf(TAG, "Created item in DB!");
        }catch (Exception ex){
            Log.wtf(TAG, "Couldn't create item!", ex);
        }
    }

    public void deleteInfoSysItem(InfoSysItem infoSysItem){
        long id = infoSysItem.getID();
        // @todo: debug-msg löschen
        //System.out.println("DEBUG: Deleted InfoSysItem: " + id);
        this.database.delete(dbHelper.TABLE_INFOSYSITEMS, dbHelper.COLUMN_ID + " = " + id, null);
    }

    public ArrayList<InfoSysItem> getAllInfoSysItems(){
        ArrayList<InfoSysItem> infoSysItems = new ArrayList<InfoSysItem>();

        Cursor cursor = database.query(dbHelper.TABLE_INFOSYSITEMS, allColumns, null, null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            InfoSysItem infoSysItem = cursorToInfoSysItem(cursor);
            infoSysItems.add(infoSysItem);
            cursor.moveToNext();
        }
        cursor.close();

        return infoSysItems;
    }

    public ArrayList<InfoSysItem>  getInfoSysItemsFromFB(int fb){
        ArrayList<InfoSysItem> infoSysItems = new ArrayList<InfoSysItem>();

        // limit the database query to max 15 entries sorted by the creation date
        Cursor cursor = database.query(dbHelper.TABLE_INFOSYSITEMS, allColumns, null, null, null, null,"created DESC", "15");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            InfoSysItem infoSysItem = cursorToInfoSysItem(cursor);
            // @todo: automatically remove old entries (1-2 months) or "tap to load all" entries button ...
            // ---
            // ---
            if(infoSysItem.getFB() == fb) {
                infoSysItems.add(infoSysItem);
            }
            cursor.moveToNext();
        }
        cursor.close();



        return infoSysItems;
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

    public InfoSysItem loadInfoSysItemByTitle(String title){
        String Query = "Select * from " + this.DB_TABLE + " where " + dbHelper.COLUMN_TITLE + " = '" + title + "'";
        Cursor cursor = this.database.rawQuery(Query, null);
        cursor.moveToFirst();
        if(cursor.getCount() <= 0){
            cursor.toString();
        }

        return cursorToInfoSysItem(cursor);
    }

    public InfoSysItem loadInfoSysItemByURL(String url){
        String Query = "Select * from " + this.DB_TABLE + " where " + dbHelper.COLUMN_LINK + " = '" + url + "'";
        Cursor cursor = this.database.rawQuery(Query, null);
        cursor.moveToFirst();
        if(cursor.getCount() <= 0){
            cursor.toString();
        }

        return cursorToInfoSysItem(cursor);
    }

    /**
     * Creates a InfoSysItem from the given database cursor
     *
     * @param cursor Database cursor
     * @return InfoSysItem Created Item
     */
    private InfoSysItem cursorToInfoSysItem(Cursor cursor){
        InfoSysItem infoSysItem = new InfoSysItem();
        try {
            infoSysItem.setID(cursor.getLong(0));
            infoSysItem.setTitle(cursor.getString(1));
            infoSysItem.setDescription(cursor.getString(2));
            infoSysItem.setLink(cursor.getString(3));
            infoSysItem.setCreator(cursor.getString(4));
            infoSysItem.setCreated(cursor.getString(5));
            infoSysItem.setFB(cursor.getInt(6));
        }catch (Exception ex){
            Log.wtf(TAG, "Err", ex);
        }

        return  infoSysItem;
    }
}
