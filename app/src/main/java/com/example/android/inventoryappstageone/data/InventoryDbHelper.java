package com.example.android.inventoryappstageone.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryappstageone.data.InventoryContract.InventoryEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = InventoryDbHelper.class.getSimpleName ();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "shelter.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link InventoryDbHelper}.
     */
    public InventoryDbHelper(Context context) {
        super (context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that Contains the AQL statement to create the inventories table
        String SQL_CREATE_INVENTORIES_TABLE;
        SQL_CREATE_INVENTORIES_TABLE = "CREATE TABLE " + InventoryEntry.TABLE_NAME + " (" + InventoryEntry._ID + " " + "INTEGER PRIMARY KEY AUTOINCREMENT, " + InventoryEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " + InventoryEntry.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, " + InventoryEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL, " + InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME + " TEXT NOT NULL, " + InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER + " INTEGER NOT NULL);";
        // Execute the SQL statement
        db.execSQL (SQL_CREATE_INVENTORIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}