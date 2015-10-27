package de.jadehs.jadehsnavigator.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import de.jadehs.jadehsnavigator.model.VPlanItem;

/**
 * Created by Nico on 30.09.2015.
 */
public class CustomVPlanDataSource {

    private SQLiteDatabase database;
    private DBHelper dbHelper;

    private String[] allColumns = {DBHelper.COLUMN_CUSTOM_VPLAN_ID, DBHelper.COLUMN_CUSTOM_VPLAN_TITLE,
            DBHelper.COLUMN_CUSTOM_VPLAN_PROF, DBHelper.COLUMN_CUSTOM_VPLAN_ROOM,
            DBHelper.COLUMN_CUSTOM_VPLAN_START, DBHelper.COLUMN_CUSTOM_VPLAN_END,
            DBHelper.COLUMN_CUSTOM_VPLAN_DAY_OF_WEEK, DBHelper.COLUMN_CUSTOM_VPLAN_STUDIENGANG_ID,
            DBHelper.COLUMN_CUSTOM_VPLAN_FB};

    public final String DB_TABLE = DBHelper.TABLE_CUSTOM_VPLAN;

    public CustomVPlanDataSource(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public void createCustomVPlanItem(VPlanItem vPlanItem) {
        if (!this.exists(DBHelper.COLUMN_CUSTOM_VPLAN_TITLE, vPlanItem.getModulName(),
                DBHelper.COLUMN_CUSTOM_VPLAN_DAY_OF_WEEK, vPlanItem.getDayOfWeek(),
                DBHelper.COLUMN_CUSTOM_VPLAN_START, vPlanItem.getStartTime())) {
            ContentValues values = new ContentValues();

            // Werte einsetzen
            values.put(DBHelper.COLUMN_CUSTOM_VPLAN_TITLE, vPlanItem.getModulName());
            values.put(DBHelper.COLUMN_CUSTOM_VPLAN_PROF, vPlanItem.getProfName());
            values.put(DBHelper.COLUMN_CUSTOM_VPLAN_ROOM, vPlanItem.getRoom());
            values.put(DBHelper.COLUMN_CUSTOM_VPLAN_START, vPlanItem.getStartTime());
            values.put(DBHelper.COLUMN_CUSTOM_VPLAN_END, vPlanItem.getEndTime());
            values.put(DBHelper.COLUMN_CUSTOM_VPLAN_DAY_OF_WEEK, vPlanItem.getDayOfWeek());
            values.put(DBHelper.COLUMN_CUSTOM_VPLAN_STUDIENGANG_ID, vPlanItem.getStudiengangID());
            values.put(DBHelper.COLUMN_CUSTOM_VPLAN_FB, vPlanItem.getFb());

            this.database.insert(DBHelper.TABLE_CUSTOM_VPLAN, null, values);
        } else {
            Log.wtf("DEBUG: ALREADY EXISTS", "Item already exists");
        }
    }

    public void deleteCustomVPlanItem(VPlanItem vPlanItem) {
        long id = vPlanItem.getId();
        this.database.delete(DBHelper.TABLE_CUSTOM_VPLAN, DBHelper.COLUMN_CUSTOM_VPLAN_ID + " = " + id, null);
    }

    public ArrayList<VPlanItem> getAllCustomVPlanItems() {
        ArrayList<VPlanItem> vPlanItems = new ArrayList<VPlanItem>();

        Cursor cursor = database.query(DBHelper.TABLE_CUSTOM_VPLAN, allColumns, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            VPlanItem vPlanItem = cursorToVPlanItem(cursor);
            vPlanItems.add(vPlanItem);
            cursor.moveToNext();
        }

        cursor.close();
        return vPlanItems;
    }

    public boolean exists(String fieldName, String fieldValue, String fieldDayOfWeek, String valueDayOfWeek, String fieldStartTime, String valueStartTime) {
        String Query = "Select * from " + this.DB_TABLE + " where " + fieldName + " = '" + fieldValue + "' and "
                + fieldDayOfWeek + " = '" + valueDayOfWeek + "' and "
                + fieldStartTime + " = '" + valueStartTime + "'";
        Cursor cursor = this.database.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
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

        return vPlanItem;
    }
}
