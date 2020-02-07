package com.example.trackmyexpense;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "expense.db";
    public static final String TABLE_NAME = "exptable";
    public static final String C_NUMBER = "SerialNumber";
    public static final String C_PURPOSE= "Purpose";
    public static final String C_EXPENSE = "Expense";

    public DBHelper(Context context) {
        //creates the DB, passing context, name, null value for factory when the constructor is invoked
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + "(" + C_NUMBER + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + C_PURPOSE + " TEXT, " + C_EXPENSE + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean check_update(String purpose) {
        SQLiteDatabase db = this.getWritableDatabase();
        String Query = String.format("SELECT * FROM %s WHERE Purpose = '%s'", TABLE_NAME, purpose);

            Cursor rs = db.rawQuery(Query, null);
            if (rs.getCount() <= 0) {
                rs.close();
                return false;
            }
            rs.close();
            return true;
    }

    public boolean update(String purpose, int expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();    //created for updating database if same string instance found
        String Q2 = String.format("SELECT Expense FROM %s WHERE Purpose = '%s'", TABLE_NAME, purpose);
        Cursor rs = db.rawQuery(Q2, null);
        rs.moveToFirst();

            int exp_update = rs.getInt(0);
            exp_update += expense;   //add new expenses to update the field and prevent redundancies
            cv.put("Expense", exp_update);
            long r = db.update(TABLE_NAME, cv, "Purpose = ?", new String[]{purpose});
            if(r == -1) return false;
            else return true;
    }

    public boolean insert(String purpose, int expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(C_PURPOSE, purpose);
        cv.put(C_EXPENSE, expense);
        long res = db.insert(TABLE_NAME, null, cv);
        if(res == -1) return false;
        else return true;
    }

    public Cursor get() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor rs = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return rs;
    }

    public Cursor get_desc() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY Expense DESC", null);
        return cur;
    }

    public int sum() {
        SQLiteDatabase db =  this.getReadableDatabase();
        String sQuery = String.format("SELECT SUM(%s) as Total FROM %s", C_EXPENSE, TABLE_NAME);
        Cursor  s = db.rawQuery(sQuery, null);
        int s1;
        if(s.moveToFirst()) {
            s1 = s.getInt(s.getColumnIndex("Total"));
            return s1;
        }
        s.close();  //prevents cursor leaks
        return 0;
    }

    public Integer remove(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, "SerialNumber = ?", new String[]{id});
    }
}
