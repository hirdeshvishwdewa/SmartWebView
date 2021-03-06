package jgarciabt.smartwebview;

import android.app.Activity;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;

import com.squareup.otto.Subscribe;
import butterknife.ButterKnife;
import butterknife.InjectView;
import jgarciabt.smartwebview.broadcast.NetworkBroadcastReceiver;
import jgarciabt.smartwebview.broadcast.events.InternetDownEvent;
import jgarciabt.smartwebview.broadcast.events.InternetUpEvent;
import jgarciabt.smartwebview.bus.BusManager;
import jgarciabt.smartwebview.utils.Constants;
import jgarciabt.smartwebview.utils.CustomWebViewClient;
import jgarciabt.smartwebview.utils.SnackbarUtils;


/**
 * Created by JGarcia on 28/3/15.
 */
public class LauncherActivity extends Activity {

    @InjectView(R.id.webViewFrame) public WebView webViewFrame;
    private BusManager busManager;
    private NetworkBroadcastReceiver networkBroadcastReceiver;
    private IntentFilter intentFilter;

    private String lastUrlAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        ButterKnife.inject(this);
        busManager = BusManager.getInstance();
        busManager.register(this);

        networkBroadcastReceiver = new NetworkBroadcastReceiver(this);
        intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");

        setupWebView();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(networkBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(networkBroadcastReceiver, intentFilter);
    }

    @Override
    public void onBackPressed() {

        if(!isOnRootURL(webViewFrame.getUrl()))
        {
            String goBackUrl = previousLevelUrl(webViewFrame.getUrl());
            webViewFrame.loadUrl(goBackUrl);
        }
        else
        {
            finish();
        }
    }

    private void setupWebView()
    {
        lastUrlAvailable = Constants.BASE_URL;
        webViewFrame.loadUrl(Constants.BASE_URL);
        webViewFrame.setWebViewClient(new CustomWebViewClient(this));
        webViewFrame.getSettings().setJavaScriptEnabled(true);
    }

    private boolean isOnRootURL(String currentUrl)
    {
        return (currentUrl.matches(Constants.BASE_URL) || currentUrl.matches(Constants.OFFLINE_FILE));
    }

    private String previousLevelUrl(String currentUrl)
    {
        String lastPath = Uri.parse(currentUrl).getLastPathSegment();
        return currentUrl.substring(0, currentUrl.length() - lastPath.length() - 1);
    }

    @Subscribe
    public void internetConnectionGone(InternetDownEvent event)
    {
        SnackbarUtils.showNoInternetSnackbar(this);
        lastUrlAvailable = webViewFrame.getUrl();
    }

    @Subscribe
    public void internetConnectionCame(InternetUpEvent event)
    {
        SnackbarUtils.dismissSnackbar();

        if(lastUrlAvailable.matches(Constants.OFFLINE_FILE))
        {
            webViewFrame.loadUrl(Constants.BASE_URL);
            return;
        }
        webViewFrame.loadUrl(lastUrlAvailable);
    }

}
