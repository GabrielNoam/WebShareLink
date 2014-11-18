   package com.webshare.link;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class LinksContentProvider extends ContentProvider 
{
	private final static String TAG = "LinksContentProvider";
	private LinksSQLiteHandler handler = null;

	private final static String AUTHORITY = "com.webshare.link";
	
	/*
    * The scheme part for this provider's URI
    */
   private static final String SCHEME = "content://";

   /**
    * Path parts for the URIs
    */

   /**
    * Path part for the Links URI
    * Example:
    * content://com.webshare.link/links : we expect to get cursor to all links in the content provider
    */
   private static final String PATH_LINKS = "/links";

   /**
    * Path part for the Link ID URI
    * content://com.webshare.link/link/12 : we expect to get cursor to link that its id = 12
    */
   private static final String PATH_LINK_ID = "/link/";

   /**
    * 0-relative position of a note ID segment in the path part of a note ID URI
    */
   public static final int LINK_ID_PATH_POSITION = 1;

   /**
    * The content:// style URL for this table
    */
   public static final Uri CONTENT_URI =  Uri.parse(SCHEME + AUTHORITY + PATH_LINKS);

   /**
    * The content URI base for a single note. Callers must
    * append a numeric note id to this Uri to retrieve a note
    */
   public static final Uri CONTENT_ID_URI_BASE
       = Uri.parse(SCHEME + AUTHORITY + PATH_LINK_ID);
   
	private static final int LINKS_ID_PATH_POSITION = 1;
	/*
	 * Constants used by the Uri matcher to choose an action based on the
	 * pattern of the incoming URI
	 */
	// The incoming URI matches the Links URI pattern
	private static final int LINKS = 1;

	// The incoming URI matches the Link ID URI pattern
	private static final int LINK_ID = 2;

	private static final UriMatcher uriMatcher;
	
	static 
	{
		/*
		 * Creates and initializes the URI matcher
		 */
		// Create a new instance
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		// Add a pattern that routes URIs terminated with "links" to a LINKS
		// operation
		uriMatcher.addURI(AUTHORITY, PATH_LINKS, LINKS);

		// Add a pattern that routes URIs terminated with "links" plus an
		// integer
		// to a link ID operation
		uriMatcher.addURI(AUTHORITY, PATH_LINK_ID + "/#", LINK_ID);
	}

	 /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/"+AUTHORITY;

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
     * note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/"+AUTHORITY;

    
	@Override
	public boolean onCreate() {
		android.os.Process
				.setThreadPriority(android.os.Process.THREAD_PRIORITY_LOWEST);

		handler = new LinksSQLiteHandler(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		/**
		 * Choose the projection and adjust the "where" clause based on URI
		 * pattern-matching.
		 */
		String finalWhere = "";
		switch (uriMatcher.match(uri)) {
		// If the incoming URI is for notes, chooses the Notes projection
		case LINKS:
			break;

		/*
		 * If the incoming URI is for a single note identified by its ID,
		 * chooses the note ID projection, and appends "_ID = <noteID>" to the
		 * where clause, so that it selects that single note
		 */
		case LINK_ID:
			// This will retreive the first component after the last part of the 
			// uri
			String id = uri.getPathSegments().get(LINKS_ID_PATH_POSITION);
			
			// EXAMPLE: URI = content://com.webshare.link/link/12
			// Note: the matched part is <content://com.webshare.link/link/>
			// Note: the leftover in path after match is "12" 
			// String val = URI.getPathSegments().get(1);
			// val = "12"
			
			finalWhere = LinksSQLiteHelper.Links.ID_COLUMN + // the name of the
					"=" + id;
					// the position of the note ID itself in the incoming URI
					
			
			if (selection != null && selection.trim().length()>0)
			{
				finalWhere = selection +" AND "+finalWhere;
			}
			break;

		default:
			// If the URI doesn't match any of the known patterns, throw an
			// exception.
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// Opens the database object in "read" mode, since no writes need to be
		// done.
		SQLiteDatabase database = handler.getReadableDatabase();

		
		/*
		 * Performs the query. If no problems occur trying to read the database,
		 * then a Cursor object is returned; otherwise, the cursor variable
		 * contains null. If no records were selected, then the Cursor object is
		 * empty, and Cursor.getCount() returns 0.
		 */
		Cursor c = database.query(
				LinksSQLiteHelper.Links.TABLE_NAME,
				projection, // The columns to return from the query
				finalWhere, // The columns for the where clause
				selectionArgs, // The values for the where clause
				null, // don't group the rows
				null, // don't filter by row groups
				null // The sort order
				);

		// Tells the Cursor what URI to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;

	}

	/**
	    * This is called when a client calls {@link android.content.ContentResolver#getType(Uri)}.
	    * Returns the MIME data type of the URI given as a parameter.
	    *
	    * @param uri The URI whose MIME type is desired.
	    * @return The MIME type of the URI.
	    * @throws IllegalArgumentException if the incoming URI pattern is invalid.
	    */
	   @Override
	   public String getType(Uri uri) {

	       /**
	        * Chooses the MIME type based on the incoming URI pattern
	        */
	       switch (uriMatcher.match(uri)) {

	           // If the pattern is for notes or live folders, returns the general content type.
	           case LINKS:
	               return CONTENT_TYPE;

	           // If the pattern is for note IDs, returns the note ID content type.
	           case LINK_ID:
	               return CONTENT_ITEM_TYPE;

	           // If the URI pattern doesn't match any permitted patterns, throws an exception.
	           default:
	               throw new IllegalArgumentException("Unknown URI " + uri);
	       }
	    }

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {

        // Validates the incoming URI. Only the full provider URI is allowed for inserts.
        if (uriMatcher.match(uri) != LINKS) 
        {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // A map to hold the new record's values.
        ContentValues values;

        // If the incoming values map is not null, uses it for the new values.
        if (initialValues != null) 
        {
            values = new ContentValues(initialValues);
        } 
        else 
        {
            // Otherwise, create a new value map
            values = new ContentValues();
        }

        // If the values map doesn't contain the link value, throws IllegalArgumentException.
        if (values.containsKey(LinksSQLiteHelper.Links.LINK_COLUMN) == false) {
        	throw new IllegalArgumentException("Missing value for column " + LinksSQLiteHelper.Links.LINK_COLUMN);
        }
        
        // If the values map doesn't contain the link name value, throws IllegalArgumentException.
        if (values.containsKey(LinksSQLiteHelper.Links.NAME_COLUMN) == false) {
        	throw new IllegalArgumentException("Missing value for column " + LinksSQLiteHelper.Links.NAME_COLUMN);
        }

    
        // Opens the database object in "write" mode.
        SQLiteDatabase db = handler.getWritableDatabase();

        // Performs the insert and returns the ID of the new note.
        long rowId = db.insert(
        		LinksSQLiteHelper.Links.TABLE_NAME,        // The table to insert into.
        		null,
        		values                        // A map of column names, and the values to insert
                                             // into the columns.
        );

        // If the insert succeeded, the row ID exists.
        if (rowId > 0) {
            // Creates a URI with the note ID pattern and the new row ID appended to it.
            Uri noteUri = ContentUris.withAppendedId(CONTENT_ID_URI_BASE, rowId);

            // Notifies observers registered against this provider that the data changed.
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        // If the insert didn't succeed, then the rowID is <= 0. Throws an exception.
        throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}



}
