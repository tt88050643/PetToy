package com.example.bgservice;

import java.util.List;

import com.example.bussinesscenter.BussinessCenter;
import com.example.helloanychat.R;
import com.example.util.BaseConst;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
/**
 * 在后台运行的service，
 * @author zhaimeng
 *
 */
public class BackService extends Service {
	private ActivityManager activityManager;
	List<RunningAppProcessInfo> appProcesses;
	private String packageName;
	private boolean bStop = false;
	private boolean bFirstShow = true;
	public final static int BACK_NOTIFICATION_APP = 0x457893;
	public final static int BACK_NOTIFICATIONID_BASE = 0x888;
	public RequestSdkBroadCast mBroadCastRecevier;

	class RequestSdkBroadCast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BaseConst.ACTION_BACK_CANCELSESSION)) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					int userId = bundle.getInt("USERID");
					cancelNotification(BACK_NOTIFICATIONID_BASE + userId);
				}
			}
			if (intent.getAction().equals(BaseConst.ACTION_BACK_EQUESTSESSION)) {
				Bundle bundle = intent.getExtras();
				if (bundle != null) {
					int userId = bundle.getInt("USERID");
					showNotification(bundle.getString("USERNAME"),
							BACK_NOTIFICATIONID_BASE + userId);
				}
			}
			if (intent.getAction().equals(
					BaseConst.ACTION_BACK_CANCELNOTIFYTION)) {
				cancelNotification();
			}
			if (intent.getAction().equals(BaseConst.ACTION_BACK_KILLSELF)) {
				cancelNotification();
				stopSelf();
				Log.i("cool", "service kill self");
			}

		}

	}

	@Override
	public void onCreate() {
		Log.i("cool", "BackService running");
		registerBroad();
		super.onCreate();
	}

	private void registerBroad() {
		mBroadCastRecevier = new RequestSdkBroadCast();
		IntentFilter intentFilter = new IntentFilter(
				BaseConst.ACTION_BACK_EQUESTSESSION);
		this.registerReceiver(mBroadCastRecevier, intentFilter);
		intentFilter = new IntentFilter(BaseConst.ACTION_BACK_CANCELSESSION);
		this.registerReceiver(mBroadCastRecevier, intentFilter);
		intentFilter = new IntentFilter(BaseConst.ACTION_BACK_CANCELNOTIFYTION);
		this.registerReceiver(mBroadCastRecevier, intentFilter);
		intentFilter = new IntentFilter(BaseConst.ACTION_BACK_KILLSELF);
		this.registerReceiver(mBroadCastRecevier, intentFilter);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		activityManager = (ActivityManager) this
				.getSystemService(Context.ACTIVITY_SERVICE);
		packageName = this.getPackageName();
		// 一个线程一直在循环控制通知的显示与不显示
		new Thread() {
			public void run() {
				try {
					while (!bStop) {// 如果service没停止运行
						if (isAppOnForeground()) {// 如果当前App在前台运行
							if (!bFirstShow) { // 不是第一次
								cancelNotification();
								bFirstShow = true;
								BussinessCenter.bBack = false;
							}
						} else {
							if (bFirstShow) {
								showNotification(
										BackService.this
												.getString(R.string.BACKING_RUNING),
										BACK_NOTIFICATION_APP);
								bFirstShow = false;
								BussinessCenter.bBack = true;
							}
						}

						Thread.sleep(1000);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 检查程序是否在前台运行
	 * 
	 * @return 是否在前台运行
	 */
	public boolean isAppOnForeground() {
		// Returns a list of application processes that are running on the
		// device
		/*
		 * ComponentName currentCompoentName = activityManager
		 * .getRunningTasks(2).get(0).topActivity; Log.i(TAG,
		 * currentCompoentName.getClassName());
		 */appProcesses = activityManager.getRunningAppProcesses();
		if (appProcesses == null)
			return false;
		for (RunningAppProcessInfo appProcess : appProcesses) {
			// The name of the process that this object is associated with.
			if (appProcess.processName.equals(packageName)
					&& appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void onDestroy() {
		this.unregisterReceiver(mBroadCastRecevier);
		bStop = true;
		super.onDestroy();
	}

	/***
	 * 显示指定通知
	 * 
	 * @param strTitle
	 *            通知内容
	 * @param notification_id
	 *            通知id
	 */
	public void showNotification(String strText, int notification_id) {

		// 得到NotificationManager
		Log.i("cool", "showNotification");
		NotificationManager notificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.ic_launcher,
				strText, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults = Notification.DEFAULT_LIGHTS;
		notification.ledARGB = Color.BLUE;
		notification.ledOnMS = 100;
		notification.ledOffMS = 100;
		Intent notificationIntent = new Intent(BussinessCenter.mContext,
				BussinessCenter.mContext.getClass());
		// Intent notificationIntent = new Intent(MainActivity.mContext,
		// MainActivity.mContext.getClass());
		notificationIntent.putExtra("action", 2);
		notificationIntent.setAction(Intent.ACTION_MAIN);
		notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		PendingIntent contentIntent = PendingIntent.getActivity(
				BussinessCenter.mContext, 0, notificationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		// PendingIntent contentIntent = PendingIntent.getActivity(
		// MainActivity.mContext, 0, notificationIntent,
		// PendingIntent.FLAG_UPDATE_CURRENT);
		notification
				.setLatestEventInfo(this,
						this.getString(R.string.BACKING_RUNING), strText,
						contentIntent);
		notificationManager.notify(notification_id, notification);
	}

	public void cancelNotification(int notificationId) {

		try {
			NotificationManager notificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(notificationId);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void cancelNotification() {
		try {
			NotificationManager notificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
			notificationManager.cancelAll();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

}
