package com.cseale.sagrnmobilepager.activities;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cseale.sagrnmobilepager.objects.PagesList;
import com.cseale.sagrnmobilepager.services.PagingService;


public class FeedServiceActivity extends Activity {
	PagesList pages;
	Messenger mService = null;
	boolean mIsBound;
	ListView itemlist;
	String filter = "Uni";
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	private ViewSwitcher switcher;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed);
		switcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
		if(PagingService.checkIfServiceIsRunning()){
			switcher.showNext();
			doBindService();
		}

	}
	@Override
	public void onStart() {
		super.onStart();
		Log.i("FeedServiceActivity", "On Start .....");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			doUnbindService();
		} catch (Throwable t) {
			Log.e("MainActivity", "Failed to unbind from the service", t);
		}
	}

	class IncomingHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case PagingService.MSG_SET_PAGES:
				pagesUpdate(msg);
				break;
			}
		}
	}


	public void pagesUpdate(Message msg){
		Bundle b = msg.getData();
		pages = (PagesList) b.getSerializable("pages");
		String messageStr = b.getString("message");
		displayFeed(messageStr);
		Log.i("FeedServiceActivity", "Displaying Pages...");
	}

	public void displayFeed(String msgStr) {
		itemlist = (ListView) findViewById(R.id.listView1);

		SimpleAdapter adapter = new SimpleAdapter(this, pages.getListViewMap(),
				android.R.layout.simple_list_item_2,
				new String[] {"date", "details"},
				new int[] {android.R.id.text1,
			android.R.id.text2});
		itemlist.setAdapter(adapter);	

		Log.i("FeedServiceActivity", "Screen Update");
		((TextView)findViewById(R.id.textView1)).setText(msgStr); 
	}


	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			try {
				Message msg = Message.obtain(null, PagingService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even do anything with it
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been unexpectedly disconnected - process crashed.
			mService = null;
		}
	};

	void doBindService() {
		Intent i = new Intent(this, PagingService.class);
		startService(i);
		bindService(i, mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}
	void doUnbindService() {
		if (mIsBound) {
			// If we have received the service, and hence registered with it, then now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null, PagingService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service has crashed.
				}
			}
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId())
		{
		case R.id.feedstopmenu:
			doUnbindService();
			stopService(new Intent(FeedServiceActivity.this, PagingService.class));
			finish();
			break;
		case R.id.menu_settings:
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
			break;
		case R.id.menu_about:
			displayAboutDialog();
			break;
		}

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	private void displayAboutDialog()
	{
		final LayoutInflater inflator = LayoutInflater.from(this);
		final View settingsview = inflator.inflate(R.layout.about, null);
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(getString(R.string.app_name));
		builder.setView(settingsview);

		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		builder.create().show();
	}

	public void startScanning(View view) {
		// TODO Auto-generated method stub
		//Next View

		switcher.setInAnimation(this, R.anim.in_animation1);
		switcher.setOutAnimation(this, R.anim.out_animation1);
		switcher.showNext();
		doBindService();

	}


}





//private void sendMessageToService(int intvaluetosend) {
//if (mIsBound) {
//  if (mService != null) {
//      try {
//          Message msg = Message.obtain(null, PagingService.MSG_SET_INT_VALUE, intvaluetosend, 0);
//          msg.replyTo = mMessenger;
//          mService.send(msg);
//      } catch (RemoteException e) {
//      }
//  }
//}
//}