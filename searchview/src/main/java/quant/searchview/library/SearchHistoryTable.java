package quant.searchview.library;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings({"unused", "WeakerAccess"})
public class SearchHistoryTable {
    public static final String DEFAULT_KEY="all";
    private static int mHistorySize = 10000;
    private static int mConnectionCount = 0;
    private static String mCurrentDatabaseKey;
    private final SearchHistoryDatabase dbHelper;
    private SQLiteDatabase db;

    public SearchHistoryTable(Context mContext) {
        dbHelper = new SearchHistoryDatabase(mContext);
    }

    // FOR onResume AND onPause
    public void open() throws SQLException {
        if (mConnectionCount == 0) {
            db = dbHelper.getWritableDatabase();
        }
        mConnectionCount++;
    }

    public void close() {
        mConnectionCount--;
        if (mConnectionCount == 0) {
            dbHelper.close();
        }
    }

    public void addItem(SearchItem item) {
        addItem(item, mCurrentDatabaseKey);
    }

    public void addItem(SearchItem item, String databaseKey) {
        ContentValues values = new ContentValues();
        if (!checkText(item,databaseKey)) {
            values.put(SearchHistoryDatabase.SEARCH_HISTORY_COLUMN_TEXT, item.text);
            values.put(SearchHistoryDatabase.SEARCH_HISTORY_COLUMN_KEY, databaseKey);
            values.put(SearchHistoryDatabase.SEARCH_HISTORY_COLUMN_CT, item.ct);
            open();
            db.insert(SearchHistoryDatabase.SEARCH_HISTORY_TABLE, null, values);
            close();
        } else {
            values.put(SearchHistoryDatabase.SEARCH_HISTORY_COLUMN_CT, System.currentTimeMillis());
            open();
            db.update(SearchHistoryDatabase.SEARCH_HISTORY_TABLE, values, SearchHistoryDatabase.SEARCH_HISTORY_COLUMN_ID + " = ? ", new String[]{String.valueOf(getItemId(item.text,databaseKey))});
            close();
        }
    }

    private int getItemId(String item,String key) {
        open();
        String query = "SELECT " + SearchHistoryDatabase.SEARCH_HISTORY_COLUMN_ID +
                " FROM " + SearchHistoryDatabase.SEARCH_HISTORY_TABLE +
                " WHERE " + SearchHistoryDatabase.SEARCH_HISTORY_COLUMN_TEXT + " = ? and "+SearchHistoryDatabase.SEARCH_HISTORY_COLUMN_KEY+"=? ";
        Cursor res = db.rawQuery(query, new String[]{item,key});
        res.moveToFirst();
        int id = res.getInt(0);
        close();
        res.close();
        return id;
    }

    private int getLastItemId(String databaseKey) {
        open();
        String sql = "SELECT " + SearchHistoryDatabase.SEARCH_HISTORY_COLUMN_ID + " FROM " + SearchHistoryDatabase.SEARCH_HISTORY_TABLE;
        if (databaseKey != null)
            sql += " WHERE " + SearchHistoryDatabase.SEARCH_HISTORY_COLUMN_KEY + " = ?";
        Cursor res = db.rawQuery(sql, new String[]{ databaseKey});
        res.moveToLast();
        int count = res.getInt(0);
        close();
        res.close();
        return count;
    }

    private boolean checkText(SearchItem item,String key) {
        open();
        String query = "SELECT * FROM " + SearchHistoryDatabase.SEARCH_HISTORY_TABLE + " WHERE " + SearchHistoryDatabase.SEARCH_HISTORY_COLUMN_TEXT + " =? and "+SearchHistoryDatabase.SEARCH_HISTORY_COLUMN_KEY+"=? ";
        Cursor cursor = null;
        boolean hasObject = false;
        try{
            cursor=db.rawQuery(query, new String[]{item.text,key});
            hasObject=cursor.moveToFirst();
        } finally {
            if(null!=cursor){
                cursor.close();
            }
        }
        close();
        return hasObject;
    }

    public List<SearchItem> getAllItems(String databaseKey) {
        mCurrentDatabaseKey = databaseKey;
        List<SearchItem> list = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + SearchHistoryDatabase.SEARCH_HISTORY_TABLE+" WHERE " + SearchHistoryDatabase.SEARCH_HISTORY_COLUMN_KEY + " = '" + databaseKey+"'";
        selectQuery += " ORDER BY " + SearchHistoryDatabase.SEARCH_HISTORY_COLUMN_ID + " DESC LIMIT " + mHistorySize;

        open();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new SearchItem(cursor.getString(1),SearchItem.HISTORY_ITEM));
            } while (cursor.moveToNext());
        }
        cursor.close();
        close();
        return list;
    }

    public void setHistorySize(int historySize) {
        mHistorySize = historySize;
    }

    public void clearDatabase() {
        clearDatabase(null);
    }

    public void clearDatabase(String key) {
        open();
        db.delete(SearchHistoryDatabase.SEARCH_HISTORY_TABLE, SearchHistoryDatabase.SEARCH_HISTORY_COLUMN_KEY + " = ?", new String[]{String.valueOf(key)});
        close();
    }

    public int getItemsCount(String key) {
        open();
        String countQuery = "SELECT * FROM " + SearchHistoryDatabase.SEARCH_HISTORY_TABLE+" where "+SearchHistoryDatabase.SEARCH_HISTORY_COLUMN_KEY+"=?";
        Cursor cursor = db.rawQuery(countQuery, new String[]{key});
        int count = cursor.getCount();
        cursor.close();
        close();
        return count;
    }

}