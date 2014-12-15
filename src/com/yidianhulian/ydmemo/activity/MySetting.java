package com.yidianhulian.ydmemo.activity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.framework.ImageLoader;
import com.yidianhulian.ydmemo.CacheHelper;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Refreshable;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.model.Model;
import com.yidianhulian.ydmemo.model.Option;
import com.yidianhulian.ydmemo.model.User;

/**
 * 关于易点备忘录
 * 
 * @author leeboo
 * 
 */
public class MySetting extends Activity implements CallApiListener, Refreshable {

    private YDMemoApplication mApp;

    private ImageView mAvatar;
    private ImageView mBackground;
    private TextView mNickname;
    private Button mSelectAvatar;
    private Button mSelectBackGround;
    private Button mSelectUserName;
    private Button mSelectPassWord;
    private static final int RESULT_LOAD_AVATAR = 0;// 选择头像
    // private static final int RESULT_LOAD_BACKGROUND = 1;// 选择背景
    private static final int SAVE_AVATAR_CODE = 2;
    private static final int SAVE_BACKGROUND_CODE = 3;
    private static final int SAVE_DELAY_SETTING = 4;
    protected static final int API_UPDATE_SETTING = 5;
    private String mAvatarPicPath = null;// 原始图片路劲
    private String mAvatarCompressPicture = null;// 压缩后图片
    private String mBgPicPath = null;// 原始图片路劲
    private String mBgCompressPicture = null;// 压缩后图片
    private TextView mBackgroudProgress;
    private Button mMarkConfig;

