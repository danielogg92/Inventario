package com.example.danie.inventario;

import android.content.Context;
import android.database.Cursor;
import android.media.Image;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.danie.inventario.data.ProductContract.ProductEntry;

public class ProductCursorAdapter extends CursorAdapter {

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
        ImageView pictureImageView = (ImageView) view.findViewById(R.id.product_thumbnail);

        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
        int salesColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SALES);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int sellpriceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SELLPRICE);
        int pictureColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PICTURE);

        String productName = cursor.getString(nameColumnIndex);
        String productSupplier = cursor.getString(supplierColumnIndex);
        String productQuantity = "Quantity in stock = " + cursor.getString(quantityColumnIndex);
        String productSales = "Product Sales to date = " + cursor.getString(salesColumnIndex);
        String productSellPrice = "Sell Price @ $" + cursor.getString(sellpriceColumnIndex);

        nameTextView.setText(productName);
        summaryTextView.setText(productSupplier);
        quantityTextView.setText(productQuantity);
        salesTextView.setText(productSales);
        sellpriceTextView.setText(productSellPrice);
    }
}
