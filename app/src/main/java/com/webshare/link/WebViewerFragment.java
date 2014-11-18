
package com.webshare.link;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
//import android.widget.TextView;

@SuppressLint("SetJavaScriptEnabled")
public class WebViewerFragment extends Fragment 
{
    final static String ARG_POSITION = "position";
    
    private Link link = null;
    
    final static String ARG_URL = "link";

	private int mCurrentPosition = -1;
	private String mCurrentUrl = "http://www.google.com";
	private String HOME_PAGE = "http://www.google.com";
	private ImageButton webFrImageView;
	private ImageButton btnSeach;
	private ImageButton fullscreenImageView;
	private ImageButton btnHome;
	private EditText webUrl;
	private WebView myWebView;
	private ImageButton webBackImageView;
	
	public static Boolean webViewStarted;
	
	private static final String HTTP = "http://";
	private boolean isHideBars = false;
	View view;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {

        // If activity recreated (such as from screen rotate), restore
        // the previous article selection set by onSaveInstanceState().
        // This is primarily necessary when in the two-pane layout.
    	getActivity().getWindow().setFeatureInt(Window.FEATURE_PROGRESS,
				Window.PROGRESS_VISIBILITY_ON);
		getActivity().getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	
    	if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(ARG_POSITION);
        }

        
        view = inflater.inflate(R.layout.web_viewer, container, false);
               return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // During startup, check if there are arguments passed to the fragment.
        // onStart is a good place to do this because the layout has already been
        // applied to the fragment at this point so we can safely call the method
        // below that sets the article text.
        Bundle args = getArguments();
        if (args != null) {
            // Set article based on argument passed in
            //updateWebView(args.getInt(ARG_POSITION));
        } else if (mCurrentPosition != -1) {
            // Set article based on saved instance state defined during onCreateView
            //updateWebView(mCurrentPosition);
        }
        myWebView = (WebView) view.findViewById(R.id.webView);
        if(link != null)
        {
        	myWebView.loadUrl(link.getLink());
        }
        // Inflate the layout for this fragment
        
     // set home page
     		webUrl = (EditText) view.findViewById(R.id.adress);
     		// webUrl.setText(getCurrentUrl());

     		// Search button in browser
     		btnSeach = (ImageButton) view.findViewById(R.id.btn_seach);
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

     		btnHome = (ImageButton) view.findViewById(R.id.btn_home);
     		btnHome.setOnClickListener(new OnClickListener() {

     			@Override
     			public void onClick(View v) {

     				myWebView.loadUrl(HOME_PAGE);

     			}
     		});

     		// Back option in browser
     		webBackImageView = (ImageButton) view.findViewById(R.id.back_btn);
     		webBackImageView.setOnClickListener(new OnClickListener() {
     			@Override
     			public void onClick(View v) {
     				if (myWebView.canGoBack()) {
     					myWebView.goBack();
     				}
     			}
     		});
     		// Forward option in browser
     		webFrImageView = (ImageButton) view.findViewById(R.id.fr_btn);
     		webFrImageView.setOnClickListener(new OnClickListener() {
     			@Override
     			public void onClick(View v) {
     				if (myWebView.canGoForward()) {
     					myWebView.goForward();
     				}
     			}
     		});

 
        myWebView = (WebView) getActivity().findViewById(R.id.webView);
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
//		myWebView.loadUrl(url);
//		mCurrentUrl = url;
		// set address bar current url
		webUrl = (EditText) getActivity().findViewById(R.id.adress);
		
		webViewStarted = true;
    }

    public void updateWebView(Link link) {
    	
    	this.link = link;
    	
    	String myUrl = link.getLink().toString();
    	
    	if(!myUrl.startsWith("http://")){
    		
    		myUrl = "http://"+myUrl;
    	}
    	
    	if(myUrl.contains(" ")){
    		
    		myUrl.replace(" ", "");
    	}
    	
        if(myUrl.contains("%20")){
    		
    		myUrl.replace("%20", "");
    	}
    	
    	if(myWebView != null)
        {
        	//myWebView.loadData(link.getLink(), "text/html", "UTF-8");
    		myWebView.loadUrl(myUrl);
        }
    	
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the current article selection in case we need to recreate the fragment
        outState.putInt(ARG_POSITION, mCurrentPosition);
    }

	public String getCurrentLink() {
		return webUrl.getText().toString();
	}
}