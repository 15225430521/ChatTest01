package com.apan.chattest01;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

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
import com.hyphenate.chat.EMMessageBody;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.NetUtils;

import java.util.List;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener, EMMessageListener{

    ActivityMainBinding binding;
    private String userName="";
    private String password="";
    private Handler handler;
    private static final String CHAT1 = "96541947789313";

    // 消息监听器
    private EMMessageListener mMessageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                binding.content.append(msg.obj.toString()+"\n");
            }
        };

        mMessageListener = this;

        binding.register.setOnClickListener(this);
        binding.login.setOnClickListener(this);
        binding.connect.setOnClickListener(this);
        binding.send.setOnClickListener(this);
        binding.exit.setOnClickListener(this);
        binding.leaveRoom.setOnClickListener(this);

        binding.userName.setText("apan4");
        binding.password.setText("apan4");

    }

    @Override
    public void onClick(final View view) {

        if (view == binding.leaveRoom){
            ChatUtils.leaveChat(CHAT1);
            binding.content.append("离开房间\n");
        }

        if (view == binding.register){

            binding.content.append("注册中...\n");

            userName = binding.userName.getText().toString();
            password = binding.password.getText().toString();

            final Message message = handler.obtainMessage();

            ChatUtils.registerUser(userName, password, new ChatUtils.ChatCallBack() {
                @Override
                public void success(Object obj) {
                    message.obj = obj;
                    handler.sendMessage(message);
                }

                @Override
                public void fail(Object obj) {
                    message.obj = obj;
                    handler.sendMessage(message);
                }
            });
        }

        if (view == binding.login){
            userName = binding.userName.getText().toString();
            password = binding.password.getText().toString();

            binding.content.append("\n登录中...\n");

            final Message message = handler.obtainMessage();
            ChatUtils.LoginUser(userName, password, new ChatUtils.ChatCallBack() {
                @Override
                public void success(Object obj) {
                    message.obj = obj;
                    handler.sendMessage(message);
                }

                @Override
                public void fail(Object obj) {
                    message.obj = obj;
                    handler.sendMessage(message);
                }
            });
        }

        if (view == binding.exit){

            final Message message = handler.obtainMessage();


            ChatUtils.exitChat(new ChatUtils.ChatCallBack() {
                @Override
                public void success(Object obj) {
                    Message message = handler.obtainMessage();
                    message.obj = obj;
                    handler.sendMessage(message);
                }

                @Override
                public void fail(Object obj) {
                    message.obj = obj;
                    handler.sendMessage(message);
                }
            });
        }

        if (view == binding.connect){
            final Message message = handler.obtainMessage();

            binding.content.append("正在加入聊天室...\n");
            ChatUtils.joinChat(CHAT1, new ChatUtils.ChatCallBack() {
                @Override
                public void success(Object obj) {
                    EMChatRoom room = (EMChatRoom) obj;
                    message.obj = "加入("+((EMChatRoom) obj).getName()+")聊天室成功";
                    handler.sendMessage(message);
                }

                @Override
                public void fail(Object obj) {
                    message.obj = obj;
                    handler.sendMessage(message);
                }
            });
        }

        if (view == binding.send){
            String text = binding.chatContent.getText().toString();
            ChatUtils.sendChat(text, CHAT1);
            binding.content.append(text+"\n");
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ChatUtils.addListener(mMessageListener);
    }

    @Override protected void onStop() {
        super.onStop();
        // 移除消息监听
        ChatUtils.removeListener(mMessageListener);
    }

    @Override
    public void onMessageReceived(List<EMMessage> list) {
        Message message = handler.obtainMessage();
        for (EMMessage data : list){
            message.obj = ((EMTextMessageBody) data.getBody()).getMessage();
            handler.sendMessage(message);
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
}
