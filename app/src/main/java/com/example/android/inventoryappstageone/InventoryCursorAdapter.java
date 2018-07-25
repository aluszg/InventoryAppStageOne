package com.example.android.inventoryappstageone;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryappstageone.data.InventoryContract.InventoryEntry;


public class InventoryCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super (context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from (context).inflate (R.layout.list_item, parent, false);
    }

    /**
     * This method binds the inventory data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current inventory can be set on the name TextView
     * in the list item layout.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView productName = (TextView) view.findViewById (R.id.name);
        TextView productPrice = (TextView) view.findViewById (R.id.price);
        TextView productQuantity = (TextView) view.findViewById (R.id.quantity);

        // Find the columns of inventory attributes that we're interested in
        int productNameColumnIndex = cursor.getColumnIndex (InventoryEntry.COLUMN_PRODUCT_NAME);
        int productPriceColumnIndex = cursor.getColumnIndex (InventoryEntry.COLUMN_PRODUCT_PRICE);
        int productQuantityColumnIndex = cursor.getColumnIndex (InventoryEntry.COLUMN_PRODUCT_QUANTITY);

        // Read the inventory attributes from the Cursor for the current inventory
        String inventoryProductName = cursor.getString (productNameColumnIndex);
        String inventoryProductPrice = cursor.getString (productPriceColumnIndex);
        String inventoryProductQuantity = cursor.getString (productQuantityColumnIndex);

        // If the product name is empty string or null, then use some default text
        // that says "Product name unknown", so the TextView isn't blank.
        if (TextUtils.isEmpty (inventoryProductName)) {
            inventoryProductName = context.getString (R.string.unknown_inventoryProductName);
            inventoryProductPrice = context.getString (R.string.unknown_inventoryProductPrice);
            inventoryProductQuantity = context.getString (R.string.unknown_inventoryProductQuantity);
        }

        // Update the TextViews with the attributes for the current inventory
        productName.setText (inventoryProductName);
        productPrice.setText (inventoryProductPrice);
        productQuantity.setText (inventoryProductQuantity);
    }
}