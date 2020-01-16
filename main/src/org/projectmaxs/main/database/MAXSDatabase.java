/*
    This file is part of Project MAXS.

    MAXS and its modules is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MAXS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MAXS.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.projectmaxs.main.database;

import org.projectmaxs.main.util.Constants;
import org.projectmaxs.shared.global.util.Log;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MAXSDatabase extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = Constants.MAIN_PACKAGE + ".db";

	private static final Log LOG = Log.getLog();

	public static final String TEXT_TYPE = " TEXT";
	public static final String TIMESTAMP_TYPE = " TIMESTAMP";
	public static final String INTEGER_TYPE = " INTEGER";
	public static final String BLOB_TYPE = " BLOB";
	public static final String DROP_TABLE = "DROP TABLE IF EXISTS ";
	public static final String NOT_NULL = " NOT NULL";
	public static final String SEMICOLON_SEP = ";";

	// @formatter:off
	private static final String[] SQL_CREATE_ENTRIES = new String[] {
				CommandTable.CREATE_TABLE,
				ModuleRegistryTable.CREATE_TABLE,
				StatusTable.CREATE_TABLE,
				TransportRegistryTable.CREATE_TABLE,
				CommandHelpTable.CREATE_TABLE,
	};
	private static final String[] SQL_DELETE_ENTRIES = new String[] {
		CommandTable.DELETE_TABLE,
		ModuleRegistryTable.DELETE_TABLE,
		StatusTable.DELETE_TABLE,
		TransportRegistryTable.DELETE_TABLE,
		CommandHelpTable.DELETE_TABLE,
	};
	// @formatter:on

	private static MAXSDatabase sMAXSDatabase;

	protected static MAXSDatabase getInstance(Context context) {
		if (sMAXSDatabase == null) sMAXSDatabase = new MAXSDatabase(context);
		return sMAXSDatabase;
	}

	private MAXSDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		LOG.i("Upgrading MAXS database from version " + oldVersion + " to " + newVersion);
		recreateDatabase(db);
	}

	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		LOG.i("Downgrading MAXS database from version " + oldVersion + " to " + newVersion);
		recreateDatabase(db);
	}

	private static void recreateDatabase(SQLiteDatabase db) {
		deleteTables(db);
		createTables(db);
	}

	private static void createTables(SQLiteDatabase db) {
		for (String s : SQL_CREATE_ENTRIES) {
			db.execSQL(s + SEMICOLON_SEP);
		}
	}

	private static void deleteTables(SQLiteDatabase db) {
		for (String s : SQL_DELETE_ENTRIES) {
			db.execSQL(s + SEMICOLON_SEP);
		}
	}
}
