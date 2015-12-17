package com.bairuitech.anychat;

//AnyChat基本事件接口
public interface AnyChatBaseEvent {
	// 连接服务器触发
    public void OnAnyChatConnectMessage(boolean bSuccess);
	// 用户登录触发
    public void OnAnyChatLoginMessage(int dwUserId, int dwErrorCode);
	// 用户进入房间触发
    public void OnAnyChatEnterRoomMessage(int dwRoomId, int dwErrorCode);
	// 用户进入房间成功后触发一次
    public void OnAnyChatOnlineUserMessage(int dwUserNum, int dwRoomId);
	// 用户离开或者进入房间触发
    public void OnAnyChatUserAtRoomMessage(int dwUserId, boolean bEnter);
	//网络断开触发
    public void OnAnyChatLinkCloseMessage(int dwErrorCode);	
	
}
