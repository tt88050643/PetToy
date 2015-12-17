package com.example.helloanychat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.bairuitech.anychat.AnyChatBaseEvent;
import com.bairuitech.anychat.AnyChatCoreSDK;
import com.bairuitech.anychat.AnyChatDefine;
import com.bairuitech.anychat.AnyChatTextMsgEvent;
import com.bairuitech.anychat.AnyChatTransDataEvent;
import com.bairuitech.anychat.AnyChatVideoCallEvent;
import com.example.bussinesscenter.BussinessCenter;
import com.example.config.ConfigEntity;
import com.example.config.ConfigService;
import com.example.util.BaseConst;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements AnyChatBaseEvent, AnyChatTransDataEvent, 
		AnyChatTextMsgEvent, AnyChatVideoCallEvent {
	// 视频配置界面标识
	public static final int ACTIVITY_ID_VIDEOCONFIG = 1;

	//private String mStrIP = "tt88050643.xicp.net";
	private String mStrIP = "demo.anychat.cn";
	private String mStrName;
	private int mSPort = 8906;
	private int mSRoomID = 1;

	private final int SHOWLOGINSTATEFLAG = 1; // 显示的按钮是登陆状态的标识
	private final int SHOWWAITINGSTATEFLAG = 2; // 显示的按钮是等待状态的标识
	private final int SHOWLOGOUTSTATEFLAG = 3; // 显示的按钮是登出状态的标识
	private final int LOCALVIDEOAUTOROTATION = 1; // 本地视频自动旋转控制

	private List<RoleInfo> mRoleInfoList = new ArrayList<RoleInfo>();
	private RoleListAdapter mAdapter;
	private int UserselfID;
	private int OtherID;

	public AnyChatCoreSDK anyChatSDK;

	private Button mBtnStart;
	private Button mBtnEnd;
	private Button mBtnCall;
	private EditText mEtName;
	private EditText mEtMessage;
	private Button mBtnMsgSend;
	private EditText mEtAlphaMessage;
	private Button mBtnAlphaMsgSend;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		mBtnStart = (Button) findViewById(R.id.btn_start);
		mBtnEnd = (Button) findViewById(R.id.btn_end);
		mBtnCall = (Button) findViewById(R.id.btn_call);
		mEtName = (EditText) findViewById(R.id.et_name);
		mBtnMsgSend = (Button) findViewById(R.id.btn_sendmessage);
		mEtMessage = (EditText) findViewById(R.id.et_message);
		mBtnAlphaMsgSend = (Button) findViewById(R.id.btn_sendalphamessage);
		mEtAlphaMessage = (EditText) findViewById(R.id.et_alphamessage);
		
		InitSDK();
		InitLayout();
		ApplyVideoConfig();
		// 注册广播
		registerBoradcastReceiver();
		startBackServce();
		
	}
	// 开启后台service
	protected void startBackServce() {
		Intent intent = new Intent();
		intent.setAction(BaseConst.ACTION_BACK_SERVICE);
		this.startService(intent);
	}
	
	private void InitSDK() {
		if (anyChatSDK == null) {
			anyChatSDK = AnyChatCoreSDK.getInstance(this);
			anyChatSDK.SetBaseEvent(this);
			anyChatSDK.SetVideoCallEvent(this);// 设置视频呼叫事件通知接口
			anyChatSDK.InitSDK(android.os.Build.VERSION.SDK_INT, 0);
			anyChatSDK.SetTextMessageEvent(this);
			anyChatSDK.SetTransDataEvent(this);
			AnyChatCoreSDK.SetSDKOptionInt(AnyChatDefine.BRAC_SO_LOCALVIDEO_AUTOROTATION,
					LOCALVIDEOAUTOROTATION);
		}
	}

	private void InitLayout() {
		mBtnStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String mStrName = mEtName.getText().toString();
				anyChatSDK.Connect(mStrIP, mSPort);
				anyChatSDK.Login(mStrName, "");
			}
		});
		mBtnEnd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				anyChatSDK.LeaveRoom(-1);
				anyChatSDK.Logout();
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
		mBtnCall.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				anyChatSDK.VideoCallControl(
						AnyChatDefine.BRAC_VIDEOCALL_EVENT_REQUEST, OtherID,
						AnyChatDefine.BRAC_ERRORCODE_SUCCESS, 0, 0, null);
			}
		});
		mBtnMsgSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String message = mEtMessage.getText().toString();
				anyChatSDK.SendTextMessage(OtherID, 1, message);
			}
		});
		mBtnAlphaMsgSend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String alphaMessage = mEtAlphaMessage.getText().toString().trim();
				byte[] strByteMsg = null;
				if (ValueUtils.isStrEmpty(alphaMessage))
					return;
				try {
					strByteMsg = alphaMessage.getBytes("UTF8");
				} catch (Exception e) {
					e.printStackTrace();
				}
				// 传送接口
				anyChatSDK.TransBuffer(OtherID, strByteMsg, strByteMsg.length);
			}
		});
	}

	// 保存登陆相关数据
	private void saveLoginData() {
		SharedPreferences preferences = getSharedPreferences("LoginInfo", 0);
		Editor preferencesEditor = preferences.edit();
		preferencesEditor.putString("UserIP", mStrIP);
		preferencesEditor.putString("UserName", mStrName);
		preferencesEditor.putInt("UserPort", mSPort);
		preferencesEditor.putInt("UserRoomID", mSRoomID);
		preferencesEditor.commit();
	}

	protected void onDestroy() {
		anyChatSDK.LeaveRoom(-1);
		anyChatSDK.Logout();
		anyChatSDK.Release();
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}

	protected void onResume() {
		BussinessCenter.mContext = MainActivity.this;
		InitSDK();
		super.onResume();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		anyChatSDK.SetBaseEvent(this);

		// 一种简便的方法，当断网的时候，返回到登录界面，不去刷新用户列表，下面广播已经清空了列表
		if (mBtnStart.getVisibility() != View.VISIBLE)
			updateUserList();
	}

	@Override
	// 连接服务器触发
	public void OnAnyChatConnectMessage(boolean bSuccess) {
	}

	// 用户登录触发
	@Override
	public void OnAnyChatLoginMessage(int dwUserId, int dwErrorCode) {
		if (dwErrorCode == 0) {
			saveLoginData();
			anyChatSDK.EnterRoom(mSRoomID, "");
			UserselfID = dwUserId;
			// finish();
			Log.i("cool", "我已登陆，我的ID为：" + UserselfID);
		}
	}

	// 用户进入房间触发
	@Override
	public void OnAnyChatEnterRoomMessage(int dwRoomId, int dwErrorCode) {
		System.out.println("OnAnyChatEnterRoomMessage" + dwRoomId + "err:"
				+ dwErrorCode);
		Log.i("cool", "我已进入房间,RoomID为：" + dwRoomId);
	}

	// 用户进入房间成功后触发一次
	@Override
	public void OnAnyChatOnlineUserMessage(int dwUserNum, int dwRoomId) {
		Log.i("cool", "我进来了,进入房间后触发一次");
		Toast.makeText(this, "登陆成功", Toast.LENGTH_SHORT).show();
		int[] a = anyChatSDK.GetOnlineUser();
		if (a.length == 1) {
			OtherID = a[0];
			Log.i("cool", "目前房间里有:" + anyChatSDK.GetUserName(OtherID) + ";"
					+ "我是：" + anyChatSDK.GetUserName(UserselfID));
		}
		updateUserList();// 更新在线用户列表，保存在List<RoleInfo> mRoleInfoList中
		// if(mRoleInfoList.size() == 1){
		// OtherID = Integer.valueOf(mRoleInfoList.get(0).getUserID());
		// Log.i("cool", "目前房间里有:" + anyChatSDK.GetUserName(OtherID) + ";" +
		// "我是：" + anyChatSDK.GetUserName(UserselfID));
		// }
	}

	private void updateUserList() {
		mRoleInfoList.clear();
		int[] userID = anyChatSDK.GetOnlineUser();
		RoleInfo userselfInfo = new RoleInfo();
		userselfInfo.setName(anyChatSDK.GetUserName(UserselfID)
				+ "(自己) 【点击可设置】");
		userselfInfo.setUserID(String.valueOf(UserselfID));
		userselfInfo.setRoleIconID(getRoleRandomIconID());
		mRoleInfoList.add(userselfInfo);

		for (int index = 0; index < userID.length; ++index) {
			RoleInfo info = new RoleInfo();
			info.setName(anyChatSDK.GetUserName(userID[index]));
			info.setUserID(String.valueOf(userID[index]));
			info.setRoleIconID(getRoleRandomIconID());
			mRoleInfoList.add(info);
		}
		mAdapter = new RoleListAdapter(MainActivity.this, mRoleInfoList);
		// mRoleList.setOnItemClickListener(new OnItemClickListener() {
		// @Override
		// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
		// long arg3) {
		// if (arg2 == 0) {
		// Intent intent = new Intent();
		// intent.setClass(MainActivity.this, VideoConfig.class);
		// startActivityForResult(intent, ACTIVITY_ID_VIDEOCONFIG);
		// return;
		// }
		//
		// onSelectItem(arg2);
		// }
		// });
	}

	private void onSelectItem(String userID) {
		String strUserID = userID;
		Intent intent = new Intent();
		intent.putExtra("UserID", strUserID);
		intent.setClass(this, VideoActivity.class);
		startActivity(intent);
	}

	private int getRoleRandomIconID() {
		int number = new Random().nextInt(5) + 1;
		if (number == 1) {
			return R.drawable.role_1;
		} else if (number == 2) {
			return R.drawable.role_2;
		} else if (number == 3) {
			return R.drawable.role_3;
		} else if (number == 4) {
			return R.drawable.role_4;
		} else if (number == 5) {
			return R.drawable.role_5;
		}

		return R.drawable.role_1;
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

	// 用户离开或者进入房间触发
	@Override
	public void OnAnyChatUserAtRoomMessage(int dwUserId, boolean bEnter) {

		if (bEnter) {// 有用户进入房间
			RoleInfo info = new RoleInfo();
			info.setUserID(String.valueOf(dwUserId));
			info.setName(anyChatSDK.GetUserName(dwUserId));
			info.setRoleIconID(getRoleRandomIconID());
			mRoleInfoList.add(info);
			mAdapter.notifyDataSetChanged();
			Log.i("cool", "有其他人进入房间了，进入房间的用户ID:" + dwUserId + "\n"
					+ "进入房间的用户名:" + info.getName());
			OtherID = dwUserId;
		} else {
			for (int i = 0; i < mRoleInfoList.size(); i++) {
				if (mRoleInfoList.get(i).getUserID().equals("" + dwUserId)) {
					mRoleInfoList.remove(i);
					mAdapter.notifyDataSetChanged();
				}
			}
		}
	}

	// 断网触发
	@Override
	public void OnAnyChatLinkCloseMessage(int dwErrorCode) {
		anyChatSDK.LeaveRoom(-1);
		anyChatSDK.Logout();
	}

	// 广播
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("VideoActivity")) {
				Toast.makeText(MainActivity.this, "网络已断开！", Toast.LENGTH_SHORT).show();
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
		case AnyChatDefine.BRAC_VIDEOCALL_EVENT_REQUEST: // 请求呼叫
			Log.i("cool",
					"收到ID为" + dwUserId + "  " + "Name为"
							+ anyChatSDK.GetUserName(dwUserId) + "的人的呼叫");
			onSelectItem(String.valueOf(dwUserId));
			break;
		case AnyChatDefine.BRAC_VIDEOCALL_EVENT_REPLY:// 呼叫回复
			Log.i("cool", "呼叫ID为" + dwUserId + "的人");
			onSelectItem(String.valueOf(dwUserId));
			break;
		case AnyChatDefine.BRAC_VIDEOCALL_EVENT_START:// 视频呼叫会话开始事件
			Log.i("cool", "视频呼叫会话开始");

			break;
		case AnyChatDefine.BRAC_VIDEOCALL_EVENT_FINISH:// 挂断（结束）呼叫会话
			Log.i("cool", "会话挂断");

			break;
		}
	}

	// 文字消息通知,dwFromUserid表示消息发送者的用户ID号，dwToUserid表示目标用户ID号，
	// 可能为-1，表示对大家说，bSecret表示是否为悄悄话
	@Override
	public void OnAnyChatTextMessage(int dwFromUserid, int dwToUserid,
			boolean bSecret, String message) {
		Log.i("cool", "收到消息，from:" + anyChatSDK.GetUserName(dwFromUserid)
				+ "到:" + anyChatSDK.GetUserName(dwToUserid) + "消息内容是:" + message);
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
	
}
