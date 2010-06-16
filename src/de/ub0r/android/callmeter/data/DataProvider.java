/*
 * Copyright (C) 2010 Felix Bechstein, The Android Open Source Project
 * 
 * This file is part of SMSdroid.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package de.ub0r.android.callmeter.data;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import de.ub0r.android.lib.Log;

/**
 * @author flx
 */
public final class DataProvider extends ContentProvider {
	/** Tag for output. */
	private static final String TAG = "dp";

	/** Name of the {@link SQLiteDatabase}. */
	private static final String DATABASE_NAME = "callmeter.db";
	/** Version of the {@link SQLiteDatabase}. */
	private static final int DATABASE_VERSION = 1;
	/** Table name for logs. */
	private static final String TABLE_LOGS = "logs";
	/** Table name for plans. */
	private static final String TABLE_PLANS = "plans";
	/** Table name for rules. */
	private static final String TABLE_RULES = "rules";

	/** {@link HashMap} for projection. */
	private static final HashMap<String, String> LOGS_PROJECTION_MAP,
			PLANS_PROJECTION_MAP, RULES_PROJECTION_MAP;

	// common
	public static final String _ID = "_id";
	public static final String _PLAN_ID = "_plan_id";
	public static final String _RULE_ID = "_rule_id";
	// logs
	public static final String _TYPE = "type";
	public static final String _DIRECTION = "direction";
	public static final String _AMOUNT = "amount";
	public static final String _BILL_AMOUNT = "bill_amount";
	public static final String _REMOTE = "remote";
	public static final String _ROAMED = "roamed";
	public static final String _COST = "cost";
	// plans
	public static final String _NAME = "name";
	public static final String _SHORTNAME = "shortname";
	public static final String _LIMIT_TYPE = "limit_type";
	public static final String _LIMIT = "limit";
	public static final String _BILLMODE = "billmode";
	public static final String _BILLDAY = "billday";
	public static final String _BILLPERIOD = "billperiod";
	public static final String _COST_PER_ITEM = "cost_per_item";
	public static final String _COST_PER_AMOUNT = "cost_per_amount";
	public static final String _COST_PER_ITEM_IN_LIMIT = "cost_per_item_in_limit";
	// rules
	public static final String _NOT = "not";
	public static final String _WHAT = "what";
	public static final String _WHAT0 = "what0";
	public static final String _WHAT1 = "what1";

	/** Internal id: logs. */
	private static final int LOGS = 1;
	/** Internal id: single log entry. */
	private static final int LOGS_ID = 2;
	/** Internal id: plans. */
	private static final int PLANS = 3;
	/** Internal id: single plan. */
	private static final int PLANS_ID = 4;
	/** Internal id: rules. */
	private static final int RULES = 5;
	/** Internal id: single rule. */
	private static final int RULES_ID = 6;

	/** Authority. */
	public static final String AUTHORITY = "de.ub0r.android.callmeter."
			+ "provider";

	/** Content {@link Uri} for logs. */
	public static final Uri LOGS_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/logs");
	/** Content {@link Uri} for plans. */
	public static final Uri PLANS_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/plans");
	/** Content {@link Uri} for rules. */
	public static final Uri RULES_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/rules");

	/**
	 * The MIME type of {@link #CONTENT_URI} providing a list of logs.
	 */
	public static final String LOGS_CONTENT_TYPE = // .
	"vnd.android.cursor.dir/vnd.ub0r.log";

	/**
	 * The MIME type of a {@link #CONTENT_URI} single log entry.
	 */
	public static final String LOGS_CONTENT_ITEM_TYPE = // .
	"vnd.android.cursor.item/vnd.ub0r.log";

