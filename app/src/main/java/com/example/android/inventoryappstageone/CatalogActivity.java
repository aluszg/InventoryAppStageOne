package com.example.android.inventoryappstageone;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.android.inventoryappstageone.data.InventoryContract.InventoryEntry;

/**
 * Displays list of inventories that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the inventory data loader
     */
    private static final int INVENTORY_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById (R.id.fab);
        fab.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (CatalogActivity.this, EditorActivity.class);
                startActivity (intent);
            }
        });

        // Find the ListView which will be populated with the inventory data
        ListView inventoryListView = (ListView) findViewById (R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById (R.id.empty_view);
        inventoryListView.setEmptyView (emptyView);

        //Setup an Adapter to create a list item for each row of inventory data in the Cursor.
        //There is no inventory data yet (until the loader finishes) so pass in null for the Cursor
        mCursorAdapter = new InventoryCursorAdapter (this, null);
        inventoryListView.setAdapter (mCursorAdapter);

        //Setup item click listener
        inventoryListView.setOnItemClickListener (new AdapterView.OnItemClickListener () {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent (CatalogActivity.this, EditorActivity.class);

                //Form the content URI that represents the specific inventory that was clicked on
                Uri currentInventoryUri = ContentUris.withAppendedId (InventoryEntry.CONTENT_URI, id);

                //Set the URI on the data field of the intent
                intent.setData (currentInventoryUri);

                //Lauch the {@link EditorActivity} to display the data for the current inventory.
                startActivity (intent);

            }
        });

        //Kick off the loader
        getLoaderManager ().initLoader (INVENTORY_LOADER, null, this);
    }

    /*
     * Helper method to insert inventory data into the database. For debugging purposes only.
     */
    private void insertInventory() {
        // Create a ContentValues object where column names are the keys,
        // and inventory attributes from the editor are the values.
        ContentValues values = new ContentValues ();
        values.put (InventoryEntry.COLUMN_PRODUCT_NAME, "");
        values.put (InventoryEntry.COLUMN_PRODUCT_PRICE, "");
        values.put (InventoryEntry.COLUMN_PRODUCT_QUANTITY, "");
        values.put (InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, "");
        values.put (InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, "");
    }


    private void deleteAllInvenotires() {
        int rowsDeleted = getContentResolver ().delete (InventoryEntry.CONTENT_URI, null, null);
        Log.v ("CatalogActivity", rowsDeleted + " rows deleted from inventory database");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater ().inflate (R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId ()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertInventory ();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllInvenotires ();
                return true;
        }
        return super.onOptionsItemSelected (item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {InventoryEntry._ID, InventoryEntry.COLUMN_PRODUCT_NAME, InventoryEntry.COLUMN_PRODUCT_PRICE, InventoryEntry.COLUMN_PRODUCT_QUANTITY, InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER};

        // This loader will execute the ContentProvider's quary method on a background thread
        return new CursorLoader (this,    // Parent activity context
                InventoryEntry.CONTENT_URI, // Provider content URI to query
                projection,     // Columns to include in the resulting Cursor
                null,       // No selection clause
                null,   // No selection arguments
                null);      // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Update (@link InventoryCursorAdapter will this cursor containing updated inventory data
        mCursorAdapter.swapCursor (data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor (null);
    }
}