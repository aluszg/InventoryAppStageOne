package com.example.android.inventoryappstageone.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.inventoryappstageone.data.InventoryContract.InventoryEntry;

/**
 * {@link ContentProvider} for Inventory app.
 */
public class InventoryProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName ();

    /**
     * URI matcher code for the content URI for the inventories table
     */
    private static final int INVENTORIES = 100;

    /**
     * URI matcher code for the content URI for a single inventory in the inventories table
     */
    private static final int INVENTORY_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher (UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {

        // The content URI of the form "content://com.example.android.inventoryappstageone.data/inventories" will map to the
        // integer code {@link #INVENTORIES). This URI is used to provide access to MULTIPLE rows
        // of the inventories table.
        sUriMatcher.addURI (InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORIES, INVENTORIES);

        //The content URI of the form "content://com.example.android.inventoryappstageone.data/inventories/#" will map to the
        // integer code {@link #INVENTORY_ID). This URI is used to provide access to ONE single row
        // of the inventories table.
        sUriMatcher.addURI (InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORIES + "/#", INVENTORY_ID);
    }

    //**Database helper object */
    private InventoryDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper (getContext ());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase ();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match (uri);
        switch (match) {
            case INVENTORIES:
                // For the INVENTORIES code, query the inventories table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the inventories table.
                cursor = database.query (InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INVENTORY_ID:
                // For the INVENTORY_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.inventoryappstageone/inventories/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf (ContentUris.parseId (uri))};

                // This will perform a query on the inventories table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query (InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException ("Cannot query unknown URI " + uri);
        }

        //set notification URI on the Cursor,
        //so we know what content URI the Cursor was created for.
        //If the data this URI changers, then we know we need to update the Cursor.
        cursor.setNotificationUri (getContext ().getContentResolver (), uri);

        //Return the cursor
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match (uri);
        switch (match) {
            case INVENTORIES:
                return insertInventory (uri, contentValues);
            default:
                throw new IllegalArgumentException ("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a inventory into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertInventory(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString (InventoryEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException ("Inventory requires a name");
        }

        // If the price is provided, check that it's greater than or equal to 0
        Integer price = values.getAsInteger (InventoryEntry.COLUMN_PRODUCT_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException ("Inventory requires valid price");
        }

        // If the price is provided, check that it's greater than or equal to 0
        Integer quantity = values.getAsInteger (InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException ("Inventory requires valid quantity");
        }

        // Check that the name is not null
        String supplier_name = values.getAsString (InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
        if (supplier_name == null) {
            throw new IllegalArgumentException ("Inventory requires a supplier name");
        }

        // Check that the name is not null
        String supplier_phone_number = values.getAsString (InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER);
        if (supplier_phone_number == null) {
            throw new IllegalArgumentException ("Inventory requires a supplier phone number");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase ();

        // Insert the new inventory with the given values
        long id = database.insert (InventoryContract.InventoryEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e (LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        //Notify all listeners that the data has changed for the inventory content URI
        getContext ().getContentResolver ().notifyChange (uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId (uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match (uri);
        switch (match) {
            case INVENTORIES:
                return updateInventory (uri, contentValues, selection, selectionArgs);
            case INVENTORY_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf (ContentUris.parseId (uri))};
                return updateInventory (uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException ("Update is not supported for " + uri);
        }
    }

    /**
     * Update inventories in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more inventories).
     * Return the number of rows that were successfully updated.
     */
    private int updateInventory(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey (InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString (InventoryEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException ("Inventory requires a name");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey (InventoryEntry.COLUMN_PRODUCT_PRICE)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer price = values.getAsInteger (InventoryEntry.COLUMN_PRODUCT_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException ("Inventory requires valid price");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey (InventoryEntry.COLUMN_PRODUCT_QUANTITY)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer quantity = values.getAsInteger (InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException ("Inventory requires valid quantity");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey (InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME)) {
            String supplier_name = values.getAsString (InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            if (supplier_name == null) {
                throw new IllegalArgumentException ("Inventory requires a supplier name");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey (InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER)) {
            String supplier_phone_number = values.getAsString (InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER);
            if (supplier_phone_number == null) {
                throw new IllegalArgumentException ("Inventory requires a supplier phone number");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size () == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase ();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update (InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext ().getContentResolver ().notifyChange (uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase ();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match (uri);
        switch (match) {
            case INVENTORIES:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete (InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf (ContentUris.parseId (uri))};
                rowsDeleted = database.delete (InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException ("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext ().getContentResolver ().notifyChange (uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match (uri);
        switch (match) {
            case INVENTORIES:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException ("Unknown URI " + uri + " with match " + match);
        }
    }
}