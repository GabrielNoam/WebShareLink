package com.webshare.link;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnDismissListener;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;

public class LinksActivity extends ActionBarActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {
	private static final String TAG = "LinksActivity";
	private static final String HOME_PAGE = "http://www.google.com";
	private static final String HTTP = "http://";

	private AlertDialog shareDialog;
	private ListView linksListView;
	private CursorAdapter linksCursorAdapter;
	protected LinksSQLiteHandler handler;

	private ImageView mSplashImageView;
	private SearchView searchView;
	private DrawerLayout mDrawerLayout;
	private View mDrawerView;

	private ListView mDrawerCategoriesList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mTitle;
	private ImageButton webFrImageView;
	private ImageButton btnSeach;
	private ImageButton btnHome;
	private EditText webUrl;
	private WebView myWebView;
	private ImageButton webBackImageView;

	public static Boolean webViewStarted;

	private final Map<Long, Boolean> selectedContactsMap = new HashMap<Long, Boolean>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.links);

		handler = new LinksSQLiteHandler(this);

		ActionBar actionBar = getActionBar();
		actionBar.setTitle("WebShareLink");
		actionBar.setSubtitle("Your link for success");

		// actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setHomeButtonEnabled(true);
		// actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));
		getOverflowMenu();

		mTitle = "Web Share Link";// getTitle();
		mSplashImageView = (ImageView) findViewById(R.id.splashImageView);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerView = findViewById(R.id.drawer);
 
		linksListView = (ListView) findViewById(R.id.linksListView);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// set up the drawer's list view with items and click listener

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new CustomActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */);

		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
		}

		initWebViewer();

		getSupportLoaderManager().initLoader(0, null, this);
		getSupportLoaderManager().initLoader(1, null,
				new LoaderManager.LoaderCallbacks<Cursor>() {

					@Override
					public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
						return new CursorLoader(LinksActivity.this,
								LinksContentProvider.CONTENT_URI, null, null,
								null, null);
					}

					@Override
					public void onLoadFinished(Loader<Cursor> loader,
							Cursor cursor) {
						
// Zion: Here you can extract the search view ""@+id/linkSerchView" from mDrawerView and make the init
// ===================================================================================================						
						linksCursorAdapter = new CursorAdapter(
								LinksActivity.this, cursor, true) {

							@Override
							public View newView(Context context, Cursor cursor,
									ViewGroup group) {
								return getLayoutInflater().inflate(R.layout.link_row, null);
							}

							@Override
							public void bindView(final View view, Context context,
									Cursor cursor) {
								
								final Link link = new Link(cursor);
								
								TextView linkNameTextView = (TextView) 
										view.findViewById(R.id.linkNameTextView);
								
								Button linkPopupButton = (Button)
										view.findViewById(R.id.linkPopupButton);
								
								linkNameTextView.setText(link.getName());
								view.setTag(link);
								linkPopupButton.setTag(link);
								
								linkNameTextView.setOnClickListener(new OnClickListener() {
									
									@Override
									public void onClick(View v) {
										updateWebView(link);
									}
								});
								
								linkPopupButton.setOnClickListener(new OnClickListener() {
									
									@Override
									public void onClick(View v) {
										showPopupMenu(link, view);
									}
								});
							}
						};

						linksListView.setAdapter(linksCursorAdapter);
						linksListView
								.setOnItemClickListener(new OnItemClickListener() {

									@Override
									public void onItemClick(
											AdapterView<?> parent, View view,
											int position, long id) {
										Link link = (Link) view.getTag();

									}
								});

					}

					@Override
					public void onLoaderReset(Loader<Cursor> arg0) {
						if (linksCursorAdapter != null) {
							linksCursorAdapter.swapCursor(null);
						}

					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.links_menu, menu);

		return true;
	}

	@Override
	public void onBackPressed() {
		if (((CustomActionBarDrawerToggle) mDrawerToggle).isClosed()) {
			super.onBackPressed();
		} else {
			mDrawerLayout.closeDrawer(mDrawerView);
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		switch (item.getItemId()) {
		case R.id.share_link:

			WebViewerFragment webViewerFrag = (WebViewerFragment) getFragmentManager()
					.findFragmentById(R.id.web_viewer_fragment);

			if (webViewerFrag == null) {
				webViewerFrag = (WebViewerFragment) getFragmentManager()
						.findFragmentById(R.id.fragment_container);

			}
			String currentLink = null;
			if (webViewerFrag != null) {
				currentLink = webViewerFrag.getCurrentLink();
				// shareDialog.setMessage(currentLink);
				shareDialog.show();
			} else {
				Toast.makeText(this, "No link is selected", Toast.LENGTH_LONG)
						.show();
			}

			break;

		case R.id.add_link:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Add Link!");
			builder.setMessage("Please enter your link");
			final View view = getLayoutInflater().inflate(
					R.layout.dialog_layout, null);
			builder.setView(view);

			builder.setPositiveButton("Add",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							EditText nameEditText = (EditText) view
									.findViewById(R.id.nameEditText);
							EditText linkEditText = (EditText) view
									.findViewById(R.id.linkEditText);

							String name = nameEditText.getText().toString();
							String link = linkEditText.getText().toString();

							// Roman: this checks whether link string starts
							// with "http" and if not
							// we will add the "http" to the link
							if (!link.startsWith("http://")) {

								link = "http://" + link;

							}

							// Roman: this checks whether the link string
							// contains spaces
							// if yes we will remove them
							if (link.contains("%20")) {

								link.replace("%20", "");
							}

							if (link.contains(" ")) {

								link.replace(" ", "");
							}

							Link l = new Link(name, link);

							// l = handler.addLink(l);
							ContentValues values = l.getContentValues();

							Uri uri = getContentResolver().insert(
									LinksContentProvider.CONTENT_URI, values);

							Toast.makeText(LinksActivity.this, uri.toString(),
									Toast.LENGTH_LONG).show();

							// Capture the article fragment from the activity
							// layout
							LinksFragment linksFrag = (LinksFragment) getFragmentManager()
									.findFragmentByTag("LinksFragment");

							if (linksFrag != null) {
								linksFrag.onLinksUpdated();
							}

						}
					});
			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});

			Dialog dialog = builder.create();

			dialog.show();

			break;

		default:
			break;
		}
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}

	private void showPopupMenu(final Link link, final View view) {
        
		final Drawable origBackgroundDrawable = view.getBackground();
        // Retrieve the clicked item from view's tag
        final PopupMenu popup = new PopupMenu(this, view)
        {
			@Override
			public void show()
			{
				view.setBackgroundColor(Color.CYAN);
		        super.show();
			}
        };

        popup.setOnDismissListener(new OnDismissListener()
		{
			@SuppressWarnings("deprecation")
			@Override
			public void onDismiss(PopupMenu menu)
			{
				view.setBackgroundDrawable(origBackgroundDrawable);
			}
		});
        
        // Inflate our menu resource into the PopupMenu's Menu
        popup.getMenuInflater().inflate(R.menu.link_popup, popup.getMenu());

        // Set a listener so we are notified if a menu item is clicked
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) 
            {
            	// Eli: Please implement all cases
            	// ===============================
               	switch (menuItem.getItemId()) {
                    case R.id.remove_link:
                    	return true;
                     	
                    case R.id.edit_link:
                    	return true;
                    
                    case R.id.share_link:
                        return true;    
                }
                return false;
            }
            
            
        });

        // Finally show the PopupMenu
        popup.show();
    }
	public boolean onQueryTextChange(String newText) {
		// mStatusView.setText("Query = " + newText);
		return false;
	}

	public boolean onQueryTextSubmit(String query) {
		// mStatusView.setText("Query = " + query + " : submitted");
		return false;
	}

	public boolean onClose() {
		// mStatusView.setText("Closed!");
		return false;
	}

	protected boolean isAlwaysExpanded() {
		return false;
	}

	private void selectItem(int position) {

		// update selected item and title, then close the drawer
		mDrawerCategoriesList.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerCategoriesList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	private void getOverflowMenu() {

		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private class CustomActionBarDrawerToggle extends ActionBarDrawerToggle {
		private Activity activity;
		private DrawerLayout drawerLayout;
		private boolean isClosed = true;

		public CustomActionBarDrawerToggle(Activity activity,
				DrawerLayout drawerLayout, int drawerImageRes,
				int openDrawerContentDescRes, int closeDrawerContentDescRes) {
			super(activity, drawerLayout, drawerImageRes,
					openDrawerContentDescRes, closeDrawerContentDescRes);

			this.activity = activity;
			this.drawerLayout = drawerLayout;
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			if (item != null && item.getItemId() == android.R.id.home) {
				if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
					drawerLayout.closeDrawer(Gravity.LEFT);
				} else {
					drawerLayout.openDrawer(Gravity.LEFT);
				}
			}
			return false;
		}

		@Override
		public void onDrawerClosed(View view) {
			// activity.getActionBar().setTitle(mTitle);
			activity.invalidateOptionsMenu(); // creates call to
			// onPrepareOptionsMenu()
			isClosed = true;
		}

		@Override
		public void onDrawerOpened(View drawerView) {
			// activity.getActionBar().setTitle(mDrawerTitle);
			activity.invalidateOptionsMenu(); // creates call to
			// onPrepareOptionsMenu()
			isClosed = false;
		}

		public boolean isClosed() {
			return isClosed;
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(this, Contacts.CONTENT_URI, null, null, null,
				null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Share link");
		View selectContactView = getLayoutInflater().inflate(
				R.layout.contact_select_layout, null);

		builder.setView(selectContactView);

		final AutoCompleteTextView selectContactAutoCompleteTextView = (AutoCompleteTextView) selectContactView
				.findViewById(R.id.selectContactAutoCompleteTextView);

		selectContactAutoCompleteTextView.setThreshold(1);

		Cursor autoCompleteCursor = getContentResolver().query(
				Contacts.CONTENT_URI, null, null, null, null);

		// TODO: set the adapter for the selectContactAutoCompleteTextView
		// (Ha-ha :-) ha ha ha)
		ListView contactsListView = (ListView) selectContactView
				.findViewById(R.id.selectContactListView);

		final CursorAdapter adapter = new CursorAdapter(this, cursor, true) {

			@Override
			public void bindView(View view, Context arg1, Cursor cursor) {
				final long id = cursor.getLong(cursor
						.getColumnIndex(Contacts._ID));
				final String displayName = cursor.getString(cursor
						.getColumnIndex(Contacts.DISPLAY_NAME));

				TextView displayNameTextView = (TextView) view
						.findViewById(R.id.displayNameTextView);
				final CheckBox selectContactCheckBox = (CheckBox) view
						.findViewById(R.id.selectContactCheckBox);

				displayNameTextView.setText(displayName);

				boolean isChecked = selectedContactsMap.containsKey(id) ? selectedContactsMap
						.get(id) : false;

				selectContactCheckBox.setChecked(isChecked);

				selectContactCheckBox
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								boolean isChecked = selectContactCheckBox
										.isChecked();
								if (!isChecked
										&& selectedContactsMap.containsKey(id)) {
									Log.d(TAG, "Remove id: " + id + ": "
											+ displayName);
									selectedContactsMap.remove(id);
								} else {
									Log.d(TAG, "Select id: " + id + ": "
											+ displayName + " : " + isChecked);
									selectedContactsMap.put(id, isChecked);
								}

								// TODO: is is checked and contact have several
								// phones, open another single choice dialog for
								// this
							}
						});

			}

			@Override
			public View newView(Context context, Cursor arg1, ViewGroup arg2) {
				View selectContactItemView = getLayoutInflater().inflate(
						R.layout.contact_select_item_layout, null);
				return selectContactItemView;
			}

		};

		selectContactAutoCompleteTextView
				.addTextChangedListener(new TextWatcher() {

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
						String text = s.toString();

						Cursor autoCompleteCursor = getContentResolver()
								.query(Contacts.CONTENT_URI,
										null,
										Contacts.DISPLAY_NAME + " LIKE '"
												+ text + "%'", null, null);

						adapter.swapCursor(autoCompleteCursor);
						adapter.notifyDataSetChanged();

					}

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
					}

					@Override
					public void afterTextChanged(Editable editable) {

					}
				});

		contactsListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				WebViewerFragment webViewerFrag = (WebViewerFragment) getFragmentManager()
						.findFragmentById(R.id.web_viewer_fragment);

				String currentLink = null;
				if (webViewerFrag != null) {
					currentLink = webViewerFrag.getCurrentLink();
					long _id = (Long) view.getTag();
					share(_id, currentLink);

				} else {
					Toast.makeText(LinksActivity.this, "No link is found",
							Toast.LENGTH_LONG).show();
				}

				shareDialog.dismiss();

			}
		});
		contactsListView.setAdapter(adapter);

		builder.setView(selectContactView);
		builder.setPositiveButton("Share!",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
		builder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(LinksActivity.this, ":-(",
								Toast.LENGTH_SHORT).show();
					}
				});

		shareDialog = builder.create();

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub

	}

	protected void share(long _id, final String currentLink) {
		// TODO: Get phone number by contact id
		// Send SMS of the link to that given phone number in case server do not
		// have it registred alread

		// 1. Extract phone number from contact id.
		List<String> phones = phoneFromContactId(_id);
		Log.d(TAG, phones.toString());

		Dialog d = null;
		if (phones.isEmpty()) {
			d = getCustomizedDialog("Share Link", "No phones are defined",
					null, null);
		} else if (phones.size() == 1) {
			final String phoneNumber = phones.get(0);
			d = getCustomizedDialog("Share Link", "Message is shared with "
					+ phoneNumber, null, null);

			sendSMS(phoneNumber, currentLink);
		} else {
			final String[] items = new String[phones.size()];
			phones.toArray(items);
			d = getCustomizedDialog("Share Link",
					"Please select a number for share", items,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							final String phoneNumber = items[which];

							sendSMS(phoneNumber, currentLink);
							dialog.dismiss();
						}
					});
		}
		d.show();

		// 2. Go over all phones in the list and for each check with the server
		// if it has a user for

	}

	private List<String> phoneFromContactId(long contactId) {
		List<String> phonesNumbers = new ArrayList<String>();
		String number = null;
		Cursor phones = getContentResolver().query(Phone.CONTENT_URI, null,
				Phone.CONTACT_ID + " = ?",
				new String[] { String.valueOf(contactId) }, null, null);
		while (phones.moveToNext()) {
			number = phones.getString(phones.getColumnIndex(Phone.NUMBER));
			phonesNumbers.add(number);
		}

		phones.close();

		return phonesNumbers;
	}

	/**
	 * Based on color palate from http://colorschemedesigner.com/csd-3.5/
	 * 
	 * @param title
	 * @param message
	 * @return
	 */

	private Dialog getCustomizedDialog(String title, String message,
			String[] items, DialogInterface.OnClickListener listener) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		View customTitleView = getLayoutInflater().inflate(
				R.layout.cutom_title_view, null);
		TextView titleTextView = (TextView) customTitleView
				.findViewById(R.id.titleTextView);
		titleTextView.setText(title);

		builder.setCustomTitle(customTitleView);

		if (items != null) {
			builder.setSingleChoiceItems(items, 0, listener);
		} else {
			View customMessageView = getLayoutInflater().inflate(
					R.layout.cutom_message_view, null);
			TextView messageTextView = (TextView) customMessageView
					.findViewById(R.id.messageTextView);
			messageTextView.setText(message);
			builder.setView(customMessageView);
		}

		return builder.create();
	}

	private void sendSMS(final String phoneNumber, String message) {
		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		SmsManager sms = SmsManager.getDefault();
		ArrayList<String> parts = sms.divideMessage(message);
		int mMessageSentTotalParts = parts.size();

		Log.i("Message Count", "Message Count: " + mMessageSentTotalParts);

		ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
		ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();

		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
				SENT), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
				new Intent(DELIVERED), 0);

		for (int j = 0; j < mMessageSentTotalParts; j++) {
			sentIntents.add(sentPI);
			deliveryIntents.add(deliveredPI);
		}

		sms.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents,
				deliveryIntents);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initWebViewer() {
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS,
				Window.PROGRESS_VISIBILITY_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		webUrl = (EditText) findViewById(R.id.adress);
		// webUrl.setText(getCurrentUrl());

		// Search button in browser
		btnSeach = (ImageButton) findViewById(R.id.btn_seach);
		btnSeach.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (webUrl.getText().toString().startsWith("http://")) {
					myWebView.loadUrl(webUrl.getText().toString());
				} else {
					myWebView.loadUrl("http://" + webUrl.getText().toString());
				}

			}
		});

		btnHome = (ImageButton) findViewById(R.id.btn_home);
		btnHome.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				myWebView.loadUrl(HOME_PAGE);

			}
		});

		// Back option in browser
		webBackImageView = (ImageButton) findViewById(R.id.back_btn);
		webBackImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (myWebView.canGoBack()) {
					myWebView.goBack();
				}
			}
		});
		// Forward option in browser
		webFrImageView = (ImageButton) findViewById(R.id.fr_btn);
		webFrImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (myWebView.canGoForward()) {
					myWebView.goForward();
				}
			}
		});

		myWebView = (WebView) findViewById(R.id.webView);
		myWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return false;
			}

			@Override
			public void onPageStarted(WebView view, final String url,
					Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				webUrl.post(new Runnable() {
					@Override
					public void run() {
						webUrl.setText(url);
					}
				});
			}
		});

		WebSettings webSettings = myWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);

		webSettings.setLoadWithOverviewMode(true);
		webSettings.setPluginState(WebSettings.PluginState.ON);
		webSettings.setJavaScriptEnabled(true);
		myWebView.setInitialScale(1);
		myWebView.getSettings().setBuiltInZoomControls(true);
		myWebView.getSettings().setUseWideViewPort(true);
		myWebView.getSettings().setDisplayZoomControls(true);
		myWebView.getSettings().setSupportZoom(true);

		if (Build.VERSION.SDK_INT >= 11) {
			myWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
		// myWebView.loadUrl(url);
		// mCurrentUrl = url;
		// set address bar current url
		webUrl = (EditText) findViewById(R.id.adress);

		webViewStarted = true;
	}

	public void updateWebView(Link link) 
	{
		String myUrl = link.getLink().toString();
		if (!myUrl.startsWith("http://")) {
			myUrl = "http://" + myUrl;
		}

		if (myUrl.contains(" ")) {
			myUrl.replace(" ", "");
		}

		if (myUrl.contains("%20")) {
			myUrl.replace("%20", "");
		}

		if (myWebView != null) {
			myWebView.loadUrl(myUrl);
		}
	}

	public String getCurrentLink() 
	{
		return webUrl.getText().toString();
	}

}
