package com.example.helloanymote;

import com.example.google.tv.anymotelibrary.client.AnymoteClientService;
import com.example.google.tv.anymotelibrary.client.AnymoteSender;
import com.example.google.tv.anymotelibrary.client.AnymoteClientService.ClientListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity implements ClientListener {

	private AnymoteClientService mAnymoteClientService;
    private ProgressBar progressBar;

    /**
     * Handles messages on main UI thread.
     */
    private Handler handler;

    /**
     * The proxy used to send events to the server using Anymote Protocol
     */
    private AnymoteSender anymoteSender;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setup the Activity UI
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.a_progressbar);
        progressBar.setVisibility(View.VISIBLE);

        // UI message handler
        handler = new Handler();
        
        // Bind to the AnymoteClientService
        Intent intent = new Intent(MainActivity.this, AnymoteClientService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    /*private void sendKeyEvent(final int keyEvent) {
        // create new Thread to avoid network operations on UI Thread
        if (anymoteSender == null) {
            Toast.makeText(MainActivity.this, R.string.waiting_for_connection,
                    Toast.LENGTH_LONG).show();
            return;
        }
        anymoteSender.sendKeyPress(keyEvent);
    }*/

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        /*
         * ServiceConnection listener methods.
         */
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAnymoteClientService = ((AnymoteClientService.AnymoteClientServiceBinder) service)
                    .getService();
            mAnymoteClientService.attachClientListener(MainActivity.this);
        }

        public void onServiceDisconnected(ComponentName name) {
            mAnymoteClientService.detachClientListener(MainActivity.this);
            mAnymoteClientService = null;
        }
    };

    @Override
    public void onConnected(final AnymoteSender anymoteSender) {
        Toast.makeText(
                MainActivity.this, R.string.pairing_succeeded_toast, Toast.LENGTH_LONG)
                .show();

        this.anymoteSender = anymoteSender;

        // Send Intent to launch the background listener on Google TV through Anymote.
        /*final Intent launchListenerIntent = new Intent("android.intent.action.MAIN");*/
        // TODO launch background listener
        /*launchListenerIntent.setComponent(new ComponentName(
              "com.google.android.tv.blackjack",
              "com.google.android.tv.blackjack.BlackJackTableActivity"));
        anymoteSender.sendIntent(launchListenerIntent);*/

        // Hide the progressBar once connection to Google TV is established.
        handler.post(new Runnable() {
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

	@Override
	public void onDisconnected() {
        Toast.makeText(
                MainActivity.this, R.string.pairing_failed_toast, Toast.LENGTH_LONG)
                .show();
        this.anymoteSender = null;
	}

	@Override
	public void onConnectionFailed() {
        Toast.makeText(
                MainActivity.this, R.string.pairing_failed_toast, Toast.LENGTH_LONG)
                .show();
        this.anymoteSender = null;
	}

	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

	@Override
    protected void onDestroy() {
        if (mAnymoteClientService != null) {
            mAnymoteClientService.detachClientListener(this);
        }
        unbindService(mConnection);
        super.onDestroy();
    }
}
