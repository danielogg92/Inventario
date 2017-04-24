package com.example.danie.inventario;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.danie.inventario.data.ProductContract.ProductEntry;
import com.example.danie.inventario.data.ProductDbHelper;

import static com.example.danie.inventario.R.id.sellprice;
import static com.example.danie.inventario.R.layout.activity_catalog;

public class ProductCursorAdapter extends CursorAdapter {

    private ProductDbHelper mDbHelper;

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.supplier);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView salesTextView = (TextView) view.findViewById(R.id.sales);
        TextView sellpriceTextView = (TextView) view.findViewById(R.id.sellprice);
        Button quicksellButton = (Button) view.findViewById(R.id.quicksale_1off);

        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
        int salesColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SALES);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int sellpriceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SELLPRICE);
        final int idColumnIndex = cursor.getColumnIndex(ProductEntry._ID);

        final String productId = cursor.getString(idColumnIndex);
        String productName = cursor.getString(nameColumnIndex);
        String productSupplier = cursor.getString(supplierColumnIndex);
        String productQuantity = "Quantity in stock = " + cursor.getString(quantityColumnIndex);
        //final int productQuantityInt = cursor.getInt(quantityColumnIndex);
        final int productQuantityInt = Integer.parseInt(cursor.getString(quantityColumnIndex));
        String productSales = "Product Sales to date = " + cursor.getString(salesColumnIndex);
        //final int productSalesInt = cursor.getInt(salesColumnIndex);
        final int productSalesInt = Integer.parseInt(cursor.getString(salesColumnIndex));
        String productSellPrice = "Sell Price @ $" + cursor.getString(sellpriceColumnIndex);

        nameTextView.setText(productName);
        summaryTextView.setText(productSupplier);
        quantityTextView.setText(productQuantity);
        salesTextView.setText(productSales);
        sellpriceTextView.setText(productSellPrice);

        quicksellButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view1){
                ContentResolver resolver = view1.getContext().getContentResolver();
                ContentValues values = new ContentValues();
                if (productQuantityInt > 0){
                    int itemId = Integer.parseInt(productId);
                    int newProductQuantityInt = productQuantityInt - 1;
                    int newSalesInt = productSalesInt + 1;

                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, newProductQuantityInt);
                    values.put(ProductEntry.COLUMN_PRODUCT_SALES, newSalesInt);
                    Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, itemId);
                    resolver.update(currentProductUri, values, null, null);
                }
            }
        });
    }
}
