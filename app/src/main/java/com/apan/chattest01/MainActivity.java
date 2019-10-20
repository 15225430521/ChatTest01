package com.apan.chattest01;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.apan.chattest01.databinding.ActivityMainBinding;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.EMMessageListener;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.NetUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, EMMessageListener {

    ActivityMainBinding binding;
    private String userName="";
    private String password="";
    private Handler handler;

    // 消息监听器
    private EMMessageListener mMessageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mMessageListener = this;

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                binding.content.append(msg.obj.toString()+"\n");
            }
        };

        binding.register.setOnClickListener(this);
        binding.login.setOnClickListener(this);
        binding.connect.setOnClickListener(this);
        binding.send.setOnClickListener(this);
        binding.exit.setOnClickListener(this);

        binding.userName.setText("apan2");
        binding.password.setText("apan2");

        requestAllPermission();



    }


    /**
     * 请求获取全部权限，调用权限管理类，然后放进去需要申请的权限，通过接口回调的方法获得权限获取结果
     */
    private void requestAllPermission() {
        /**
         * 请求所有必要的权限----原理就是获取清单文件中申请的权限
         */
        PermissionsManager.getInstance().requestAllManifestPermissionsIfNecessary(this, new PermissionsResultAction() {
            @Override
            public void onGranted() {
//              Toast.makeText(MainActivity.this, "All permissions have been granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDenied(String permission) {
                //Toast.makeText(MainActivity.this, "Permission " + permission + " has been denied", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //因为权限管理类无法监听系统，所以需要重写onRequestPermissionResult方法，更新权限管理类，并回调结果。这个是必须要有的。
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }



    @Override
    public void onClick(View view) {

        if (view == binding.register){
            binding.content.append("注册中...\n");

            userName = binding.userName.getText().toString();
            password = binding.password.getText().toString();

            registerUser(userName, password);
        }

        if (view == binding.login){
            userName = binding.userName.getText().toString();
            password = binding.password.getText().toString();

            Log.e("userName", userName);
            Log.e("password", password);

            binding.content.append("\n登录中...\n");
            LoginUser(userName, password);


        }

        if (view == binding.connect){
            binding.content.append("正在加入聊天室...");
            liveChat("96541947789313");
            intoChat("96526612365313");
        }


        if (view == binding.send){
            String text = binding.chatContent.getText().toString();
            liaotian(text);
            receive();
        }

        if (view == binding.exit){
            exit();
        }


    }

    public void liveChat(String toChatUsername){
        EMClient.getInstance().chatroomManager().leaveChatRoom(toChatUsername);
    }

    public void registerUser(final String userName, final String password){



        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().createAccount(userName, password);//同步方法
                    Message message = handler.obtainMessage();
                    message.obj = "注册成功";
                    handler.sendMessage(message);
                } catch (HyphenateException e) {
                    Log.e("注册", e.getMessage());
                    Message message = handler.obtainMessage();
                    message.obj = "注册失败";
                    handler.sendMessage(message);

                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void LoginUser(String userName, String password){
        final Message message = handler.obtainMessage();


        EMClient.getInstance().login(userName,password,new EMCallBack() {//回调
            @Override
            public void onSuccess() {
                EMClient.getInstance().groupManager().loadAllGroups();
                EMClient.getInstance().chatManager().loadAllConversations();

                message.obj = "登录聊天服务器成功！\n";
                handler.sendMessage(message);
                Log.d("main", "登录聊天服务器成功！");
            }

            @Override
            public void onProgress(int progress, String status) {

            }

            @Override
            public void onError(int code, String msg) {
                message.obj = "登录聊天服务器失败！\n失败信息：\n"+msg + "\n";
                handler.sendMessage(message);
                Log.d("code", code+"");
                Log.d("msg", msg);

            }
        });

    }

    public void exit(){

        final Message message = handler.obtainMessage();
        EMClient.getInstance().logout(true, new EMCallBack() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                message.obj = "退出成功";
                handler.sendMessage(message);

            }

            @Override
            public void onProgress(int progress, String status) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onError(int code, String msg) {
                // TODO Auto-generated method stub
                message.obj = "退出失败："+msg;
                handler.sendMessage(message);
            }
        });
    }

    public void chatListener(){

        //注册一个监听连接状态的listener
        EMClient.getInstance().addConnectionListener(new MyConnectionListener());
    }


    //实现ConnectionListener接口
    private class MyConnectionListener implements EMConnectionListener {
        @Override
        public void onConnected() {
        }
        @Override
        public void onDisconnected(final int error) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if(error == EMError.USER_REMOVED){
                        // 显示帐号已经被移除
                    }else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                        // 显示帐号在其他设备登录
                    } else
                        if (NetUtils.hasNetwork(MainActivity.this)) {
                            //连接不到聊天服务器
                        } else{
                            //当前网络不可用，请检查网络设置

                        }
                }
            });
        }
    }


    private String createChat(String subject, String description){

        /**
         * \~chinese
         * 创建聊天室，聊天室最大人数上限10000。只有特定用户有权限创建聊天室。
         * @param subject           名称
         * @param description       描述
         * @param welcomeMessage    邀请成员加入聊天室的消息
         * @param maxUserCount      允许加入聊天室的最大成员数
         * @param members           邀请加入聊天室的成员列表
         * @return EMChatRoom 聊天室
         * @throws HyphenateException
         */
        try {
            EMChatRoom room = EMClient.getInstance().chatroomManager().createChatRoom(subject, description, null,
                    1000, null);
            return room.getId();
        } catch (HyphenateException e) {
            e.printStackTrace();
        }

        return null;


    }

    public void intoChat(final String roomId){

        //roomId为聊天室ID
        EMClient.getInstance().chatroomManager().joinChatRoom(roomId, new EMValueCallBack<EMChatRoom>() {

            final Message message = handler.obtainMessage();
            @Override
            public void onSuccess(EMChatRoom value) {
                //加入聊天室成功
                try {
                    EMChatRoom room = EMClient.getInstance().chatroomManager().fetchChatRoomFromServer(roomId);

                    room.getName();//聊天室名称
                    room.getId();//聊天室id
                    room.getDescription();//聊天室描述
                    room.getOwner();//聊天室创建者
                    message.obj = "加入聊天室成功\n聊天室信息：" +
                            "(聊天室名称)"+ room.getName() +
                            "   (聊天室id)"+room.getId()+
                            "   (聊天室描述)"+room.getDescription()+
                            "   (聊天室创建者)"+room.getOwner();

                    handler.sendMessage(message);


                } catch (HyphenateException e) {
                    e.printStackTrace();
                    message.obj = "加入聊天室失败\n失败信息："+e.getMessage();
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onError(final int error, String errorMsg) {
                //加入聊天室失败
                message.obj = "加入聊天室失败\n失败信息："+errorMsg;
                handler.sendMessage(message);
            }
        });

    }

    public void liaotian(String content){
        //创建一条文本消息，content为消息文字内容，toChatUsername为对方用户或者群聊的id，后文皆是如此
        EMMessage message = EMMessage.createTxtSendMessage(content, "96526612365313");

        message.setChatType(EMMessage.ChatType.GroupChat);
        //发送消息
        EMClient.getInstance().chatManager().sendMessage(message);
    }

    public void receive(){

        EMConversation conversation = EMClient.getInstance().chatManager().getConversation("96526612365313");
        //获取此会话的所有消息
        List<EMMessage> messages = conversation.getAllMessages();

        EMMessage messge = conversation.getLastMessage();
        EMTextMessageBody body = (EMTextMessageBody) messge.getBody();


        binding.content.append("\n" + body.getMessage());
        //SDK初始化加载的聊天记录为20条，到顶时需要去DB里获取更多
        //获取startMsgId之前的pagesize条消息，此方法获取的messages SDK会自动存入到此会话中，APP中无需再次把获取到的messages添加到会话中
//        List<EMMessage> messages = conversation.loadMoreMsgFromDB(startMsgId, pagesize);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onMessageReceived(List<EMMessage> list) {

        Log.e("onMessageReceived", list.size() + "");

        for (EMMessage message : list) {
            EMTextMessageBody body = (EMTextMessageBody) message.getBody();

            Log.e("message", body.getMessage());
            Log.e("message", message.getFrom());

            Message msg = handler.obtainMessage();
            msg.what = 0;
            msg.obj = body.getMessage();
            handler.sendMessage(msg);

        }

    }

    @Override
    public void onCmdMessageReceived(List<EMMessage> list) {

    }

    @Override
    public void onMessageRead(List<EMMessage> list) {

    }

    @Override
    public void onMessageDelivered(List<EMMessage> list) {

    }

    @Override
    public void onMessageRecalled(List<EMMessage> list) {

    }

    @Override
    public void onMessageChanged(EMMessage emMessage, Object o) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        EMClient.getInstance().chatManager().addMessageListener(mMessageListener);
    }

    @Override protected void onStop() {
        super.onStop();
        // 移除消息监听
        EMClient.getInstance().chatManager().removeMessageListener(mMessageListener);
    }


}
