package com.example.marsphotos.data

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri


class ExchangeRateProvider : ContentProvider() {

    companion object {
        private const val AUTHORITY = "com.example."
        private const val TABLE_NAME = "exchange_rates"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$TABLE_NAME")

        private const val EXCHANGE_RATES = 1
        private const val EXCHANGE_RATE_ID = 2

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, TABLE_NAME, EXCHANGE_RATES)
            addURI(AUTHORITY, "$TABLE_NAME/#", EXCHANGE_RATE_ID)
        }
    }

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(): Boolean {
        dbHelper = DatabaseHelper(context!!)
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        val db = dbHelper.readableDatabase
        val cursor = when (uriMatcher.match(uri)) {
            EXCHANGE_RATES -> db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder)
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = dbHelper.writableDatabase
        val id = db.insert(TABLE_NAME, null, values)
        if (id != -1L) {
            context?.contentResolver?.notifyChange(uri, null)
            return ContentUris.withAppendedId(CONTENT_URI, id)
        }
        return null
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val db = dbHelper.writableDatabase
        val count = db.update(TABLE_NAME, values, selection, selectionArgs)
        context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val db = dbHelper.writableDatabase
        val count = db.delete(TABLE_NAME, selection, selectionArgs)
        context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            EXCHANGE_RATES -> "vnd.android.cursor.dir/vnd.$AUTHORITY.$TABLE_NAME"
            EXCHANGE_RATE_ID -> "vnd.android.cursor.item/vnd.$AUTHORITY.$TABLE_NAME"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    private class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "exchange_rates.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE $TABLE_NAME (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "currency TEXT NOT NULL, " +
                        "rate REAL NOT NULL, " +
                        "timestamp TEXT NOT NULL)"
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }
    }
}
