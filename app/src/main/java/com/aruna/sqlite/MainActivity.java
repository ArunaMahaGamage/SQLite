package com.aruna.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button btn_insert, btn_read;
    TextView tv_data;

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedReaderContract.FeedEntry.TABLE_NAME + " (" +
                    FeedReaderContract.FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE + " TEXT," +
                    FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedReaderContract.FeedEntry.TABLE_NAME;

    FeedReaderDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDbHelper = new FeedReaderDbHelper(getBaseContext());

        initUI();
    }

    private void initUI() {
        btn_insert = findViewById(R.id.btn_insert);
        btn_read = findViewById(R.id.btn_read);

        tv_data = findViewById(R.id.tv_data);

        btn_insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertData();
            }
        });

        btn_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readData();
            }
        });
    }

    private void insertData() {

        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, "My Title");
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE, "SubTitle");

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(FeedReaderContract.FeedEntry.TABLE_NAME, null, values);

        tv_data.setText("Insert : " + newRowId);
    }

    private void readData() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE,
                FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE + " = ?";
        String[] selectionArgs = { "My Title" };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE + " DESC";

        Cursor cursor = db.query(
                FeedReaderContract.FeedEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        List <Data> itemIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(FeedReaderContract.FeedEntry._ID));

            String title = cursor.getString(cursor.getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE));

            String subTitle = cursor.getString(cursor.getColumnIndex(FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE));

            Data data = new Data();
            data.setId(itemId);
            data.setTitle(title);
            data.setSubTitle(subTitle);

            itemIds.add(data);
        }
        cursor.close();

        for (int i = 0; i < itemIds.size(); i++) {
            Data data = itemIds.get(i);
            tv_data.setText("ID : " + data.getId() + " : Title : " + data.getTitle() + " : SubTitle : " + data.getSubTitle() );
        }
    }

    private void update() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //      New value for one column
        String title = "MyNewTitle";
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE, title);

        // Which row to update, based on the title
        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE + " LIKE ?";
        String[] selectionArgs = { "MyOldTitle" };

        int count = db.update(
                FeedReaderContract.FeedEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        tv_data.setText(" Update : " + count );
    }

    private void delete() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Define 'where' part of query.
        String selection = FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE + " LIKE ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = { "MyTitle" };
        // Issue SQL statement.
        int deletedRows = db.delete(FeedReaderContract.FeedEntry.TABLE_NAME, selection, selectionArgs);
    }


    public class FeedReaderDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "FeedReader.db";

        public FeedReaderDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }

    @Override
    protected void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }
}
