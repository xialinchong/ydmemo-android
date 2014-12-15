package com.yidianhulian.ydmemo.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.model.User;
import com.yidianhulian.ydmemo.widget.MemoUsers;

public class SendSms extends Activity {

    protected static final int API_SEND_SMD      = 1;
    public static final String ARG_SHARE_TIP     = "ARG_SHARE_TIP";
    public static final String ARG_SHARE_DEFAULT = "ARG_SHARE_DEFAULT";
    public static final String ARG_USERS         = "ARG_USERS";
    private TextView mShareTip;
    private MemoUsers mShareUsers;
    private EditText mShareText;
    private ImageButton mShareBtn;
    private List<User> mUsers = new ArrayList<User>();
    private YDMemoApplication mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share);
        mApp        = (YDMemoApplication)getApplication();
        mShareTip   = (TextView)findViewById(R.id.share_tip);
        mShareUsers = new MemoUsers(this, (ViewGroup)findViewById(R.id.share_users));
        mShareText  = (EditText)findViewById(R.id.share_text);
        mShareBtn   = (ImageButton)findViewById(R.id.share_send);
        mShareBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mShareText.getText().toString().trim().isEmpty()){
                    Util.showToast(SendSms.this, "说点什么吧");
                    return;
                }
                Util.showLoading(SendSms.this, "发送短信中");
                for (User user : mUsers) {
                    Util.sendSMS(user.cellphone(), mShareText.getText().toString());
                }
                Util.showToast(SendSms.this, "邀请发送成功");
                Util.hideLoading();
                finish();
            }
        });
        mShareTip.setText(Html.fromHtml(getIntent().getStringExtra(ARG_SHARE_TIP)));
        mShareText.setText(getIntent().getStringExtra(ARG_SHARE_DEFAULT));
        mUsers = getIntent().getParcelableArrayListExtra(ARG_USERS);
        
        mShareUsers.shown(false, false, mUsers);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setIcon(R.drawable.logo);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
