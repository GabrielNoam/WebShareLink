package com.webshare.link;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class LinksSQLiteHelper extends SQLiteOpenHelper 
{
	private static final String TAG = "LinksSQLiteHelper";
	
	
	// 1. Define the database version
	private static final int DATABASE_VERSION = 1;
	
	// 2. Define the database name (can have only one in single SQLiteOpenHelper class)
	private static final String DATABASE_NAME = "links.db";
	
	// 3. The table name (you can have more than one table database in sine) 
	
	// 4. In order to sinegrate with exist android framework components (e.g. CursorAdaper) keep the table key named "_id"
	
	public static final class Links
	{
		public static final String TABLE_NAME = "links";
		
		public static final String ID_COLUMN = BaseColumns._ID;
		
		// 5. The rest of the column names
		public static final String NAME_COLUMN = "name";
		public static final String LINK_COLUMN = "link";
	}
	public LinksSQLiteHelper(Context context) 
	{
		// During runtime we pass the constructor the database version.
		// If database not exist we go into onCreate
		// If database current version not equals to DATABASE_VERSION we go into onUpgrade
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		// The SQLite create command syntax is as follows 
		// "CREATE TABLE <TableName> ( <ColumnName ColumnType [PRIMARY KEY [autoincrement]|NOT NULL] >,* )  
		// @link http://www.tutorialspoint.com/sqlite/
		String CREATE_LINKS_TABLE = 
				"CREATE TABLE " + Links.TABLE_NAME +
				  "(" + Links.ID_COLUMN + " INTEGER PRIMARY KEY autoincrement NOT NULL," + 
				  Links.NAME_COLUMN + " TEXT," +
				  Links.LINK_COLUMN + " TEXT)";
		 
		
		
		
		try
		{
			db.execSQL(CREATE_LINKS_TABLE);
			
			Link l1 = new Link("Google","http://www.google.com");
			Link l2 = new Link("Ynet","http://www.ynet.co.il");
			Link l3 = new Link("Walla","http://www.walla.co.il");
			Link l4 = new Link("Stackoverflow","http://stackoverflow.com/");
			
			db.insert(Links.TABLE_NAME, null, l1.getContentValues());
			db.insert(Links.TABLE_NAME, null, l2.getContentValues());
			db.insert(Links.TABLE_NAME, null, l3.getContentValues());
			db.insert(Links.TABLE_NAME, null, l4.getContentValues());
			
		}
		catch(SQLiteException e)
		{
			e.printStackTrace();
			Log.e(TAG,"Failed to create the table");
			throw new RuntimeException(e.getMessage());
		}	
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int oldVersio, int newVersion) 
	{
		// TODO Auto-generated method stub

	}

}