    private RadioGroup mDelaySetting;
    private RadioButton mFive;
    private RadioButton mTen;
    private RadioButton mThirty;
    private RadioButton mSixty;
    private int mCheckedVal;
    private Button mNotifyMy;
    private CheckBox mNotifyFollowComment;
    private CheckBox mNotifyMyComment;
    private Option mOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_setting);
        mApp = (YDMemoApplication) getApplication();

        restoreActionBar();
        CacheHelper helper = new CacheHelper(mApp);
        mOption = helper.getSetting(mApp.loginUser().id());

        mAvatar = (ImageView) findViewById(R.id.avatar);

        // mBackground = (ImageView) findViewById(R.id.background);

        mNickname = (TextView) findViewById(R.id.nickname);
        // mBackgroudProgress = (TextView)
        // .findViewById(R.id.background_progress);
        mSelectAvatar = (Button) findViewById(R.id.select_avatar);
        mSelectAvatar.setOnClickListener(new SelectImageOnClickListener(
                RESULT_LOAD_AVATAR));

        // mSelectBackGround = (Button)
        // .findViewById(R.id.select_background);
        // mSelectBackGround.setOnClickListener(new SelectImageOnClickListener(
        // SAVE_BACKGROUND_CODE));

        mSelectUserName = (Button) findViewById(R.id.select_username);
        mSelectUserName.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 把fragment换成activity xialinchong 2014-12-11
                Intent intent = new Intent();
                intent.setClass(MySetting.this, EditNickName.class);
                startActivity(intent);
            }
        });
        mSelectPassWord = (Button) findViewById(R.id.select_pwd);
        mSelectPassWord.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // MainActivity mainActivity = (MainActivity) this;
                // mainActivity.pushFragment(new EditPassWord(),
                // "EditPassWord");
                // 把fragment换成activity xialinchong 2014-12-11
                Intent intent = new Intent();
                intent.setClass(MySetting.this, EditPassWord.class);
                startActivity(intent);
            }
        });
        mMarkConfig = (Button) findViewById(R.id.select_mark);
        mMarkConfig.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MySetting.this, MarkComment.class);
                intent.putExtra(MarkComment.CONFIG, true);
                MySetting.this.startActivity(intent);
            }
        });

        mDelaySetting = (RadioGroup) findViewById(R.id.delay_setting);
        mFive = (RadioButton) findViewById(R.id.five);
        mTen = (RadioButton) findViewById(R.id.ten);
        mThirty = (RadioButton) findViewById(R.id.thirty);
        mSixty = (RadioButton) findViewById(R.id.sixty);

        mDelaySetting
                .setOnCheckedChangeListener(new OnCheckedChangeListenerImp());

        mNotifyMyComment = (CheckBox) findViewById(R.id.my_has_comment_chk);
        mNotifyFollowComment = (CheckBox) findViewById(R.id.follow_has_comment_chk);

        mNotifyMy = (Button) findViewById(R.id.notify_to_me_my_has_comment);
        mNotifyMy.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mNotifyMyComment.setChecked(!mNotifyMyComment.isChecked());
                Map<String, String> setting = new HashMap<String, String>();
                String bool = mNotifyMyComment.isChecked() ? "yes" : "no";
                setting.put("name", "notice_for_me_my_memo_has_comment");
                setting.put("value", bool);
                CallApiTask.doCallApi(API_UPDATE_SETTING, MySetting.this,
                        MySetting.this, setting);
            }
        });

        Button notifyFollow = (Button) findViewById(R.id.notify_to_me_follow_has_comment);
        notifyFollow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mNotifyFollowComment.setChecked(!mNotifyFollowComment
                        .isChecked());
                Map<String, String> setting = new HashMap<String, String>();
                String bool = mNotifyFollowComment.isChecked() ? "yes" : "no";
                setting.put("name", "notice_for_me_follow_has_comment");
                setting.put("value", bool);
                CallApiTask.doCallApi(API_UPDATE_SETTING, MySetting.this,
                        MySetting.this, setting);
            }
        });

        shown();
    }

    class OnCheckedChangeListenerImp implements OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (mFive.getId() == checkedId) {
                mCheckedVal = 5;
            } else if (mTen.getId() == checkedId) {
                mCheckedVal = 10;
            } else if (mThirty.getId() == checkedId) {
                mCheckedVal = 30;
            } else if (mSixty.getId() == checkedId) {
                mCheckedVal = 60;
            }
            if (mOption.alertInterval() != mCheckedVal && mCheckedVal!=0)
                loadData(SAVE_DELAY_SETTING, false);
        }

    }

    private void shown() {
        mNickname.setText(mApp.loginUser().name());
        // Util.loadBackground(mApp, mBackground);
        Util.loadAvatar(mApp, mApp.loginUser(), mAvatar);

        mDelaySetting.clearCheck();
        if (mOption != null) {
            switch (mOption.alertInterval()) {
            case 5:
                mDelaySetting.check(mFive.getId());
                break;
            case 10:
                mDelaySetting.check(mTen.getId());
                break;
            case 30:
                mDelaySetting.check(mThirty.getId());
                break;
            case 60:
                mDelaySetting.check(mSixty.getId());
                break;
            }
        } else {
            mDelaySetting.check(mFive.getId());
        }

        boolean myMemoHasComment = mOption.noticeMyMemoHasComment();
        boolean followHasComment = mOption.noticeFollowHasComment();

        mNotifyFollowComment.setChecked(followHasComment);
        mNotifyMyComment.setChecked(myMemoHasComment);
    }

    /**
     * 头像或背景选择按钮事件
     * 
     * @author xialinchong 2014-11-18
     */
    class SelectImageOnClickListener implements OnClickListener {
        private int mImageType;

        public SelectImageOnClickListener(int imageType) {
            this.mImageType = imageType;
        }

        @Override
        public void onClick(View v) {
            if (Util.isSdcardExisting()) {
                Intent intent;
                if (Build.VERSION.SDK_INT < 19) {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");

                } else {
                    intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                }
                startActivityForResult(intent, mImageType);
            } else {
                Toast.makeText(v.getContext(), "请插入sd卡", Toast.LENGTH_LONG)
                        .show();
            }
        }

    }

    private void loadData(int what, boolean showLoading) {
        //if (showLoading) {
            Util.showLoading(this, this.getString(R.string.please_waiting));
        //}
        CallApiTask.doCallApi(what, MySetting.this, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
        case RESULT_LOAD_AVATAR:
            cropPhoto(data.getData(), SAVE_AVATAR_CODE, 0, 0);
            break;

        case SAVE_AVATAR_CODE:
            if (data != null) {
                showResizeImage(data, mAvatar);
                loadData(SAVE_AVATAR_CODE, true);
            }
            break;
        // case RESULT_LOAD_BACKGROUND:
        // DisplayMetrics dm = new DisplayMetrics();
        // this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        // int width = dm.widthPixels;
        // int height = dm.heightPixels;
        // cropPhoto(data.getData(), SAVE_BACKGROUND_CODE, width, height);
        // break;

        case SAVE_BACKGROUND_CODE:
            if (data != null) {
                // showResizeImage(data, mBackground);
                ContentResolver cr = this.getContentResolver();
                Cursor cursor = cr
                        .query(data.getData(), null, null, null, null);
                cursor.moveToFirst();
                mBgPicPath = cursor.getString(cursor.getColumnIndex("_data"));
                if (mBgPicPath != null && !mBgPicPath.equals("")) {
                    mBgCompressPicture = Util.compressImage(mBgPicPath);
                    loadData(SAVE_BACKGROUND_CODE, true);
                    ImageLoader loader = new ImageLoader(this);
                    loader.loadImage(mBackground, mBgCompressPicture);

                }
                // loadData(SAVE_BACKGROUND_CODE);
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 调用系统的裁剪
     * 
     * @param uri
     */
    public void cropPhoto(Uri uri, int resize_code, int width, int height) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        if (width == 0) {
            // aspectX aspectY 是宽高的比例
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            // outputX outputY 是裁剪图片宽高
            intent.putExtra("outputX", 150);
            intent.putExtra("outputY", 150);
        }
        // else {
        // intent.putExtra("aspectX", width);
        // intent.putExtra("aspectY", height);
        // intent.putExtra("outputX", width);
        // intent.putExtra("outputY", height);
        // }

        intent.putExtra("return-data", true);
        startActivityForResult(intent, resize_code);
    }

    private void showResizeImage(Intent data, ImageView imageView) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(getResources(), photo);
            imageView.setImageDrawable(drawable);

            File myCaptureFile;
            try {
                myCaptureFile = File.createTempFile("crop_avatar", "jpg");

                BufferedOutputStream bos = new BufferedOutputStream(
                        new FileOutputStream(myCaptureFile));
                photo.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
                photo.recycle();
                mAvatarPicPath = myCaptureFile.getAbsolutePath();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setIcon(R.drawable.logo);
        actionBar.setTitle(R.string.my_setting);
    }

    @Override
    public Api getApi(Context context, int what, Object... params) {
        Map<String, String> data = new HashMap<String, String>();

        List<String> files = new ArrayList<String>();
        switch (what) {
        case SAVE_AVATAR_CODE:
            if (mAvatarPicPath == null)
                break;

            files.add("avatar");
            data.put("avatar", mAvatarPicPath);
            return new Api("post", Util.URI_SAVE_PROFILE + "?uid="
                    + mApp.loginUser().id(), data, files);

        case SAVE_BACKGROUND_CODE:
            if (mBgPicPath == null)
                break;

            files.add("background");
            data.put("background", mBgPicPath);
            return new Api("post", Util.URI_SAVE_PROFILE + "?uid="
                    + mApp.loginUser().id(), data, files);
        case SAVE_DELAY_SETTING:
            data.put("alert_interval", mCheckedVal + "");
            return new Api("post", Util.URI_SAVE_PROFILE + "?uid="
                    + mApp.loginUser().id(), data);
        case API_UPDATE_SETTING:
            return new Api("post", String.format("%s?uid=%s",
                    Util.URI_SAVE_SETTING, mApp.loginUser().id()),
                    (Map<String, String>) params[0]);
        }
        return null;

    }

    @Override
    public boolean isCallApiSuccess(Context context, int what,
            JSONObject result, Object... params) {
        return Util.checkResult(result);
    }

    @Override
    public String getCacheKey(Context context, int what, Object... params) {
        return null;
    }

    @Override
    public void updateProgress(Context context, int what, float percent,
            Object... params) {
        if (mBackgroudProgress == null)
            return;
        if (percent >= 1.0f) {
            mBackgroudProgress.setText(getString(R.string.please_waiting));
        } else {
            mBackgroudProgress.setText(String.format("%d%%",
                    (int) (percent * 100)));
        }
    }

    @Override
    public void handleResult(Context context, int what, JSONObject result,
            boolean isDone, Object... params) {
        Util.hideLoading();
        if (!this.isCallApiSuccess(context, what, result)) {
            if (result == null) {
                Util.showToast(this, "网络错误！");
                return;
            }
            Util.showToast(this, Api.getStringValue(result, "msg"));
            return;
        }
        JSONObject data = Api.getJSONValue(result, "data", JSONObject.class);
        switch (what) {
        case SAVE_AVATAR_CODE:
            User user = new User(mApp, data);
            mApp.saveUser(user);
            Util.showToast(this, "头像修改成功！");
            Util.updateCacheAndUI(this, user, user.id());
            break;

        case SAVE_BACKGROUND_CODE:
            Option option = new Option(data);
            Util.updateCacheAndUI(this, option, mApp.loginUser().id());
            mBackgroudProgress.setVisibility(View.GONE);
            Util.showToast(this, "背景修改成功！");

            break;
        }
    }

    @Override
    public void apiNetworkException(Context context, int what, Exception e,
            Object... params) {
        Util.showToast(this, getString(R.string.network_error));
    }

    @Override
    public boolean refresh(Model model) {
        if (model instanceof User) {
            mApp.saveUser((User) model);
            shown();
        }

        return false;
    }

    @Override
    public JSONObject handleCache(Context context, int what, JSONObject from,
            JSONObject to, Object... params) {
        return from;
    }
}
