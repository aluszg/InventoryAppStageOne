package com.example.android.inventoryappstageone;

import android.content.ContentValues;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryappstageone.data.InventoryContract.InventoryEntry;


public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the inventory data loader
     */
    private static final int EXISTING_INVENTORY_LOADER = 0;
    /**
     * Product quantity
     */
    int quantity = 0;
    /**
     * Content URI for the existing inventory (null if it's a new inventory)
     */
    private Uri mCurrentInventoryUri;
    /**
     * EditText field to enter the product's name
     */
    private EditText mProductName;
    /**
     * EditText field to enter the product's price
     */
    private EditText mProductPrice;
    /**
     * EditText field to enter the quantity of the product's
     */
    private TextView mProductQuantity;
    /**
     * EditText field to enter the supplier name
     */
    private EditText mSupplierName;
    /**
     * EditText field to enter the supplier phone number
     */
    private EditText mSupplierPhoneNumber;
    /**
     * Boolean flag that keeps track of whether the inventory has been edited (true) or not (false)
     */
    private boolean mInventoryHasChanged = false;
    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener () {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInventoryHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_editor);

        // Setup order button
        FloatingActionButton order = findViewById (R.id.order);
        order.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                String supplierPhoneNumberString = mSupplierPhoneNumber.getText ().toString ().trim ();
                // Otherwise, the insertion was successful and we can display a toast.
                if (supplierPhoneNumberString != "") {
                    String uri = "tel:" + supplierPhoneNumberString ;
                    Intent intent = new Intent (Intent.ACTION_CALL);
                    intent.setData (Uri.parse (uri));
                }
                else {

                }
            }
        });

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new inventory or editing an existing one.
        Intent intent = getIntent ();
        mCurrentInventoryUri = intent.getData ();

        //If the intent DOES NOT contain a inventory content URI, then we know that we are creating a new inventory
        if (mCurrentInventoryUri == null) {
            //This is a new inventory, so change the app bar to say "Add a Inventory"
            setTitle (getString (R.string.editor_activity_title_new_inventory));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a inventory that hasn't been created yet.)
            invalidateOptionsMenu ();
        } else {
            //Otherwise this is an existing inventory, so change app bar to say "Edit Inventory"
            setTitle (getString (R.string.editor_activity_title_edit_inventory));

            // Initialize a loader to read the inventory data from the database
            // and display the current values in the editor
            getLoaderManager ().initLoader (EXISTING_INVENTORY_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mProductName = (EditText) findViewById (R.id.edit_product_name);
        mProductPrice = (EditText) findViewById (R.id.edit_product_price);
        mProductQuantity = (TextView) findViewById (R.id.edit_product_quantity);
        mSupplierName = (EditText) findViewById (R.id.edit_supplier_name);
        mSupplierPhoneNumber = (EditText) findViewById (R.id.edit_supplier_phone_number);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mProductName.setOnTouchListener (mTouchListener);
        mProductPrice.setOnTouchListener (mTouchListener);
        mProductQuantity.setOnTouchListener (mTouchListener);
        mSupplierName.setOnTouchListener (mTouchListener);
        mSupplierPhoneNumber.setOnTouchListener (mTouchListener);
    }

    /**
     * Get user input from editor and save inventory into database.
     */
    private void saveInventory() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String productNameString = mProductName.getText ().toString ().trim ();
        String productPriceString = mProductPrice.getText ().toString ().trim ();
        String productQuantityString = Integer.toString (quantity);
        String supplierNameString = mSupplierName.getText ().toString ().trim ();
        String supplierPhoneNumberString = mSupplierPhoneNumber.getText ().toString ().trim ();

        // Check if this is supposed to be a new inventory
        // and check if all the fields in the editor are blank
        if (mCurrentInventoryUri == null && TextUtils.isEmpty (productNameString) && TextUtils.isEmpty (productPriceString) && TextUtils.isEmpty (productQuantityString) && TextUtils.isEmpty (supplierNameString) && TextUtils.isEmpty (supplierPhoneNumberString)) {
            // Since no fields were modified, we can return early without creating a new inventory.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }
        // Create a ContentValues object where column names are the keys,
        // and inventory attributes from the editor are the values.
        ContentValues values = new ContentValues ();
        values.put (InventoryEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put (InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierNameString);
        values.put (InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER, supplierPhoneNumberString);

        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int productPrice = 0;
        if (!TextUtils.isEmpty (productPriceString)) {
            productPrice = Integer.parseInt (productPriceString);
        }
        values.put (InventoryEntry.COLUMN_PRODUCT_PRICE, productPrice);

        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int productQuantity = 0;
        if (!TextUtils.isEmpty (productQuantityString)) {
            productQuantity = Integer.parseInt (productQuantityString);
        }
        values.put (InventoryEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);

        // Determine if this is a new or existing inventory by checking if mCurrentPetUri is null or not
        if (mCurrentInventoryUri == null) {
            // This is a NEW inventory, so insert a new inventory into the provider,
            // returning the content URI for the new inventory.
            Uri newUri = getContentResolver ().insert (InventoryEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText (this, getString (R.string.editor_insert_inventory_failed), Toast.LENGTH_SHORT).show ();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText (this, getString (R.string.editor_insert_inventory_successful), Toast.LENGTH_SHORT).show ();
            }

        } else {

            // Otherwise this is an EXISTING inventory, so update the inventory with content URI: mCurrentPetUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentPetUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver ().update (mCurrentInventoryUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText (this, getString (R.string.editor_update_inventory_failed), Toast.LENGTH_SHORT).show ();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText (this, getString (R.string.editor_update_inventory_successful), Toast.LENGTH_SHORT).show ();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater ().inflate (R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu (menu);
        // If this is a new inventory, hide the "Delete" menu item.
        if (mCurrentInventoryUri == null) {
            MenuItem menuItem = menu.findItem (R.id.action_delete);
            menuItem.setVisible (false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId ()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save inventory to database
                saveInventory ();
                // Exit activity
                finish ();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog ();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the inventory hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mInventoryHasChanged) {
                    NavUtils.navigateUpFromSameTask (EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener () {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, navigate to parent activity.
                        NavUtils.navigateUpFromSameTask (EditorActivity.this);
                    }
                };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog (discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected (item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the inventory hasn't changed, continue with handling back button press
        if (!mInventoryHasChanged) {
            super.onBackPressed ();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener () {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked "Discard" button, close the current activity.
                finish ();
            }
        };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog (discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        // Since the editor shows all inventory attributes, define a projection that contains
        // all columns from the inventory table
        String[] projection = {InventoryEntry._ID, InventoryEntry.COLUMN_PRODUCT_NAME, InventoryEntry.COLUMN_PRODUCT_PRICE, InventoryEntry.COLUMN_PRODUCT_QUANTITY, InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader (this,  // Parent activity context
                mCurrentInventoryUri,   // Query the content URI for the current inventory
                projection,             // Columns to include in the resulting Cursor
                null,           // No selection clause
                null,        // No selection arguments
                null);          // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount () < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst ()) {
            // Find the columns of inventory attributes that we're interested in
            int productNameColumnIndex = cursor.getColumnIndex (InventoryEntry.COLUMN_PRODUCT_NAME);
            int productPriceColumnIndex = cursor.getColumnIndex (InventoryEntry.COLUMN_PRODUCT_PRICE);
            int productQuantityColumnIndex = cursor.getColumnIndex (InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex (InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int supplierPhoneNumberColumnIndex = cursor.getColumnIndex (InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER);

            // Extract out the value from the Cursor for the given column index
            String productName = cursor.getString (productNameColumnIndex);
            int productPrice = cursor.getInt (productPriceColumnIndex);
            int productQuantity = cursor.getInt (productQuantityColumnIndex);
            String supplierName = cursor.getString (supplierNameColumnIndex);
            String supplierPhoneNumber = cursor.getString (supplierPhoneNumberColumnIndex);

            // Update the views on the screen with the values from the database
            mProductName.setText (productName);
            mProductPrice.setText (Integer.toString (productPrice));
            mProductQuantity.setText (Integer.toString (productQuantity));
            mSupplierName.setText (supplierName);
            mSupplierPhoneNumber.setText (supplierPhoneNumber);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductName.setText ("");
        mProductPrice.setText ("");
        mProductQuantity.setText ("");
        mSupplierName.setText ("");
        mSupplierPhoneNumber.setText ("");
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder (this);
        builder.setMessage (R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton (R.string.discard, discardButtonClickListener);
        builder.setNegativeButton (R.string.keep_editing, new DialogInterface.OnClickListener () {
            public void onClick(DialogInterface dialog, int id) {

                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the inventory.
                if (dialog != null) {
                    dialog.dismiss ();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create ();
        alertDialog.show ();
    }


    /**
     * Prompt the user to confirm that they want to delete this inventory.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder (this);
        builder.setMessage (R.string.delete_dialog_msg);
        builder.setPositiveButton (R.string.delete, new DialogInterface.OnClickListener () {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the inventory.
                deleteInventory ();
            }
        });

        builder.setNegativeButton (R.string.cancel, new DialogInterface.OnClickListener () {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the inventory.
                if (dialog != null) {
                    dialog.dismiss ();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create ();
        alertDialog.show ();
    }

    /**
     * Perform the deletion of the inventory in the database.
     */
    private void deleteInventory() {
        // Only perform the delete if this is an existing inventory.
        if (mCurrentInventoryUri != null) {
            // Call the ContentResolver to delete the inventory at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the inventory that we want.
            int rowsDeleted = getContentResolver ().delete (mCurrentInventoryUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText (this, getString (R.string.editor_delete_inventory_failed), Toast.LENGTH_SHORT).show ();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText (this, getString (R.string.editor_delete_inventory_successful), Toast.LENGTH_SHORT).show ();
            }
        }
        // Close the activity
        finish ();
    }

    /**
     * This method is called when the plus button is clicked.
     */
    public void increment(View view) {
        if (quantity == 100) {
            // Show an error message as a toast
            Toast.makeText (this, "You cannot have more than 100 inventory", Toast.LENGTH_SHORT).show ();
            // Exit this method early because there's nothing left to do
            return;
        }
        quantity = quantity + 1;
        displayQuantity (quantity);
    }

    /**
     * This method is called when the minus button is clicked.
     */
    public void decrement(View view) {
        if (quantity == 0) {
            // Show an error message as a toast
            Toast.makeText (this, "You cannot have less than 0 inventory", Toast.LENGTH_SHORT).show ();
            // Exit this method early because there's nothing left to do
            return;
        }
        quantity = quantity - 1;
        displayQuantity (quantity);
    }

    /**
     * This method displays the given quantity value on the screen.
     */
    private void displayQuantity(int quantity) {
        TextView quantityTextView = (TextView) findViewById (R.id.edit_product_quantity);
        quantityTextView.setText (Integer.toString (quantity));
    }
}