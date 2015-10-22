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

import de.jadehs.jadehsnavigator.model.VPlanItem;

/**
 * Created by Nico on 16.08.2015.
 */
public class VPlanItemDataSource {

    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private String[] allColumns = {dbHelper.COLUMN_VPLAN_ID, dbHelper.COLUMN_VPLAN_TITLE,
            dbHelper.COLUMN_VPLAN_PROF, dbHelper.COLUMN_VPLAN_ROOM,
            dbHelper.COLUMN_VPLAN_START, dbHelper.COLUMN_VPLAN_END,
            dbHelper.COLUMN_VPLAN_DAY_OF_WEEK, dbHelper.COLUMN_VPLAN_STUDIENGANG_ID,
            dbHelper.COLUMN_VPLAN_FB, dbHelper.COLUMN_VPLAN_WEEK_OF_YEAR};

    public final String DB_TABLE = dbHelper.TABLE_VPLANITEMS;

    public VPlanItemDataSource(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void createVPlanItem(VPlanItem vPlanItem) {
        //if (!this.exists(dbHelper.COLUMN_VPLAN_TITLE, vPlanItem.getModulName()))
        if (!this.existsDetail(dbHelper.COLUMN_VPLAN_TITLE, vPlanItem.getModulName(),
                dbHelper.COLUMN_VPLAN_ROOM, vPlanItem.getRoom(),
                dbHelper.COLUMN_VPLAN_START, vPlanItem.getStartTime(),
                dbHelper.COLUMN_VPLAN_END, vPlanItem.getEndTime(),
                dbHelper.COLUMN_VPLAN_DAY_OF_WEEK, vPlanItem.getDayOfWeek(),
                dbHelper.COLUMN_VPLAN_WEEK_OF_YEAR, vPlanItem.getWeekOfYear())) {

            ContentValues values = new ContentValues();

            // Werte einsetzen
            values.put(dbHelper.COLUMN_VPLAN_TITLE, vPlanItem.getModulName());
            values.put(dbHelper.COLUMN_VPLAN_PROF, vPlanItem.getProfName());
            values.put(dbHelper.COLUMN_VPLAN_ROOM, vPlanItem.getRoom());
            values.put(dbHelper.COLUMN_VPLAN_START, vPlanItem.getStartTime());
            values.put(dbHelper.COLUMN_VPLAN_END, vPlanItem.getEndTime());
            values.put(dbHelper.COLUMN_VPLAN_DAY_OF_WEEK, vPlanItem.getDayOfWeek());
            values.put(dbHelper.COLUMN_VPLAN_STUDIENGANG_ID, vPlanItem.getStudiengangID());
            values.put(dbHelper.COLUMN_VPLAN_FB, vPlanItem.getFb());
            values.put(dbHelper.COLUMN_VPLAN_WEEK_OF_YEAR, vPlanItem.getWeekOfYear());

            this.database.insert(dbHelper.TABLE_VPLANITEMS, null, values);
        } else {
            Log.wtf("DEBUG: ALREADY EXISTS", "Item already exists");
        }
    }


    public void deleteVPlanItem(VPlanItem vPlanItem) {
        long id = vPlanItem.getId();
        this.database.delete(dbHelper.TABLE_VPLANITEMS, dbHelper.COLUMN_VPLAN_ID + " = " + id, null);
    }

    public boolean existsDetail(String fieldName, String fieldValue,
                                String fieldRoom, String valueRoom,
                                String fieldStart, String valueStart,
                                String fieldEnd, String valueEnd,
                                String fieldDayOfWeek, String valueDayOfWeek,
                                String fieldWeekOfYear, String valueWeekOfYear) {
        String Query = "Select * from " + this.DB_TABLE + " where " + fieldName + " = '" + fieldValue + "' and "
                + fieldRoom + " = '" + valueRoom + "' and "
                + fieldStart + " = '" + valueStart + "' and "
                + fieldEnd + " = '" + valueEnd + "' and "
                + fieldDayOfWeek + " = '" + valueDayOfWeek + "' and "
                + fieldWeekOfYear + " = '" + valueWeekOfYear + "'";

        Cursor cursor = this.database.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public boolean exists(String fieldName, String fieldValue) {
        String Query = "Select * from " + this.DB_TABLE + " where " + fieldName + " = '" + fieldValue + "'";
        Cursor cursor = this.database.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public ArrayList<VPlanItem> getVPlanItem(String studiengang, String weekOfYear) {
        ArrayList<VPlanItem> vPlanItems = new ArrayList<VPlanItem>();

        Cursor cursor = database.query(dbHelper.TABLE_VPLANITEMS, allColumns, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            VPlanItem vPlanItem = cursorToVPlanItem(cursor);
            if (vPlanItem.getStudiengangID().equals(studiengang) && vPlanItem.getWeekOfYear().equals(weekOfYear)) {
                vPlanItems.add(vPlanItem);
            }
            cursor.moveToNext();
        }

        cursor.close();
        return vPlanItems;
    }


    private VPlanItem cursorToVPlanItem(Cursor cursor) {
        VPlanItem vPlanItem = new VPlanItem();

        vPlanItem.setId(cursor.getLong(0));
        vPlanItem.setModulName(cursor.getString(1));
        vPlanItem.setProfName(cursor.getString(2));
        vPlanItem.setRoom(cursor.getString(3));
        vPlanItem.setStartTime(cursor.getString(4));
        vPlanItem.setEndTime(cursor.getString(5));
        vPlanItem.setDayOfWeek(cursor.getString(6));
        vPlanItem.setStudiengangID(cursor.getString(7));
        vPlanItem.setFb(cursor.getInt(8));
        vPlanItem.setWeekOfYear(cursor.getString(9));

        return vPlanItem;
    }
}
