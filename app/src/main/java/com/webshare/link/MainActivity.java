/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webshare.link;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements
		LinksFragment.OnHeadlineSelectedListener,
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = MainActivity.class.getSimpleName();
	// We don't instanciate the handler here as context is not yet ready before
	// create
	private LinksSQLiteHandler handler = null;
	private AlertDialog shareDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.links_layout);

		// Check whether the activity is using the layout version with
		// the fragment_container FrameLayout. If so, we must add the first
		// fragment
		if (findViewById(R.id.fragment_container) != null) {

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				return;
			}

			// Create an instance of ExampleFragment
			LinksFragment firstFragment = new LinksFragment();

			// In case this activity was started with special instructions from
			// an Intent,
			// pass the Intent's extras to the fragment as arguments
			firstFragment.setArguments(getIntent().getExtras());

			// Add the fragment to the 'fragment_container' FrameLayout
			getFragmentManager()
					.beginTransaction()
					.add(R.id.fragment_container, firstFragment,
							"LinksFragment").commit();
		}

		handler = new LinksSQLiteHandler(this);

		getLoaderManager().initLoader(0, null, this);
	}

	public void onLinkSelected(Link link) {
		// The user selected the headline of an article from the
		// HeadlinesFragment
		String myLink = link.getLink().toString();

		Toast.makeText(getApplication(), myLink, Toast.LENGTH_LONG).show();
		// Capture the article fragment from the activity layout
		WebViewerFragment webViewerFrag = (WebViewerFragment) getFragmentManager()
				.findFragmentById(R.id.web_viewer_fragment);

		if (webViewerFrag != null) {
			// If article frag is available, we're in two-pane layout...

			// Call a method in the ArticleFragment to update its content

		} else {
			// If the frag is not available, we're in the one-pane layout and
			// must swap frags...

			// Create fragment and give it an argument for the selected article
			webViewerFrag = new WebViewerFragment();
			Bundle args = new Bundle();
			// args.putInt(WebViewerFragment.ARG_POSITION, 0);
			webViewerFrag.setArguments(args);
			FragmentTransaction transaction = getFragmentManager()
					.beginTransaction();

			// Replace whatever is in the fragment_container view with this
			// fragment,
			// and add the transaction to the back stack so the user can
			// navigate back
			transaction.replace(R.id.fragment_container, webViewerFrag);
			transaction.addToBackStack(null);

			// Commit the transaction
			transaction.commit();
		}

		webViewerFrag.updateWebView(link);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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

			builder.setPositiveButton("Add", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					EditText nameEditText = (EditText) view
							.findViewById(R.id.nameEditText);
					EditText linkEditText = (EditText) view
							.findViewById(R.id.linkEditText);

					String name = nameEditText.getText().toString();
					String link = linkEditText.getText().toString();

					// Roman: this checks whether link string starts with "http"
					// and if not
					// we will add the "http" to the link
					if (!link.startsWith("http://")) {

						link = "http://" + link;

					}

					// Roman: this checks whether the link string contains
					// spaces
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

					Toast.makeText(MainActivity.this, uri.toString(),
							Toast.LENGTH_LONG).show();

					// Capture the article fragment from the activity layout
					LinksFragment linksFrag = (LinksFragment) getFragmentManager()
							.findFragmentByTag("LinksFragment");

					if (linksFrag != null) {
						linksFrag.onLinksUpdated();
					}

				}
			});
			builder.setNegativeButton("Cancel", new OnClickListener() {
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

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// TODO: Please add the sort order
		return new CursorLoader(this, Contacts.CONTENT_URI, null, null, null,
				null);
	}

	final Map<Long, Boolean> selectedContactsMap = new HashMap<Long, Boolean>();

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
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
					Toast.makeText(MainActivity.this, "No link is found",
							Toast.LENGTH_LONG).show();
				}

				shareDialog.dismiss();

			}
		});
		contactsListView.setAdapter(adapter);

		builder.setView(selectContactView);
		builder.setPositiveButton("Share!", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		builder.setNegativeButton("Cancel", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(MainActivity.this, ":-(", Toast.LENGTH_SHORT)
						.show();
			}
		});

		shareDialog = builder.create();

		/*
				*/

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
			/*
			 * int type = phones.getInt(phones.getColumnIndex(Phone.TYPE));
			 * switch (type) { case Phone.TYPE_HOME: Log.i("TYPE_HOME", "" +
			 * number); break; case Phone.TYPE_MOBILE: Log.i("TYPE_MOBILE", "" +
			 * number); break; case Phone.TYPE_WORK: Log.i("TYPE_WORK", "" +
			 * number); break; case Phone.TYPE_FAX_WORK: Log.i("TYPE_FAX_WORK",
			 * "" + number); break; case Phone.TYPE_FAX_HOME:
			 * Log.i("TYPE_FAX_HOME", "" + number); break;
			 * 
			 * case Phone.TYPE_OTHER: Log.i("TYPE_OTHER", "" + number); break; }
			 */

		}

		phones.close();

		return phonesNumbers;
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub

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

	/*
	 * private void registerBroadCastReceivers(){
	 * 
	 * registerReceiver(new BroadcastReceiver() {
	 * 
	 * @Override public void onReceive(Context arg0, Intent arg1) { switch
	 * (getResultCode()) { case Activity.RESULT_OK:
	 * 
	 * mMessageSentParts++; if ( mMessageSentParts == mMessageSentTotalParts ) {
	 * mMessageSentCount++; sendNextMessage(); }
	 * 
	 * Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
	 * break; case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
	 * Toast.makeText(getBaseContext(), "Generic failure",
	 * Toast.LENGTH_SHORT).show(); break; case
	 * SmsManager.RESULT_ERROR_NO_SERVICE: Toast.makeText(getBaseContext(),
	 * "No service", Toast.LENGTH_SHORT).show(); break; case
	 * SmsManager.RESULT_ERROR_NULL_PDU: Toast.makeText(getBaseContext(),
	 * "Null PDU", Toast.LENGTH_SHORT).show(); break; case
	 * SmsManager.RESULT_ERROR_RADIO_OFF: Toast.makeText(getBaseContext(),
	 * "Radio off", Toast.LENGTH_SHORT).show(); break; } } }, new
	 * IntentFilter(SENT));
	 * 
	 * registerReceiver(new BroadcastReceiver() {
	 * 
	 * @Override public void onReceive(Context arg0, Intent arg1) { switch
	 * (getResultCode()) {
	 * 
	 * case Activity.RESULT_OK: Toast.makeText(getBaseContext(),
	 * "SMS delivered", Toast.LENGTH_SHORT).show(); break; case
	 * Activity.RESULT_CANCELED: Toast.makeText(getBaseContext(),
	 * "SMS not delivered", Toast.LENGTH_SHORT).show(); break; } } }, new
	 * IntentFilter(DELIVERED));
	 * 
	 * }
	 */

}