	/** {@link UriMatcher}. */
	private static final UriMatcher URI_MATCHER;

	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, "logs", LOGS);
		URI_MATCHER.addURI(AUTHORITY, "logs/#", LOGS_ID);
		URI_MATCHER.addURI(AUTHORITY, "plans", PLANS);
		URI_MATCHER.addURI(AUTHORITY, "plans/#", PLANS_ID);
		URI_MATCHER.addURI(AUTHORITY, "rules", RULES);
		URI_MATCHER.addURI(AUTHORITY, "rules/#", RULES_ID);

		LOGS_PROJECTION_MAP = new HashMap<String, String>();
		LOGS_PROJECTION_MAP.put(_ID, _ID);
		LOGS_PROJECTION_MAP.put(_PLAN_ID, _PLAN_ID);
		LOGS_PROJECTION_MAP.put(_TYPE, _TYPE);
		LOGS_PROJECTION_MAP.put(_DIRECTION, _DIRECTION);
		LOGS_PROJECTION_MAP.put(_AMOUNT, _AMOUNT);
		LOGS_PROJECTION_MAP.put(_BILL_AMOUNT, _BILL_AMOUNT);
		LOGS_PROJECTION_MAP.put(_REMOTE, _REMOTE);
		LOGS_PROJECTION_MAP.put(_ROAMED, _ROAMED);
		LOGS_PROJECTION_MAP.put(_COST, _COST);

		PLANS_PROJECTION_MAP = new HashMap<String, String>();
		PLANS_PROJECTION_MAP.put(_ID, _ID);
		PLANS_PROJECTION_MAP.put(_NAME, _NAME);
		PLANS_PROJECTION_MAP.put(_SHORTNAME, _SHORTNAME);
		PLANS_PROJECTION_MAP.put(_LIMIT_TYPE, _LIMIT_TYPE);
		PLANS_PROJECTION_MAP.put(_LIMIT, _LIMIT);
		PLANS_PROJECTION_MAP.put(_BILLMODE, _BILLMODE);
		PLANS_PROJECTION_MAP.put(_BILLDAY, _BILLDAY);
		PLANS_PROJECTION_MAP.put(_BILLPERIOD, _BILLPERIOD);
		PLANS_PROJECTION_MAP.put(_COST_PER_ITEM, _COST_PER_ITEM);
		PLANS_PROJECTION_MAP.put(_COST_PER_AMOUNT, _COST_PER_AMOUNT);
		PLANS_PROJECTION_MAP.put(_COST_PER_ITEM_IN_LIMIT,
				_COST_PER_ITEM_IN_LIMIT);

		RULES_PROJECTION_MAP = new HashMap<String, String>();
		RULES_PROJECTION_MAP.put(_ID, _ID);
		RULES_PROJECTION_MAP.put(_PLAN_ID, _PLAN_ID);
		RULES_PROJECTION_MAP.put(_NOT, _NOT);
		RULES_PROJECTION_MAP.put(_WHAT, _WHAT);
		RULES_PROJECTION_MAP.put(_WHAT0, _WHAT0);
		RULES_PROJECTION_MAP.put(_WHAT1, _WHAT1);
	}

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		/**
		 * Default Constructor.
		 * 
		 * @param context
		 *            {@link Context}
		 */
		DatabaseHelper(final Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onCreate(final SQLiteDatabase db) {
			Log.i(TAG, "create database");
			db.execSQL("CREATE TABLE " + TABLE_LOGS + " (" // .
					+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " // .
					+ _PLAN_ID + " INTEGER," // .
					+ _TYPE + " INTEGER," // .
					+ _DIRECTION + " INTEGER," // .
					+ _AMOUNT + " INTEGER," // .
					+ _BILL_AMOUNT + " INTEGER," // .
					+ _REMOTE + " TEXT,"// .
					+ _ROAMED + " INTEGER," // .
					+ _COST + " INTEGER"// .
					+ ");");

			db.execSQL("CREATE TABLE " + TABLE_PLANS + " (" // .
					+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " // .
					+ _NAME + " TEXT,"// .
					+ _SHORTNAME + " TEXT,"// .
					+ _LIMIT_TYPE + " INTEGER"// .
					+ _LIMIT + " INTEGER"// .
					+ _BILLMODE + " TEXT,"// .
					+ _BILLDAY + " INTEGER"// .
					+ _BILLPERIOD + " INTEGER"// .
					+ _COST_PER_ITEM + " INTEGER"// .
					+ _COST_PER_AMOUNT + " INTEGER"// .
					+ _COST_PER_ITEM_IN_LIMIT + " INTEGER"// .
					+ ");");

			db.execSQL("CREATE TABLE " + TABLE_RULES + " (" // .
					+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " // .
					+ _NAME + " TEXT,"// .
					+ _PLAN_ID + " INTEGER"// .
					+ _NOT + " INTEGER"// .
					+ _WHAT + " INTEGER"// .
					+ _WHAT0 + " INTEGER"// .
					+ _WHAT1 + " INTEGER"// .
					+ ");");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
				final int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLANS);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_RULES);
			this.onCreate(db);
		}
	}

	/** {@link DatabaseHelper}. */
	private DatabaseHelper mOpenHelper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int delete(final Uri uri, final String selection,
			final String[] selectionArgs) {
		if (uri.equals(LOGS_CONTENT_URI)) {
			final SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
			int ret = db.delete(TABLE_LOGS, selection, selectionArgs);
			return ret;
		} else {
			throw new IllegalArgumentException("method not implemented");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getType(final Uri uri) {
		switch (URI_MATCHER.match(uri)) {
		case LOGS:
			return LOGS_CONTENT_TYPE;
		case LOGS_ID:
			return LOGS_CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Uri insert(final Uri uri, final ContentValues values) {
		throw new IllegalArgumentException("method not implemented");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreate() {
		this.mOpenHelper = new DatabaseHelper(this.getContext());
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Cursor query(final Uri uri, final String[] projection,
			final String selection, final String[] selectionArgs,
			final String sortOrder) {
		final SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(TABLE_LOGS);

		switch (URI_MATCHER.match(uri)) {
		case LOGS:
			qb.setProjectionMap(LOGS_PROJECTION_MAP);
			break;
		case LOGS_ID:
			qb.setProjectionMap(LOGS_PROJECTION_MAP);
			qb.appendWhere(_ID + "=" + uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown ORIG_URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = null;
		} else {
			orderBy = sortOrder;
		}

		// Run the query
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, orderBy);

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(this.getContext().getContentResolver(), uri);
		return c;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int update(final Uri uri, final ContentValues values,
			final String selection, final String[] selectionArgs) {
		int tid = -1;
		try {
			tid = Integer.parseInt(uri.getLastPathSegment());
		} catch (NumberFormatException e) {
			Log.e(TAG, "not a number: " + uri, e);
			throw new IllegalArgumentException("method not implemented");
		}
		final SQLiteDatabase db = this.mOpenHelper.getWritableDatabase();
		int ret = db.update(TABLE_LOGS, values, _ID + " = " + tid, null);
		return ret;
	}
}