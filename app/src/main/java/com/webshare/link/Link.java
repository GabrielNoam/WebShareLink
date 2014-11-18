package com.webshare.link;

import com.webshare.link.LinksSQLiteHelper.Links;

import android.content.ContentValues;
import android.database.Cursor;

public class Link 
{

	private long _id;
	private String name;
	private String link;

    // this is a comment
    
	public Link(Cursor cursor) 
	{
		int idColumn = cursor.getColumnIndex(Links.ID_COLUMN);
		int nameColumn = cursor.getColumnIndex(Links.NAME_COLUMN);
		int linkColumn = cursor.getColumnIndex(Links.LINK_COLUMN);
		
		this._id = cursor.getLong(idColumn);
		this.name = cursor.getString(nameColumn);
		this.link = cursor.getString(linkColumn);
	}
	
	
	public Link(String name, String link) {
		super();
		this.name = name;
		this.link = link;
	}
	
	public ContentValues getContentValues() {
		ContentValues value = new ContentValues();
		value.put(Links.NAME_COLUMN, name);
		value.put(Links.LINK_COLUMN, link);
		
		return value;
	}
	
	public long get_id() {
		return _id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}

	protected void setId(long id) {
		this._id = id;
	}

	
	
}
