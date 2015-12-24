package com.example.helloanychat;

import com.bairuitech.anychat.AnyChatBaseEvent;
import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;
import com.bairuitech.anychat.AnyChatTextMsgEvent;
import com.bairuitech.anychat.AnyChatTransDataEvent;
import com.bairuitech.anychat.AnyChatVideoCallEvent;
import com.example.bussinesscenter.BussinessCenter;
import com.example.bussinesscenter.OnlineUserItem;
import com.example.config.ConfigEntity;
import com.example.config.ConfigService;
import com.example.util.BaseConst;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity implements AnyChatBaseEvent,
		AnyChatTransDataEvent, AnyChatTextMsgEvent, AnyChatVideoCallEvent {
	// 视频配置界面标识
	public static final int ACTIVITY_ID_VIDEOCONFIG = 1;

	private final int SHOWLOGINSTATEFLAG = 1; // 显示的按钮是登陆状态的标识
	private final int SHOWWAITINGSTATEFLAG = 2; // 显示的按钮是等待状态的标识
	private final int SHOWLOGOUTSTATEFLAG = 3; // 显示的按钮是登出状态的标识
	private final int LOCALVIDEOAUTOROTATION = 1; // 本地视频自动旋转控制
	// private String mStrIP = "demo.anychat.cn";
	private String mStrIP = "tt88050643.xicp.net";
	private String mStrUserName;// 我的用户名
	private int mIntPort = 8906;
	private int mIntRoomID = 1;

	public AnyChatCoreSDK anyChatSDK;

	private RoleListAdapter mAdapter;
	private ListView mLvOnlineUsers;
	private Button mBtnLogin;
	private Button mBtnLogout;
	private EditText mEtUserName;// 我登陆的用户名
	private EditText mEtRoomID;// 我进入的房间号
	private EditText mEtServerName;
	private ProgressBar pb_login;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		InitSDK();
		InitLayout();

		ApplyVideoConfig();
		registerBoradcastReceiver();
		startBackServce();// 开启后台service，用于当程序进入后台后在通知栏控制通知
		BussinessCenter.getBussinessCenter();
	}
	
	
	// 开启后台service
	protected void startBackServce() {
		Intent intent = new Intent();
		intent.setAction(BaseConst.ACTION_BACK_SERVICE);
		this.startService(intent);
	}

	private void InitSDK() {
		anyChatSDK = AnyChatCoreSDK.getInstance(this);
		anyChatSDK.SetBaseEvent(this);
		anyChatSDK.SetVideoCallEvent(this);// 设置视频呼叫事件通知接口
		anyChatSDK.InitSDK(android.os.Build.VERSION.SDK_INT, 0);
		anyChatSDK.SetTextMessageEvent(this);
		anyChatSDK.SetTransDataEvent(this);
		AnyChatCoreSDK.SetSDKOptionInt(
				AnyChatDefine.BRAC_SO_LOCALVIDEO_AUTOROTATION,
				LOCALVIDEOAUTOROTATION);
		BussinessCenter.mContext = MainActivity.this;
	}

	private void InitLayout() {
		mBtnLogin = (Button) findViewById(R.id.btn_login);
		mBtnLogout = (Button) findViewById(R.id.btn_logout);
		mEtUserName = (EditText) findViewById(R.id.et_username);
		mEtServerName = (EditText) findViewById(R.id.et_servername);
		mEtRoomID = (EditText) findViewById(R.id.et_roomid);
		mLvOnlineUsers = (ListView) findViewById(R.id.lv_onlineuser);
		pb_login = (ProgressBar) findViewById(R.id.pb_login);
		ReadLoginData();
		mBtnLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mStrUserName = mEtUserName.getText().toString();
				mStrIP = mEtServerName.getText().toString();
				pb_login.setVisibility(View.VISIBLE);
				anyChatSDK.Connect(mStrIP, mIntPort);
				anyChatSDK.Login(mStrUserName, "");
			}
		});
		mBtnLogout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				anyChatSDK.LeaveRoom(-1);
				anyChatSDK.Logout();
				mBtnLogout.setBackgroundResource(R.drawable.btn_green_pressed);
				mBtnLogin.setBackgroundResource(R.drawable.btn_green_normal);
				updateUserList();
			}
		});
		/***
		 * 发送呼叫事件
		 * 
		 * @param dwEventType
		 *            视频呼叫事件类型
		 * @param dwUserId
		 *            目标userid
		 * @param dwErrorCode
		 *            出错代码
		 * @param dwFlags
		 *            功能标志
		 * @param dwParam
		 *            自定义参数，传给对方
		 * @param szUserStr
		 *            自定义参数，传给对方
		 */
		// mBtnCall.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// Log.i("cool", "我发起了视频请求，我的Name：" +
		// anyChatSDK.GetUserName(BussinessCenter.selfUserId) +
		// "对方的Name：" + anyChatSDK.GetUserName(OtherID));
		// BussinessCenter.VideoCallControl(
		// AnyChatDefine.BRAC_VIDEOCALL_EVENT_REQUEST, OtherID,
		// AnyChatDefine.BRAC_ERRORCODE_SUCCESS, 0, 0, "");
		// }
		// });
		// mBtnMsgSend.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// String message = mEtMessage.getText().toString();
		// anyChatSDK.SendTextMessage(OtherID, 1, message);
		// }
		// });
		// mBtnAlphaMsgSend.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// String alphaMessage = mEtAlphaMessage.getText().toString()
		// .trim();
		// byte[] strByteMsg = null;
		// if (ValueUtils.isStrEmpty(alphaMessage))
		// return;
		// try {
		// strByteMsg = alphaMessage.getBytes("UTF8");
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// // 传送接口
		// anyChatSDK.TransBuffer(OtherID, strByteMsg, strByteMsg.length);
		// }
		// });
	}

	/**
	 * 保存登陆信息
	 */
	private void saveLoginData() {
		SharedPreferences preferences = getSharedPreferences("LoginInfo", 0);
		Editor preferencesEditor = preferences.edit();
		preferencesEditor.putString("UserIP", mStrIP);
		preferencesEditor.putString("UserName", mStrUserName);
		preferencesEditor.putInt("UserPort", mIntPort);
		preferencesEditor.putInt("UserRoomID", mIntRoomID);
		preferencesEditor.commit();
	}

	/**
	 * 读取登陆信息
	 */
	private void ReadLoginData() {
		SharedPreferences preferences = getSharedPreferences("LoginInfo",
				MODE_PRIVATE);
		mStrIP = preferences.getString("UserIP", "tt88050643.xicp.net");
		mStrUserName = preferences.getString("UserName", "xiyangyang");
		mIntPort = preferences.getInt("UserPort", 8906);
		mIntRoomID = preferences.getInt("UserRoomID", 1);
		mEtServerName.setText(mStrIP);
		mEtUserName.setText(mStrUserName);
		mEtRoomID.setText(String.valueOf(mIntRoomID));
	}

	protected void onDestroy() {
		Log.i("cool", "onDestroy");
		anyChatSDK.LeaveRoom(-1);
		anyChatSDK.Logout();
		anyChatSDK.Release();
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			showExitDialog(this);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	protected void onResume() {
		super.onResume();
		InitSDK();
		Log.i("cool", "onResume");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.i("cool", "onRestart");
		// 一种简便的方法，当断网的时候，返回到登录界面，不去刷新用户列表，下面广播已经清空了列表
		if (mBtnLogin.getVisibility() != View.VISIBLE) {
			createUserList();
		}
	}

	@Override
	// 连接服务器触发
	public void OnAnyChatConnectMessage(boolean bSuccess) {
	}

	// 用户登录触发
	@Override
	public void OnAnyChatLoginMessage(int dwUserId, int dwErrorCode) {
		if (dwErrorCode == 0) {
			BussinessCenter.selfUserId = dwUserId;
			BussinessCenter.selfUserIconId = BussinessCenter.getBussinessCenter().getRoleRandomIconID();
			BussinessCenter.selfUserName = anyChatSDK.GetUserName(dwUserId);
			// saveLoginData();
			anyChatSDK.EnterRoom(mIntRoomID, "");
		}
	}

	// 用户进入房间触发
	@Override
	public void OnAnyChatEnterRoomMessage(int dwRoomId, int dwErrorCode) {
		System.out.println("OnAnyChatEnterRoomMessage" + dwRoomId + "err:"
				+ dwErrorCode);
		Log.i("cool", "我已进入房间,RoomID为：" + dwRoomId);
		if (dwErrorCode == 0) {
			saveLoginData();
		}
	}

	// 用户进入房间成功后触发一次
	@Override
	public void OnAnyChatOnlineUserMessage(int dwUserNum, int dwRoomId) {
		Log.i("cool", "我进来了,进入房间后触发一次");
		pb_login.setVisibility(View.GONE);
		mBtnLogin.setBackgroundResource(R.drawable.btn_green_pressed);
		mBtnLogout.setBackgroundResource(R.drawable.btn_green_normal);
		Toast.makeText(this, "登陆成功", Toast.LENGTH_SHORT).show();
		createUserList();
		for (OnlineUserItem eachone : BussinessCenter.mOnlineUserItems) {
			Log.i("cool", "当前在线用户:" + eachone.getUserName());
		}
		mEtServerName.setVisibility(View.GONE);
		mEtRoomID.setVisibility(View.GONE);
		mEtUserName.setVisibility(View.GONE);
	}

	// 用户离开或者进入房间触发
	@Override
	public void OnAnyChatUserAtRoomMessage(int dwUserId, boolean bEnter) {
		updateUserList();
		if(bEnter){
			Log.i("cool",
					"有其他人进入房间了，进入房间的用户ID:" + dwUserId + "\n" + "进入房间的用户名:"
							+ BussinessCenter.getBussinessCenter().getUserItemByUserId(dwUserId).getUserName());
		}
		
	}

	// 断网触发
	@Override
	public void OnAnyChatLinkCloseMessage(int dwErrorCode) {
		mBtnLogin.setBackgroundResource(R.drawable.btn_green_normal);
		mBtnLogout.setBackgroundResource(R.drawable.btn_green_normal);
		anyChatSDK.LeaveRoom(-1);
		anyChatSDK.Logout();
		updateUserList();
	}

	private void createUserList() {
		BussinessCenter.getBussinessCenter().getOnlineUserDatas();
		for(int i = 0; i < BussinessCenter.mOnlineUserItems.size(); i++){
			OnlineUserItem onlineUserItem = BussinessCenter.mOnlineUserItems.get(i);
			if(onlineUserItem.getUserId() == BussinessCenter.selfUserId){
				onlineUserItem.setUserName(BussinessCenter.selfUserName + "(自己) 【点击可设置】");
			}
		}
		mAdapter = new RoleListAdapter(MainActivity.this, BussinessCenter.mOnlineUserItems);
		mLvOnlineUsers.setAdapter(mAdapter);
		mLvOnlineUsers.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {// 点击自己
					Intent intent = new Intent();
					intent.setClass(MainActivity.this, VideoConfig.class);
					startActivityForResult(intent, ACTIVITY_ID_VIDEOCONFIG);
					return;
				}
				onSelectItem(position);// 点击别人
			}
		});
	}
	/**
	 * 更新UserListView
	 */
	private void updateUserList(){
		BussinessCenter.getBussinessCenter().getOnlineUserDatas();
		for(int i = 0; i < BussinessCenter.mOnlineUserItems.size(); i++){
			OnlineUserItem onlineUserItem = BussinessCenter.mOnlineUserItems.get(i);
			if(onlineUserItem.getUserId() == BussinessCenter.selfUserId){
				onlineUserItem.setUserName(BussinessCenter.selfUserName + "(自己) 【点击可设置】");
			}
		}
		mAdapter.notifyDataSetChanged();
	}
	

	/**
	 * listview的item被选中时执行
	 * 
	 * @param position
	 */
	private void onSelectItem(int position) {
		int otherID = BussinessCenter.mOnlineUserItems.get(position).getUserId();
		Log.i("cool",
				"我发起了视频请求，我的Name："
						+ anyChatSDK.GetUserName(BussinessCenter.selfUserId)
						+ "对方的Name："
						+ anyChatSDK.GetUserName(otherID));
		BussinessCenter.VideoCallControl(
				AnyChatDefine.BRAC_VIDEOCALL_EVENT_REQUEST,
				Integer.valueOf(otherID),
				AnyChatDefine.BRAC_ERRORCODE_SUCCESS, 0, 0, "");
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == ACTIVITY_ID_VIDEOCONFIG) {
			ApplyVideoConfig();
		}
	}

	// 根据配置文件配置视频参数
	private void ApplyVideoConfig() {
		ConfigEntity configEntity = ConfigService.LoadConfig(this);
		if (configEntity.mConfigMode == 1) // 自定义视频参数配置
		{
			// 设置本地视频编码的码率（如果码率为0，则表示使用质量优先模式）
			AnyChatCoreSDK.SetSDKOptionInt(
					AnyChatDefine.BRAC_SO_LOCALVIDEO_BITRATECTRL,
					configEntity.mVideoBitrate);
			// if (configEntity.mVideoBitrate == 0) {
			// 设置本地视频编码的质量
			AnyChatCoreSDK.SetSDKOptionInt(
					AnyChatDefine.BRAC_SO_LOCALVIDEO_QUALITYCTRL,
					configEntity.mVideoQuality);
			// }
			// 设置本地视频编码的帧率
			AnyChatCoreSDK.SetSDKOptionInt(
					AnyChatDefine.BRAC_SO_LOCALVIDEO_FPSCTRL,
					configEntity.mVideoFps);
			// 设置本地视频编码的关键帧间隔
			AnyChatCoreSDK.SetSDKOptionInt(
					AnyChatDefine.BRAC_SO_LOCALVIDEO_GOPCTRL,
					configEntity.mVideoFps * 4);
			// 设置本地视频采集分辨率
			AnyChatCoreSDK.SetSDKOptionInt(
					AnyChatDefine.BRAC_SO_LOCALVIDEO_WIDTHCTRL,
					configEntity.mResolutionWidth);
			AnyChatCoreSDK.SetSDKOptionInt(
					AnyChatDefine.BRAC_SO_LOCALVIDEO_HEIGHTCTRL,
					configEntity.mResolutionHeight);
			// 设置视频编码预设参数（值越大，编码质量越高，占用CPU资源也会越高）
			AnyChatCoreSDK.SetSDKOptionInt(
					AnyChatDefine.BRAC_SO_LOCALVIDEO_PRESETCTRL,
					configEntity.mVideoPreset);
		}
		// 让视频参数生效
		AnyChatCoreSDK.SetSDKOptionInt(
				AnyChatDefine.BRAC_SO_LOCALVIDEO_APPLYPARAM,
				configEntity.mConfigMode);
		// P2P设置
		AnyChatCoreSDK.SetSDKOptionInt(
				AnyChatDefine.BRAC_SO_NETWORK_P2PPOLITIC,
				configEntity.mEnableP2P);
		// 本地视频Overlay模式设置
		AnyChatCoreSDK.SetSDKOptionInt(
				AnyChatDefine.BRAC_SO_LOCALVIDEO_OVERLAY,
				configEntity.mVideoOverlay);
		// 回音消除设置
		AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_AUDIO_ECHOCTRL,
				configEntity.mEnableAEC);
		// 平台硬件编码设置
		AnyChatCoreSDK.SetSDKOptionInt(
				AnyChatDefine.BRAC_SO_CORESDK_USEHWCODEC,
				configEntity.mUseHWCodec);
		// 视频旋转模式设置
		AnyChatCoreSDK.SetSDKOptionInt(
				AnyChatDefine.BRAC_SO_LOCALVIDEO_ROTATECTRL,
				configEntity.mVideoRotateMode);
		// 本地视频采集偏色修正设置
		AnyChatCoreSDK.SetSDKOptionInt(
				AnyChatDefine.BRAC_SO_LOCALVIDEO_FIXCOLORDEVIA,
				configEntity.mFixColorDeviation);
		// 视频GPU渲染设置
		AnyChatCoreSDK.SetSDKOptionInt(
				AnyChatDefine.BRAC_SO_VIDEOSHOW_GPUDIRECTRENDER,
				configEntity.mVideoShowGPURender);
		// 本地视频自动旋转设置
		AnyChatCoreSDK.SetSDKOptionInt(
				AnyChatDefine.BRAC_SO_LOCALVIDEO_AUTOROTATION,
				configEntity.mVideoAutoRotation);
	}

	// 广播
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("VideoActivity")) {
				Toast.makeText(MainActivity.this, "网络已断开！", Toast.LENGTH_SHORT)
						.show();
				anyChatSDK.LeaveRoom(-1);
				anyChatSDK.Logout();
			}
		}
	};

	public void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction("VideoActivity");
		// 注册广播
		registerReceiver(mBroadcastReceiver, myIntentFilter);
	}

	// 呼叫事件触发
	@Override
	public void OnAnyChatVideoCallEvent(int dwEventType, int dwUserId,
			int dwErrorCode, int dwFlags, int dwParam, String userStr) {

		switch (dwEventType) {
		case AnyChatDefine.BRAC_VIDEOCALL_EVENT_REQUEST: // 呼叫请求(接收方收到请求时触发)
			Log.i("cool",
					"我接收到请求，我的Name为"
							+ anyChatSDK
									.GetUserName(BussinessCenter.selfUserId)
							+ "对方的Name为" + anyChatSDK.GetUserName(dwUserId));
			// 发送通知栏通知，播放呼叫铃声
			BussinessCenter.getBussinessCenter().onVideoCallRequest(dwUserId,
					dwFlags, dwParam, userStr);
			BussinessCenter.VideoCallControl(
					AnyChatDefine.BRAC_VIDEOCALL_EVENT_REPLY, dwUserId,
					AnyChatDefine.BRAC_ERRORCODE_SUCCESS, 0, 0, null);
			// onSelectItem(String.valueOf(dwUserId));
			break;
		case AnyChatDefine.BRAC_VIDEOCALL_EVENT_REPLY:// 呼叫请求回复(发送方接收到接收方回复时触发)
			if (dwErrorCode == AnyChatDefine.BRAC_ERRORCODE_SUCCESS) {
				Log.i("cool",
						"我收到回复，我的Name为"
								+ anyChatSDK
										.GetUserName(BussinessCenter.selfUserId)
								+ "对方的Name为" + anyChatSDK.GetUserName(dwUserId));
				// 根据错误码判断控制铃声播放和通知栏
				BussinessCenter.getBussinessCenter().onVideoCallReply(dwUserId,
						dwErrorCode, dwFlags, dwParam, userStr);
			}
			break;
		case AnyChatDefine.BRAC_VIDEOCALL_EVENT_START:// 视频呼叫会话开始事件
			Log.i("cool", "视频呼叫会话开始,下面将启动videoactivity");
			Log.i("cool", "dwUserId:" + dwUserId + ";" + "dwFlags:" + dwFlags
					+ ";" + "dwParam:" + dwParam + ";" + "userStr:" + userStr);
			Log.i("cool",
					"视频通话将开始，我的Name为"
							+ anyChatSDK
									.GetUserName(BussinessCenter.selfUserId)
							+ "对方的Name为" + anyChatSDK.GetUserName(dwUserId));
			BussinessCenter.getBussinessCenter().onVideoCallStart(dwUserId,
					dwFlags, dwParam, userStr);
			break;
		case AnyChatDefine.BRAC_VIDEOCALL_EVENT_FINISH:// 挂断（结束）呼叫会话
			Log.i("cool", "会话挂断事件！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！");
			BussinessCenter.getBussinessCenter().onVideoCallEnd(dwUserId,
					dwFlags, dwParam, userStr);
			break;
		}
	}

	/**
	 * 收到信息时触发 文字消息通知,dwFromUserid表示消息发送者的用户ID号，dwToUserid表示目标用户ID号，
	 * 可能为-1，表示对大家说，bSecret表示是否为悄悄话
	 */
	@Override
	public void OnAnyChatTextMessage(int dwFromUserid, int dwToUserid,
			boolean bSecret, String message) {
		Log.i("cool", "收到消息，from:" + anyChatSDK.GetUserName(dwFromUserid)
				+ "到:" + anyChatSDK.GetUserName(dwToUserid) + "消息内容是:"
				+ message);
	}

	@Override
	public void OnAnyChatTransFile(int dwUserid, String FileName,
			String TempFilePath, int dwFileLength, int wParam, int lParam,
			int dwTaskId) {
		Log.i("cool", "文件传输回调函数定义");
	}

	@Override
	public void OnAnyChatTransBuffer(int dwUserid, byte[] lpBuf, int dwLen) {
		Log.i("cool", "透明通道数据回调函数定义");
		Log.i("cool", "from:" + dwUserid + "内容:" + lpBuf);
	}

	@Override
	public void OnAnyChatTransBufferEx(int dwUserid, byte[] lpBuf, int dwLen,
			int wparam, int lparam, int taskid) {
		Log.i("cool", "扩展透明通道数据回调函数定义");
	}

	@Override
	public void OnAnyChatSDKFilterData(byte[] lpBuf, int dwLen) {
		Log.i("cool", "SDK Filter 通信数据回调函数定义");
	}
	
	public void showExitDialog(final Context context) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage("Are you sure to exit ?").setCancelable(false).setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								sendBroadcast(new Intent(BaseConst.ACTION_BACK_KILLSELF));
								finish();
							}
						}).setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();
	}
}
