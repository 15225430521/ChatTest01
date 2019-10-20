package com.apan.chattest01;

import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.os.Message;
import android.util.Log;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMMessageListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.exceptions.HyphenateException;

import java.util.Iterator;
import java.util.List;

//环信聊天室工具类
public class ChatUtils {

    /**
     * 注册账户
     * @param userName
     * @param password
     * @param chatCallBack
     */
    public static void registerUser(final String userName, final String password, final ChatCallBack chatCallBack){

        Log.e("userName", userName);
        Log.e("password", password);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().createAccount(userName, password);//同步方法
                    chatCallBack.success("注册成功");
                } catch (HyphenateException e) {
                    Log.e("注册", e.getMessage());
                    chatCallBack.success("注册失败："+e.getMessage());

                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 登录账户
     * @param userName
     * @param password
     * @param chatCallBack
     */
    public static void LoginUser(String userName, String password, final ChatCallBack chatCallBack){
        EMClient.getInstance().login(userName,password,new EMCallBack() {//回调
            @Override
            public void onSuccess() {
                EMClient.getInstance().groupManager().loadAllGroups();
                EMClient.getInstance().chatManager().loadAllConversations();

                chatCallBack.success("登录聊天服务器成功！");

            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String msg) {

                chatCallBack.success("登录聊天服务器失败:"+msg);
            }
        });

    }

    /**
     * 退出登录
     */
    public static void exitChat(final ChatCallBack chatCallBack){

        EMClient.getInstance().logout(true, new EMCallBack() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                chatCallBack.success("退出成功");
            }

            @Override
            public void onProgress(int progress, String status) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onError(int code, String message) {
                // TODO Auto-generated method stub

                chatCallBack.success("退出失败："+message);

            }
        });

    }

    /**
     * 加入聊天室
     *
     * 聊天室名称:room.getName()
     * 聊天室id:room.getId()
     * 聊天室描述:room.getDescription()
     * 聊天室创建者:room.getOwner()
     * @param roomId
     * @param chatCallBack
     */
    public static void joinChat(final String roomId, final ChatCallBack chatCallBack){

        //roomId为聊天室ID
        EMClient.getInstance().chatroomManager().joinChatRoom(roomId, new EMValueCallBack<EMChatRoom>() {

            @Override
            public void onSuccess(EMChatRoom value) {
                //加入聊天室成功
                try {
                    EMChatRoom room = EMClient.getInstance().chatroomManager().fetchChatRoomFromServer(roomId);

                    room.getName();//聊天室名称
                    room.getId();//聊天室id
                    room.getDescription();//聊天室描述
                    room.getOwner();//聊天室创建者
                    chatCallBack.success(room);


                } catch (HyphenateException e) {
                    e.printStackTrace();
                    chatCallBack.fail("加入聊天室失败:"+e.getMessage());
                }
            }

            @Override
            public void onError(final int error, String errorMsg) {
                //加入聊天室失败
                chatCallBack.fail("加入聊天室失败:"+errorMsg);
            }
        });

    }

    /**
     * 离开聊天室
     * @param roomId
     */
    public static void leaveChat(String roomId){
        EMClient.getInstance().chatroomManager().leaveChatRoom(roomId);
    }

    /**
     * 发送消息
     * @param content 消息文本
     * @param roomId  房间号
     */
    public static void sendChat(String content, String roomId){
        EMMessage message = EMMessage.createTxtSendMessage(content, roomId);

        message.setChatType(EMMessage.ChatType.GroupChat);
        //发送消息
        EMClient.getInstance().chatManager().sendMessage(message);
    }


    /**
     * 添加监听
     */
    public static void addListener(EMMessageListener mMessageListener){
        EMClient.getInstance().chatManager().addMessageListener(mMessageListener);
    }

    /**
     * 移除监听
     * @param mMessageListener
     */
    public static void removeListener(EMMessageListener mMessageListener){
        EMClient.getInstance().chatManager().removeMessageListener(mMessageListener);
    }


    /**
     * SDK初始化的一些配置
     * 关于 EMOptions 可以参考官方的 API 文档
     * http://www.easemob.com/apidoc/android/chat3.0/classcom_1_1hyphenate_1_1chat_1_1_e_m_options.html
     */
    public static EMOptions initOptions() {

        EMOptions options = new EMOptions();
        // 设置Appkey，如果配置文件已经配置，这里可以不用设置
        // options.setAppKey("lzan13#hxsdkdemo");
        // 设置自动登录
//        options.setAutoLogin(true);
        // 设置是否需要发送已读回执
        options.setRequireAck(true);
        // 设置是否需要发送回执，
        options.setRequireDeliveryAck(true);
        // 设置是否根据服务器时间排序，默认是true
        options.setSortMessageByServerTime(false);
        // 收到好友申请是否自动同意，如果是自动同意就不会收到好友请求的回调，因为sdk会自动处理，默认为true
        options.setAcceptInvitationAlways(false);
        // 设置是否自动接收加群邀请，如果设置了当收到群邀请会自动同意加入
        options.setAutoAcceptGroupInvitation(false);
        // 设置（主动或被动）退出群组时，是否删除群聊聊天记录
        options.setDeleteMessagesAsExitGroup(false);
        // 设置是否允许聊天室的Owner 离开并删除聊天室的会话
        options.allowChatroomOwnerLeave(true);
        // 设置google GCM推送id，国内可以不用设置
        // options.setGCMNumber(MLConstants.ML_GCM_NUMBER);
        // 设置集成小米推送的appid和appkey
        // options.setMipushConfig(MLConstants.ML_MI_APP_ID, MLConstants.ML_MI_APP_KEY);

        return options;
    }



    /**
     * 回调
     */
    public static interface ChatCallBack{
        public void success(Object obj);
        public void fail(Object obj);
    }

    /**
     * 消息监听回调
     */
    public static interface ChatMessageListener{
        public void message(Object obj);
    }

}
