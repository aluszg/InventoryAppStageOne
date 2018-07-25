package com.example.android.inventoryappstageone.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Inventories app.
 */
public final class InventoryContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryappstageone";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse ("content://" + CONTENT_AUTHORITY);
    /**
     * Possible path (appended to base content URI for possible URI's)
     */
    public static final String PATH_INVENTORIES = "inventories";

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private InventoryContract() {
    }

    /* Inner class that defines the table contents */
    public static final class InventoryEntry implements BaseColumns {

        /**
         * The content URI to access the inventory data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath (BASE_CONTENT_URI, PATH_INVENTORIES);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of inventories.
         */
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORIES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single inventory.
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORIES;

        /**
         * Name of database table for inventories
         */
        public static final String TABLE_NAME = "inventory";

        /**
         * Unique ID number for the inventory (only for use in the database table).
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the product.
         * Type: TEXT
         */
        public static final String COLUMN_PRODUCT_NAME = "product_name";

        /**
         * Price of the product.
         * Type: INTEGER
         */
        public static final String COLUMN_PRODUCT_PRICE = "price";

        /**
         * Quantity of the product.
         * Type: INTEGER
         */
        public static final String COLUMN_PRODUCT_QUANTITY = "quantity";

        /**
         * Name of the product supplier.
         * Type: TEXT
         */
        public static final String COLUMN_PRODUCT_SUPPLIER_NAME = "supplier_name";

        /**
         * Phone number of the product supplier.
         * Type: INTEGER
         */
        public static final String COLUMN_PRODUCT_SUPPLIER_PHONE_NUMBER = "supplier_phone_number";
    }
}