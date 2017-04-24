package com.example.danie.inventario;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.danie.inventario.data.ProductContract;
import com.example.danie.inventario.data.ProductContract.ProductEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;
    static final int REQUEST_UPLOAD_PICTURE = 1;

    private Uri mCurrentProductUri;

    private EditText mNameEditText;
    private EditText mSupplierEditText;
    private EditText mSellPriceEditText;
    private EditText mBuyQuantityEditText;

    private TextView mQuantityInStockString;
    private TextView mSalesString;

    private boolean mProductHasChanged = false;

    private ImageView mProductImage;

    static final int RETRIEVE_IMAGE_REQUEST = 1;

    private Uri mProductPhotoUri;
    private String mProductPhotoString;

    private Button mBuyStockButton;
    private Button mSellStockButton;
    private Button mUploadPicButton;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mSupplierEditText = (EditText) findViewById(R.id.edit_product_supplier);
        mSellPriceEditText = (EditText) findViewById(R.id.edit_product_sellprice);
        mBuyQuantityEditText = (EditText) findViewById(R.id.edit_product_buy_quantity);

        mQuantityInStockString = (TextView) findViewById(R.id.quantity_instock_integer);
        mSalesString = (TextView) findViewById(R.id.quantity_sales_integer);

        mProductImage = (ImageView) findViewById(R.id.product_image);

        mBuyStockButton = (Button) findViewById(R.id.buy_stock_button);
        mSellStockButton = (Button) findViewById(R.id.sell_product_button);
        mUploadPicButton = (Button) findViewById(R.id.upload_pict_button);

        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_product));
            invalidateOptionsMenu();
            mBuyStockButton.setVisibility(View.GONE);
            mSellStockButton.setVisibility(View.GONE);
            mUploadPicButton.setVisibility(View.GONE);
            mQuantityInStockString.setVisibility(View.GONE);
            mSalesString.setVisibility(View.GONE);
            mProductPhotoUri = Uri.parse("android.resource://com.example.danie.inventario/"
                    + R.drawable.ic_upload_icon);;
            //mProductPhotoString = mProductPhotoUri.toString();
            //mProductImage.setImageURI(mProductPhotoUri);
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_product));
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
            //mProductPhotoString = ProductEntry.COLUMN_PRODUCT_THUMBNAIL;
            //mProductPhotoUri = Uri.parse(mProductPhotoString);
            //mProductImage.setImageURI(mProductPhotoUri);
        }

        mBuyStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buyStock();
            }
        });

        mSellStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sellStock();
            }
        });

        mNameEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mSellPriceEditText.setOnTouchListener(mTouchListener);
        mBuyQuantityEditText.setOnTouchListener(mTouchListener);
    }

    public void sellStock(){
        String existingStockQuantity = mQuantityInStockString.getText().toString();
        String sellQuantity = mBuyQuantityEditText.getText().toString();
        String nameString = mNameEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String sellPriceString = mSellPriceEditText.getText().toString().trim();

        if (existingStockQuantity.isEmpty()) {
            //Toast.makeText(EditorActivity.this, "Enter a Buy / Sell Quantity", Toast.LENGTH_SHORT).show();
            return;
        } else {
            String salesToDateText = mSalesString.getText().toString();
            Integer stockInt = Integer.parseInt(existingStockQuantity);
            Integer sellQuantityInt = Integer.parseInt(sellQuantity);
            Integer salesToDateInt = Integer.parseInt(salesToDateText);
            Integer newQuantity = stockInt - sellQuantityInt;
            if (newQuantity >= 0) {
                ContentValues values = new ContentValues();
                values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
                values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplierString);
                values.put(ProductEntry.COLUMN_PRODUCT_SELLPRICE, sellPriceString);
                values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);
                values.put(ProductEntry.COLUMN_PRODUCT_SALES, sellQuantityInt + salesToDateInt);
                getContentResolver().update(mCurrentProductUri, values, null, null);
                mBuyQuantityEditText.setText("0");
            }
        }
    }

    public void buyStock() {
        String existingStockQuantity = mQuantityInStockString.getText().toString();
        String buyQuantity = mBuyQuantityEditText.getText().toString();
        String nameString = mNameEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String sellPriceString = mSellPriceEditText.getText().toString().trim();
        if (buyQuantity.isEmpty() || buyQuantity.equals("0")) {
            return;
        } else {
            Integer stockInt = Integer.parseInt(existingStockQuantity);
            Integer newStockToAddInt = Integer.parseInt(buyQuantity);
            Integer newQuantity = stockInt + newStockToAddInt;

            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);
            values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
            values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplierString);
            values.put(ProductEntry.COLUMN_PRODUCT_SELLPRICE, sellPriceString);
            getContentResolver().update(mCurrentProductUri, values, null, null);
            mBuyQuantityEditText.setText("0");
        }
    }

    private void uploadPicture() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_UPLOAD_PICTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_UPLOAD_PICTURE && resultCode == RESULT_OK) {
            Uri fullPhotoUri = data.getData();
            mProductPhotoString = fullPhotoUri.toString();
            mProductImage.setImageURI(fullPhotoUri);

            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_PRODUCT_THUMBNAIL, mProductPhotoString);
            if(mCurrentProductUri!=null){
                getContentResolver().update(mCurrentProductUri, values, null, null);
            }


        }
    }

    private void saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String sellPriceString = mSellPriceEditText.getText().toString().trim();
        String buyQuantityString = mBuyQuantityEditText.getText().toString().trim();
        int productInStockInt = Integer.parseInt(mQuantityInStockString.getText().toString().toString());
        int productSalesInt = Integer.parseInt(mSalesString.getText().toString().trim());

        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(supplierString) &&
                TextUtils.isEmpty(buyQuantityString) &&
                TextUtils.isEmpty(sellPriceString)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplierString);
        values.put(ProductEntry.COLUMN_PRODUCT_SELLPRICE, sellPriceString);
        if (mCurrentProductUri == null){
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, buyQuantityString);
        } else {
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, productInStockInt);
        }
        values.put(ProductEntry.COLUMN_PRODUCT_SALES, productSalesInt);
