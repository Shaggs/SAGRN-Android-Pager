package com.cseale.sagrnmobilepager.managers;

import java.util.Timer;
import java.util.TimerTask;

import com.cseale.sagrnmobilepager.interfaces.RefreshCallback;

public class RefreshManager {

	private static RefreshCallback timerCallback;
	private Timer timer;
	
	public RefreshManager(){
		timer = new Timer();
	}
	
	public synchronized void startTimer() {
		TimerTask updateTimerValuesTask = new TimerTask() {
			@Override
			public void run() {
				RefreshManager.timerCallback.onTimerUpdate();
			}
		};
		timer.schedule(updateTimerValuesTask, 1000*2, 1000*60);
	}

	public static RefreshCallback getTimerCallback() {
		return timerCallback;
	}

	public void setTimerCallback(final RefreshCallback timerCallback) {
		RefreshManager.timerCallback = timerCallback;
	}

	public synchronized void cancelTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
}
