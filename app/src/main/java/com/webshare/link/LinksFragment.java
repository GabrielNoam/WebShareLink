
package com.webshare.link;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class LinksFragment extends ListFragment{
    OnHeadlineSelectedListener mCallback;

    private CursorAdapter adapter = null;

    private static int loaderId = 0;
    // The container Activity must implement this interface so the frag can deliver messages
    public interface OnHeadlineSelectedListener {
        /** Called by HeadlinesFragment when a list item is selected */
        public void onLinkSelected(Link link);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We need to use a different list item layout for devices older than Honeycomb
        int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                android.R.layout.simple_list_item_activated_1 : android.R.layout.simple_list_item_1;
        
        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        Bundle bundle = null;
        getLoaderManager().initLoader(loaderId++, bundle, new LoaderManager.LoaderCallbacks<Cursor>()
        {

        	@Override
        	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) 
        	{
//        		AsyncTaskLoader<Cursor> myLoader = new AsyncTaskLoader<Cursor>(getActivity())
//        		{
//        			@Override
//        			public Cursor loadInBackground() 
//        			{
//        				// TODO: Also load pictures from the cloud ...
//        				return getActivity().getContentResolver().query
//        						( LinksContentProvider.CONTENT_URI,
//        				                null, null, null,null);
//        			}
//        
//        		};
        		
        		
        		return new CursorLoader
        			   (getActivity(), 
        					   LinksContentProvider.CONTENT_URI,
                        null, null, null,null)
        		{
        			@Override
        			public Cursor loadInBackground() 
        			{
        				// TODO: Also load pictures from the cloud ...
        				
        				return super.loadInBackground();
        			}
        		};
        		
//        		return myLoader;
        	}

        	@Override
        	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) 
        	{
        		adapter = new CursorAdapter(getActivity(), cursor, true) {
        			
        			@Override
        			public View newView(Context context, Cursor cursor, ViewGroup group) 
        			{
        				return new TextView(getActivity());
        			}
        			
        			@Override
        			public void bindView(View view, Context context, Cursor cursor) 
        			{
        				Link link = new Link(cursor);
        				TextView textView = (TextView)view;
        				textView.setText(link.getName());
        				
        				textView.setTag(link);
        			}
        		};
                
                setListAdapter(adapter);
             
        	}

        	@Override
        	public void onLoaderReset(Loader<Cursor> arg0) {
        		if(adapter != null)
        		{
        			adapter.swapCursor(null); 
        		}
        		
        	}
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // When in two-pane layout, set the listview to highlight the selected list item
        // (We do this during onStart because at the point the listview is available.)
        if (getFragmentManager().findFragmentById(R.id.links_fragment) != null) {
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (OnHeadlineSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onListItemClick(ListView l, View view, int position, long id) {
        
    	Link link =  (Link)view.getTag();
    	// Notify the parent activity of selected item
        mCallback.onLinkSelected(link);
        
        // Set the item as checked to be highlighted when in two-pane layout
        getListView().setItemChecked(position, true);
    }

    public void onLinksUpdated() 
    {
    	 Cursor newCursor = getActivity().getContentResolver().query
         		(LinksContentProvider.CONTENT_URI, null, null, null, null);
        
    	 ((CursorAdapter)getListAdapter()).swapCursor(newCursor);
    	 ((CursorAdapter)getListAdapter()).notifyDataSetChanged();
    	 
    }

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	}