//        if(mProductPhotoString!=null){
//            values.put(ProductEntry.COLUMN_PRODUCT_THUMBNAIL, mProductPhotoUri.toString()));
//        }
        //values.put(ProductEntry.COLUMN_PRODUCT_THUMBNAIL, mProductPhotoUri.toString());

        double sellPrice = 0.0;
        if (!TextUtils.isEmpty(sellPriceString)) {
            sellPrice = Double.parseDouble(sellPriceString);
        }
        values.put(ProductEntry.COLUMN_PRODUCT_SELLPRICE, sellPrice);

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_LONG).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            case R.id.upload_photo_menu:
                uploadPicture();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductEntry.COLUMN_PRODUCT_SELLPRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SALES,
                ProductEntry.COLUMN_PRODUCT_THUMBNAIL};

        return new CursorLoader(this, mCurrentProductUri, projection, null, null, null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
            int sellPriceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SELLPRICE);
            int buyQuantityColumnIndex =
                    cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int salesColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SALES);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_THUMBNAIL);

            String name = cursor.getString(nameColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            double sellPrice = cursor.getDouble(sellPriceColumnIndex);
            int buyQuantity = cursor.getInt(buyQuantityColumnIndex);
            int salesQuantity = cursor.getInt(salesColumnIndex);
            String thumbnail = cursor.getString(imageColumnIndex);

            if (thumbnail != null) {
                Uri pictureUri = Uri.parse(thumbnail);
                mProductImage.setImageURI(pictureUri);
            }

            mNameEditText.setText(name);
            mSupplierEditText.setText(supplier);
            mSellPriceEditText.setText(Double.toString(sellPrice));
            mBuyQuantityEditText.setText(Integer.toString(0));
            mQuantityInStockString.setText(Integer.toString(buyQuantity));
            mSalesString.setText(Integer.toString(salesQuantity));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mSupplierEditText.setText("");
        mSellPriceEditText.setText("");
        mBuyQuantityEditText.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_LONG).show();
            }
        }

        finish();
    }
}