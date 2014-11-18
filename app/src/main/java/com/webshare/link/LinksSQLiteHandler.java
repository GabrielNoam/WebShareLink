package com.webshare.link;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.webshare.link.LinksSQLiteHelper.Links;

/**
 * 
 * Supplies Database CRUD methods
 * C = Create
 * R = Retrieve
 * U = Update 
 * D = Delete
 * 
 * The CRUD methods provides with database tables life-cycle for managing the records 
 * 
 * @author gabriel
 *
 */
public class LinksSQLiteHandler 
{
	private final Context context;
	private final LinksSQLiteHelper helper;
	
	public LinksSQLiteHandler(Context context)
	{
		this.context = context;
		helper = new LinksSQLiteHelper(context);
	}
	
	public Link addLink(Link link)
	{
		// We need writable database so we can insert data (make changes)
		SQLiteDatabase database = helper.getWritableDatabase();
			
		long id = database.insert(Links.TABLE_NAME, null, link.getContentValues());
		link.setId(id);
		
		database.close();
		return link;
	}

	public Link updateLink(Link link)
	{
		// We need writable database so we can insert data (make changes)
		SQLiteDatabase database = helper.getWritableDatabase();
			
		long id = database.update(Links.TABLE_NAME, link.getContentValues(), 
				Links.ID_COLUMN+"=?" ,
				new String[]{ String.valueOf(link.get_id())});
		link.setId(id);
		
		database.close();
		return link;
	}
	public Link deleteLink(Link link)
	{
		// We need writable database so we can insert data (make changes)
		SQLiteDatabase database = helper.getWritableDatabase();
			
		long id = database.delete(Links.TABLE_NAME,Links.ID_COLUMN+"=?" ,
				new String[]{ String.valueOf(link.get_id())});
		link.setId(id);
		
		database.close();
		return link;
	}
	public Link retrieveLink(long id)
	{
		// We need writable database so we can insert data (make changes)
		SQLiteDatabase database = helper.getReadableDatabase();
			
		Cursor cursor = 
				database.query(Links.TABLE_NAME, null,
						Links.ID_COLUMN+"=?" ,
						new String[]{ String.valueOf(id)},
				null,null,null);
		
		
		Link link = new Link(cursor);
		database.close();
		
		return link;
	}

	
	public Cursor retrieveLinks()
	{
		// We need writable database so we can insert data (make changes)
		SQLiteDatabase database = helper.getReadableDatabase();
			
		Cursor cursor = 
				database.query(Links.TABLE_NAME, null,
						null,null,null,null,null);
		
		return cursor;
	}

	public SQLiteDatabase getReadableDatabase() 
	{
		return helper.getReadableDatabase();
	}

	public SQLiteDatabase getWritableDatabase() {
		return helper.getWritableDatabase();
	}
}
