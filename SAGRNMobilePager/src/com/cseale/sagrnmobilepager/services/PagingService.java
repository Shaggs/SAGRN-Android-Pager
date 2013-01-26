package com.cseale.sagrnmobilepager.services;

import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cseale.sagrnmobilepager.activities.FeedServiceActivity;
import com.cseale.sagrnmobilepager.activities.HTMLParser;
import com.cseale.sagrnmobilepager.activities.R;
import com.cseale.sagrnmobilepager.interfaces.RefreshCallback;
import com.cseale.sagrnmobilepager.managers.RefreshManager;
import com.cseale.sagrnmobilepager.objects.PagesList;

public class PagingService extends Service implements RefreshCallback{
	String url = "";
	boolean isUpdating  = false;
	PagesList data = null;

	private boolean notifications;
	static State mobile;

	SharedPreferences sharedPref;
	ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
	int mValue = 0; // Holds last value set by a client.
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_SET_PAGES = 3;
	public static final int MSG_UPDATE_SETTINGS = 4;

	final int UNIQUE_ID = 123458;

	final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.
	private RefreshManager rf = null;
	String filter;
	String messageStr = "";

	private NotificationManager nm;
	private OnSharedPreferenceChangeListener listener;
	private static boolean isRunning = false;


	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("MyService", "Service Started.");
		isRunning = true;
	}
	@Override
	public void onStart(Intent intent, int startId) {
		init();
		startScanning();
		Log.i("MyService", "onStart Setup Complete");

	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("MyService", "onStart Setup Complete");
		init();
		startScanning();
		return START_STICKY; // run until explicitly stopped.
	}

	public static boolean checkIfServiceIsRunning(){
		return isRunning;
	}
	public void init(){
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

		listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
				startScanning();
			}
		};

		sharedPref.registerOnSharedPreferenceChangeListener(listener);
		nm= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		//mobile
		mobile = conMan.getNetworkInfo(0).getState();

	}

	private void startScanning() {
		// TODO Auto-generated method stub
		if(rf != null){
			rf.cancelTimer();
			rf = null;
		}
		data = null;
		isUpdating = false;
		filter = sharedPref.getString(getString(R.string.keyfilteredit), "");
		notifications = sharedPref.getBoolean(getString(R.string.keynotifycheck), false);
		url=sharedPref.getString(getString(R.string.keyselectfeed), "http://paging1.sacfs.org/live/");
		rf = new RefreshManager();
		rf.setTimerCallback(this);
		rf.startTimer();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		rf.cancelTimer();
		nm.cancel(UNIQUE_ID); // Cancel the persistent notification.
		sharedPref.unregisterOnSharedPreferenceChangeListener(listener);
		Log.i("MyService", "Service Stopped.");
		isRunning  = false;
	}    

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}
	class IncomingHandler extends Handler { // Handler of incoming messages from clients.
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
			case MSG_UPDATE_SETTINGS:
				init();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	private void sendMessageToUI(int message) {
		for (int i=mClients.size()-1; i>=0; i--) {
			try {
				Message msg;

				switch(message){
				case MSG_SET_PAGES:
					//Send data as a String
					Bundle b = new Bundle();
					b.putSerializable("pages", data);
					b.putString("message", messageStr);
					msg = Message.obtain(null, MSG_SET_PAGES);
					msg.setData(b);
					mClients.get(i).send(msg);
				}
			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
				mClients.remove(i);
			}
			finally{
				isUpdating = false;
			}
		}
	}


	@Override
	public void onTimerUpdate() {
		Log.i("MyService", "onTimerUpdate");
		if (isUpdating == false){
			isUpdating = true;
			if(haveNetwork(this)){
				try{
					PagesList newPages = HTMLParser.getPagesList(url);
					newPages.filterPages(filter);
					Log.i("MyService", "PagesList size: " + newPages.size());
					if(data != null 
							&& newPages.size() > 0 
							&& !data.pagesAreSame(newPages) 
							&& notifications)
					{
						Log.i("MyService", "New Page Recieved, Displaying Notifcation");
						showNotification(newPages.get(0).getName(), newPages.get(0).getDescription());
					}
					if (newPages.size() == 0){
						messageStr = "No Pages to Display";
					}
					else{
						String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
						messageStr = "Last Refresh Time : " + currentDateTimeString; //update message with time
					}
					data = new PagesList(newPages);
				}
				catch(SocketTimeoutException e){
					messageStr = "Network Timeout, retrying..."; //timeout exception message
				}
			}
			else{
				messageStr = "No Network Connection"; //no network message
				data = new PagesList();
				Log.i("MyService", "No Pages");

			}
			sendMessageToUI(MSG_SET_PAGES);
		}
	}

	public static boolean haveNetwork(Context ctx) {

		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

		if (info == null || !info.isConnected()) {
			return false;
		}
		if (info.isRoaming()) {
			// here is the roaming option you can change it if you want to
			// disable internet while roaming, just return false
			return false;
		}
		/*		if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
			return false;
		} */
		return true;
	}

	private void showNotification(String string, String string2) {
		Log.i("MyService", "showNotification called...");
		final Intent notificationIntent = new Intent(this, FeedServiceActivity.class);

		notificationIntent.setAction(Intent.ACTION_MAIN);
		notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		final Notification notification = new Notification(R.drawable.ic_launcher, "SAGRN Mobile Pager", System.currentTimeMillis());
		notification.defaults |= Notification.DEFAULT_ALL;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(this,string ,string2, contentIntent);

		// Notifying the user about the new update.
		if (nm != null) {
			nm.notify(UNIQUE_ID, notification);
		}
	}

}



