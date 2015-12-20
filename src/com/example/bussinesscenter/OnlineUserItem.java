package com.example.bussinesscenter;

import java.io.Serializable;

import android.util.Log;

public class OnlineUserItem implements Serializable,Cloneable{

	private String mUserName;
	private String mStrIp;
	private int mUserId;
	//private int mUserIdenty;
	//private int mGroupId;
	private static final long serialVersionUID=8502706820090766507L;
	public static final int USERSTATUS_OFFLINE=0;
	public static final int USERSTATUS_ONLINE=1;
	public static final int USERINFO_NAME=1;
	public static final int USERINFO_IP=2;
	public OnlineUserItem(int mUserId,String strName,String strIp)
	{
		this.mUserName=strName;
		this.mUserId=mUserId;
		this.mStrIp=strIp;
	}
	public OnlineUserItem()
	{
		mUserName="";
		mStrIp="";
	}
	public String getUserName() {
		return mUserName;
	}
	public void setUserName(String mUserName) {
		this.mUserName = mUserName;
	}
	public String getStrIp() {
		return mStrIp;
	}
	public void setStrIp(String mStrIp) {
		this.mStrIp = mStrIp;
	}
	public int getUserId() {
		return mUserId;
	}
	public void setUserId(int mUserId) {
		this.mUserId = mUserId;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public OnlineUserItem clone()
	{
		OnlineUserItem item=null;
		try
		{
			item=(OnlineUserItem) super.clone();
		}
		catch(Exception e)
		{
			Log.i("useritem-clone", e.toString());
		}
		return item;
	}
}
