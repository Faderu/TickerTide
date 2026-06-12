package com.example.tickertide.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.tickertide.model.Stock;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHelper - Implementasi SQLiteOpenHelper untuk mengelola database lokal.
 *
 * Tabel yang dikelola:
 * - watchlist : Menyimpan data saham yang terakhir dilihat / ditambah ke watchlist
 *
 * Operasi yang tersedia:
 * - insertOrUpdateStock()    : Menyimpan / update data saham
 * - getAllWatchlistStocks()  : Mengambil semua data dari watchlist
 * - getStockBySymbol()       : Mengambil satu data saham berdasarkan simbol
 * - deleteStock()            : Menghapus satu saham dari watchlist
 * - clearAllStocks()         : Menghapus semua data (untuk refresh)
 * - isWatchlistEmpty()       : Cek apakah watchlist kosong
 *
 * CATATAN: Semua operasi DB harus dipanggil dari background thread menggunakan
 *          AppExecutors untuk menghindari blocking di Main Thread.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DB_NAME    = "tickertide.db";
    private static final int    DB_VERSION = 1;

    // SQL untuk membuat tabel watchlist
    private static final String CREATE_TABLE_WATCHLIST =
            "CREATE TABLE IF NOT EXISTS " + Stock.TABLE_NAME + " (" +
            Stock.COL_ID         + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            Stock.COL_SYMBOL     + " TEXT NOT NULL UNIQUE, " +
            Stock.COL_NAME       + " TEXT, " +
            Stock.COL_PRICE      + " REAL DEFAULT 0, " +
            Stock.COL_CHANGE     + " REAL DEFAULT 0, " +
            Stock.COL_HIGH       + " REAL DEFAULT 0, " +
            Stock.COL_LOW        + " REAL DEFAULT 0, " +
            Stock.COL_VOLUME     + " INTEGER DEFAULT 0, " +
            Stock.COL_MARKET_CAP + " INTEGER DEFAULT 0, " +
            Stock.COL_TIMESTAMP  + " INTEGER DEFAULT 0" +
            ");";

    // Singleton instance
    private static DatabaseHelper instance;

    /**
     * Singleton pattern - pastikan hanya ada satu koneksi database
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_WATCHLIST);
        Log.d(TAG, "Database & tabel watchlist berhasil dibuat.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Untuk versi baru: drop tabel lama dan buat ulang
        db.execSQL("DROP TABLE IF EXISTS " + Stock.TABLE_NAME);
        onCreate(db);
        Log.d(TAG, "Database diupgrade dari v" + oldVersion + " ke v" + newVersion);
    }

    // ================================================================
    // OPERASI WRITE
    // ================================================================

    /**
     * Insert saham baru atau update jika simbol sudah ada (UPSERT).
     * Gunakan dari background thread via AppExecutors.
     *
     * @param stock Data saham yang akan disimpan
     * @return rowId jika insert berhasil, -1 jika gagal
     */
    public long insertOrUpdateStock(Stock stock) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = stockToContentValues(stock);

        // CONFLICT_REPLACE: jika UNIQUE constraint (symbol) bertabrakan, replace data lama
        long result = db.insertWithOnConflict(
                Stock.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );

        if (result == -1) {
            Log.e(TAG, "Gagal menyimpan saham: " + stock.getSymbol());
        } else {
            Log.d(TAG, "Berhasil simpan/update saham: " + stock.getSymbol());
        }
        return result;
    }

    /**
     * Simpan banyak saham sekaligus dalam satu transaksi (efisien).
     *
     * @param stocks List saham yang akan disimpan
     */
    public void insertOrUpdateStocks(List<Stock> stocks) {
        if (stocks == null || stocks.isEmpty()) return;

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (Stock stock : stocks) {
                ContentValues values = stockToContentValues(stock);
                db.insertWithOnConflict(
                        Stock.TABLE_NAME,
                        null,
                        values,
                        SQLiteDatabase.CONFLICT_REPLACE
                );
            }
            db.setTransactionSuccessful();
            Log.d(TAG, stocks.size() + " saham berhasil disimpan ke lokal.");
        } catch (Exception e) {
            Log.e(TAG, "Error saat batch insert: " + e.getMessage());
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Hapus satu saham dari watchlist berdasarkan simbol.
     *
     * @param symbol Simbol saham (mis. "AAPL")
     * @return jumlah baris yang dihapus
     */
    public int deleteStock(String symbol) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(
                Stock.TABLE_NAME,
                Stock.COL_SYMBOL + " = ?",
                new String[]{symbol}
        );
        Log.d(TAG, "Hapus saham " + symbol + ", rows affected: " + rows);
        return rows;
    }

    /**
     * Hapus semua data dari tabel watchlist (untuk fresh refresh).
     */
    public void clearAllStocks() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(Stock.TABLE_NAME, null, null);
        Log.d(TAG, "Semua data watchlist dihapus.");
    }

    // ================================================================
    // OPERASI READ
    // ================================================================

    /**
     * Mengambil semua saham dari tabel watchlist, diurutkan berdasarkan timestamp terbaru.
     * Gunakan dari background thread via AppExecutors.
     *
     * @return List<Stock> semua data saham lokal
     */
    public List<Stock> getAllWatchlistStocks() {
        List<Stock> stocks = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                Stock.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                Stock.COL_TIMESTAMP + " DESC"  // urutan: terbaru di atas
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                stocks.add(cursorToStock(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        Log.d(TAG, "Loaded " + stocks.size() + " saham dari lokal DB.");
        return stocks;
    }

    /**
     * Mengambil satu saham berdasarkan simbol.
     *
     * @param symbol Simbol saham (mis. "AAPL")
     * @return Stock object atau null jika tidak ditemukan
     */
    public Stock getStockBySymbol(String symbol) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                Stock.TABLE_NAME,
                null,
                Stock.COL_SYMBOL + " = ?",
                new String[]{symbol},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            Stock stock = cursorToStock(cursor);
            cursor.close();
            return stock;
        }

        return null;
    }

    /**
     * Cek apakah watchlist kosong.
     *
     * @return true jika tidak ada data di watchlist
     */
    public boolean isWatchlistEmpty() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + Stock.TABLE_NAME, null
        );
        if (cursor != null && cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            cursor.close();
            return count == 0;
        }
        return true;
    }

    // ================================================================
    // HELPER METHODS
    // ================================================================

    /**
     * Mengkonversi Stock object ke ContentValues untuk operasi DB.
     */
    private ContentValues stockToContentValues(Stock stock) {
        ContentValues values = new ContentValues();
        values.put(Stock.COL_SYMBOL,     stock.getSymbol());
        values.put(Stock.COL_NAME,       stock.getCompanyName());
        values.put(Stock.COL_PRICE,      stock.getCurrentPrice());
        values.put(Stock.COL_CHANGE,     stock.getChangePercent());
        values.put(Stock.COL_HIGH,       stock.getDayHigh());
        values.put(Stock.COL_LOW,        stock.getDayLow());
        values.put(Stock.COL_VOLUME,     stock.getVolume());
        values.put(Stock.COL_MARKET_CAP, stock.getMarketCap());
        values.put(Stock.COL_TIMESTAMP,  System.currentTimeMillis());
        return values;
    }

    /**
     * Mengkonversi Cursor row ke Stock object.
     */
    private Stock cursorToStock(Cursor cursor) {
        Stock stock = new Stock();

        int idxSymbol    = cursor.getColumnIndexOrThrow(Stock.COL_SYMBOL);
        int idxName      = cursor.getColumnIndexOrThrow(Stock.COL_NAME);
        int idxPrice     = cursor.getColumnIndexOrThrow(Stock.COL_PRICE);
        int idxChange    = cursor.getColumnIndexOrThrow(Stock.COL_CHANGE);
        int idxHigh      = cursor.getColumnIndexOrThrow(Stock.COL_HIGH);
        int idxLow       = cursor.getColumnIndexOrThrow(Stock.COL_LOW);
        int idxVolume    = cursor.getColumnIndexOrThrow(Stock.COL_VOLUME);
        int idxMarketCap = cursor.getColumnIndexOrThrow(Stock.COL_MARKET_CAP);
        int idxTimestamp = cursor.getColumnIndexOrThrow(Stock.COL_TIMESTAMP);

        stock.setSymbol(cursor.getString(idxSymbol));
        stock.setCompanyName(cursor.getString(idxName));
        stock.setCurrentPrice(cursor.getDouble(idxPrice));
        stock.setChangePercent(cursor.getDouble(idxChange));
        stock.setDayHigh(cursor.getDouble(idxHigh));
        stock.setDayLow(cursor.getDouble(idxLow));
        stock.setVolume(cursor.getLong(idxVolume));
        stock.setMarketCap(cursor.getLong(idxMarketCap));
        stock.setTimestamp(cursor.getLong(idxTimestamp));

        return stock;
    }
}
