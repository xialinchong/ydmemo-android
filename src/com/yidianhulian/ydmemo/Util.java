package com.yidianhulian.ydmemo;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RotateDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Intents;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.View.MeasureSpec;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.ImageLoader;
import com.yidianhulian.ydmemo.model.Memo;
import com.yidianhulian.ydmemo.model.Model;
import com.yidianhulian.ydmemo.model.Option;
import com.yidianhulian.ydmemo.model.Reminder;
import com.yidianhulian.ydmemo.model.User;

public class Util {
    public static final String URI = "http://3.ydmemoapi.vipsinaapp.com/";
    // public static final String URI = "http://ydmemoapi.vipsinaapp.com/";
    public static final String URI_MY_MEMO = URI + "mymemos.php";
    public static final String URI_LOGIN = URI + "login.php";
    public static final String URI_MY_FOLLOW_MEMO = URI + "followmemos.php";
    public static final String URI_SHARE_MEMO = URI + "memoinvite.php";
    public static final String URI_MEMO_ASSIGN_TO_ME = URI + "assign_to_me.php";
    public static final String URI_MEMO_SHARE_TO_ME = URI + "share_to_me.php";
    public static final String URI_MEMO_REFUSE_TO_ME = URI + "refuse_to_me.php";
    public static final String URI_MISC = URI + "misc.php";
    public static final String URI_HANDLE_SHARE = URI + "processfollow.php";
    public static final String URI_REMOVE_MEMO = URI + "memodel.php";
    public static final String URI_SAVE_SETTING = URI + "save_setting.php";
    public static final String URI_POST_MEMO = URI + "memoadd.php";
    public static final String URI_LOAD_COMMENT = URI + "comments.php";
    public static final String URI_LOAD_MEMO = URI + "memo.php";
    public static final String URI_ADD_REMINDER = URI + "reminderadd.php";
    public static final String URI_POST_SUGGEST = URI + "post_suggest.php";
    public static final String URI_POST_COMMENT = URI + "commentadd.php";
    public static final String URI_MEMO_EDIT = URI + "memoedit.php";
    public static final String URI_SIGN_UP = URI + "user_register.php";
    public static final String URI_VERIFY_CELLPHONE = URI
            + "verify_cellphone.php";
    public static final String URI_CHECK_SIGNUP_USER = URI
            + "check_signup_users.php";
    public static final String URI_INVITE_USER = URI + "user_invite.php";
    public static final String URI_MEMO_CLOSED = URI + "memo_closed.php";
    public static final String URI_SAVE_PROFILE = URI + "save_profile.php";
    public static final String URI_UPDATE_TOKEN = URI + "update_token.php";
    public static final String URI_COMMENT_HANDLE = URI + "comment_handle.php";
    public static final String URI_GET_BY_ID = URI + "getbyid.php";
    public static final String URI_REMOVE_REMIND = URI + "reminderdel.php";
    public static final String URI_MODIFY_REMIND = URI + "reminderedit.php";
    public static final String URI_NOTIFICATION = URI + "notification.php";
    public static final String URI_REMOVE_NOTIFICATION = URI + "remove_notification.php";
    public static final String URI_UPDATE_READ_COMMENT = URI + "update_last_read_comment.php";

    public static ProgressDialog loading;
    public static Map<Button, Object[]> loadingButtons = new HashMap<Button, Object[]>();

    /**
     * 定义通知打开备忘的行为,通知用
     */
    public static final String ACTION_OPEN_MEMO = "open_memo";
    /**
     * 新建备忘的通知
     */
    public static final int M_NEW_MEMO = 0;
    /**
     * 新建提醒的透传消息
     */
    public static final int M_NEW_REMIND = 1;
    /**
     * 新建留言的通知
     */
    public static final int M_NEW_COMMENT = 2;
    /**
     * 邀请参与备忘的通知
     */
    public static final int M_MEMO_INVITE = 3;
    /**
     * 产生动态的通知
     */
    public static final int M_NEW_DYNAMIC = 4;
    
    /**
     * 闹钟提醒的通知
     */
    public static final int M_REMIND_ALARM = 999;

    /**
     * 定义本地存储的提醒的键名称
     */
    public static final String REMINDS_NAME = "reminds";
    /**
     * 本地存储的提醒
     */
    public static JSONObject mLocalReminds;

    /**
     * 注册验证码获取的间隔时间限制,单位为秒
     */
    public static int VERIFY_CODE_TIME_LIMIT = 60;

    /**
     * 定义推送自定义参数保存在本地的key
     */
    public static final String PUSH_PARAMS_KEY = "push_params";

    //API 错误码，与服务端一致
    
    /**
     * 数据被删除了
     */
    public static final int ERROR_CODE_DATA_REMOVED = -1;
    
    /**
     * 判断指定字符串是否为空
     * 
     * @param s
     * @return
     */
    public static boolean isEmpty(String s) {
        if (null == s)
            return true;
        if (s.length() == 0)
            return true;
//        if (s.trim().length() == 0)
//            return true;
//        某些情况下空格不能认为是empty，所以把控制权交给调用着 leeboo

        return false;
    }

    public static void showLoadingAtButton(final Button btn, Activity context,
            final String msg) {
        if (loadingButtons.containsKey(btn)) {
            return;// has loading
        }

        Drawable[] oriDrawables = btn.getCompoundDrawables();
        String oriText = btn.getText().toString();

        final RotateDrawable rd = (RotateDrawable) context.getResources()
                .getDrawable(R.drawable.loading_anim);
        rd.setBounds(0, 0, rd.getMinimumWidth(), rd.getMinimumHeight());
        btn.setCompoundDrawables(rd, null, null, null);

        btn.setEnabled(false);
        btn.setText(msg);

        rd.setLevel(250);

        RotateAnimation ra = new RotateAnimation(0f, 0f);
        ra.setRepeatCount(Animation.INFINITE);
        ra.setRepeatMode(Animation.RESTART);
        ra.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                rd.setLevel(rd.getLevel() + 250);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }
        });
        btn.startAnimation(ra);
        loadingButtons.put(btn, new Object[] { oriText, oriDrawables, ra });
    }

    public static void hideLoadingAtButton(Button btn, Activity context) {
        if (!loadingButtons.containsKey(btn)) {
            return;
        }
        Object[] datas = loadingButtons.get(btn);
        Drawable[] drawables = (Drawable[]) datas[1];
        RotateAnimation ra = (RotateAnimation) datas[2];
        btn.setText((String) datas[0]);
        btn.setCompoundDrawables(drawables[0], drawables[1], drawables[2],
                drawables[3]);
        ra.cancel();
        ra = null;
        btn.setEnabled(true);
        loadingButtons.remove(btn);
    }

    public static void showLoading(Activity context, String msg) {
        if (loading != null) {
            loading.dismiss();
            loading = null;
        }

        loading = new ProgressDialog(context);
        loading.setCancelable(false);
        loading.setMessage(msg);
        loading.show();
    }

    public static void hideLoading() {
        if (loading != null) {
            synchronized (loading) {
                if (loading.isShowing()) {
                    loading.dismiss();
                    loading = null;
                }
            }
        }
    }

    /**
     * 消息提示
     * 
     * @param msg
     */
    public static void showToast(Context context, String msg) {
        Toast myToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        myToast.setGravity(Gravity.CENTER, 0, 0);
        myToast.show();
    }

    /**
     * 检查json，接口调用正确返回true，其他情况返回false
     * 
     * @param context
     * @param result
     * @param defaultMsg
     *            当返回当json为空时显示（即不知道显示什么消息）
     * @return
     */
    public static boolean checkResult(Context context, JSONObject result,
            String defaultMsg) {
        if (result == null) {
            Util.showToast(context, defaultMsg);
            return false;
        }
        String msg = Api.getStringValue(result, "msg");
        if (null == msg || msg.isEmpty()) {
            msg = defaultMsg;
        }
        if (!"true".equalsIgnoreCase(Api.getStringValue(result, "success"))) {
            Util.showToast(context, msg);
            return false;
        }
        return true;
    }

    public static int getErrorCode(JSONObject result) {
        if (result == null) {
            return 0;
        }
        return Api.getIntegerValue(result, "errorCode");
    }
    public static boolean checkResult(JSONObject result) {
        if (result == null) {
            return false;
        }
        if (!"true".equalsIgnoreCase(Api.getStringValue(result, "success"))) {
            return false;
        }
        return true;
    }
    
    public static Map<String, User> localContacts(Context context) {
        Map<String, User> users = new HashMap<String, User>();
        
        // 获得所有的联系人
        Cursor cur = context.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME
                        + " COLLATE LOCALIZED ASC");
        // 循环遍历
        if (cur == null) return null;
        if (cur.moveToFirst()) {
            int idColumn = cur.getColumnIndex(ContactsContract.Contacts._ID);
            int displayNameColumn = cur
                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

            do {
                // 获得联系人的ID号
                Long contactId = cur.getLong(idColumn);
                // 获得联系人姓名
                String disPlayName = cur.getString(displayNameColumn);

                // 联系人头像
                Uri uri = ContentUris.withAppendedId(
                        ContactsContract.Contacts.CONTENT_URI, contactId);
                InputStream input = ContactsContract.Contacts
                        .openContactPhotoInputStream(
                                context.getContentResolver(), uri);
                Bitmap contactIcon = BitmapFactory.decodeStream(input);

                // 查看该联系人有多少个电话号码。如果没有这返回值为0
                int phoneCount = cur
                        .getInt(cur
                                .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                if (phoneCount == 0)
                    continue;

                // 获得联系人的电话号码
                Cursor phones = context.getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                + " = " + contactId, null, null);
                if (phones.moveToFirst()) {
                    do {
                        // 遍历所有的电话号码
                        String phoneNumber = phones
                                .getString(phones
                                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        // String phoneType =
                        // phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

                        User user = new User(null);
                        user.setAttr("id", "0");
                        user.setLocalName(disPlayName);
                        user.setLocalAvatar(contactIcon);
                        user.setLocalId(contactId);
                        user.setAttr("cellphone", phoneNumber);

                        users.put(user.cellphone(), user);
                    } while (phones.moveToNext());
                }
                phones.close();
            } while (cur.moveToNext());
        }

        cur.close();

        return users;
    }

    public static void sendSMS(String cellphone, String msg) {
        if (msg == null || msg.isEmpty())
            return;
        SmsManager smsManager = SmsManager.getDefault();
        /** 切分短信，每七十个汉字切一个，不足七十就只有一个：返回的是字符串的List集合 */
        List<String> texts = smsManager.divideMessage(msg);

        // 发送之前检查短信内容是否为空

        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            smsManager.sendTextMessage(cellphone, null, text, null, null);
        }
    }

    public static void loadAvatar(YDMemoApplication app, User user,
            ImageView imageView) {
        if (user == null) {
            imageView.setImageResource(R.drawable.avatar);
            return;
        }
        user.initLocalUserInfo(app);
        Bitmap localAvatar = user.getLocalAvatar();
        String airAvatar = user.avatar();
        if (localAvatar != null) {
            imageView.setImageBitmap(localAvatar);
        } else if (airAvatar != null && !airAvatar.isEmpty()) {
            new ImageLoader(app).loadImage(imageView, airAvatar);
        } else {
            imageView.setImageResource(R.drawable.avatar);
        }
    }

    /**
     * @author xialinchong
     * @param app
     * @param imageView
     * @2014-11-26
     */
    public static void loadBackground(YDMemoApplication app, ImageView imageView) {
        CacheHelper helper = new CacheHelper(app);
        Option option = helper.getSetting(app.loginUser().id());
        if (option == null)
            return;
        String airAvatar = option.background();
        if (airAvatar != null && !airAvatar.isEmpty()) {
            new ImageLoader(app).loadImage(imageView, airAvatar);
        } else {
            imageView.setImageResource(R.drawable.memo_bg_img);
        }
    }

    /**
     * @desc 把大图片压缩
     * @author xialinchong
     * @param path原始图片路径
     * @return 压缩后的图片路径
     */
    public static String compressImage(String srcPath) {  
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;  
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
          
        newOpts.inJustDecodeBounds = false;  
         
        newOpts.inSampleSize = 2; 
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        int bitmapWidth = bitmap.getWidth();  
        int bitmapHeight = bitmap.getHeight();  
        // 缩放图片的尺寸  
        float scaleWidth = (float) bitmapWidth / bitmapWidth;
        if (bitmapWidth * 2 > 500) {
            scaleWidth = (float) 500 / bitmapWidth;
        }
        Matrix matrix = new Matrix();  
        matrix.postScale(scaleWidth, scaleWidth);  
        // 产生缩放后的Bitmap对象  
        Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, false);
        return compressImage(resizeBitmap);
    }
    
    public static String compressImage(Bitmap image) {  
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        int options = 100;  
//        while ( baos.toByteArray().length / 1024 > 100) {
//            baos.reset(); 
//            image.compress(Bitmap.CompressFormat.JPEG, options, baos); 
//            options -= 10;  
//        }  
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());  
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        File myCaptureFile;
        try {
            myCaptureFile = File.createTempFile("compressed", "jpg");

            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(myCaptureFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            return myCaptureFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";  
    }
    
    /**
     * @判断sd卡是否存在
     * @author xialinchong
     * @return true or false
     */
    public static boolean isSdcardExisting() {
        final String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 
     * @param v
     *            按钮或者其他视图
     * @param app
     *            Application context
     * @param context
     *            activity 点击头像进入联系人详情或者新建联系人界面
     */
    public static void showContact(User user, Activity context) {

        if (user == null) {
            return;
        }

        if (user.getLocalId() != null && user.getLocalId() > 0) {
            Uri uri = ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI, user.getLocalId());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        } else {
            Uri inserUri = ContactsContract.Contacts.CONTENT_URI;
            Intent intent = new Intent(Intent.ACTION_INSERT, inserUri);
            intent.putExtra(Intents.Insert.NAME, user.displayName());
            intent.putExtra(Intents.Insert.PHONE, user.cellphone());
            context.startActivity(intent);
        }
    }

    public static void registerPush(final YDMemoApplication app) {
        // XGPushManager.registerPush(app.getApplicationContext(),
        // new XGIOperateCallback() {
        //
        // @Override
        // public void onSuccess(Object arg0, int arg1) {
        // System.out.println("ydhl---------注册成功，设备token为：" + arg0);
        // String deviceToken = (String) arg0;
        // app.deviceToken = deviceToken;
        // User loginUser = app.loginUser();
        // if (loginUser != null && deviceToken != null) {
        // Map<String, String> data = new HashMap<String, String>();
        // data.put("token", deviceToken);
        // data.put("type", "android");
        // data.put("action", "add");
        // data.put("uid", "" + app.loginUser().id());
        // try {
        // new Api("post", Util.URI_UPDATE_TOKEN, data)
        // .invoke();
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // }
        // }
        //
        // @Override
        // public void onFail(Object arg0, int arg1, String arg2) {
        // System.out
        // .println("ydhl---------注册失败，错误码：" + arg1 + ",错误信息：" + arg2);
        // }
        // });

        // 改用极光推送,@HuJinhao,@2014-11-25
        AsyncInvokeApi.registerPush(app);
    }

    /**
     * 推送本地通知
     * 
     * @author HuJinhao
     * @since 2014-10-30
     * @param context
     * @param title
     * @param content
     * @param custom_params
     * @param type
     * @return
     */
    public static void pushLocalNotification(Context context, String title,
            String content, String custom_params, int type) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        BitmapDrawable bitmap = (BitmapDrawable)context.getResources().getDrawable(R.drawable.ic_launcher);
        builder.setContentTitle(title).setContentText(content)
                .setWhen(System.currentTimeMillis()).setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setLargeIcon(bitmap.getBitmap());
        

        switch (type) {
        case Util.M_NEW_MEMO:
        case Util.M_MEMO_INVITE:
            builder.setSmallIcon(R.drawable.notify_invited_memos);
            break;
        case Util.M_NEW_COMMENT:
            builder.setSmallIcon(R.drawable.notify_comment);
            break;
        case Util.M_NEW_REMIND:
            builder.setSmallIcon(R.drawable.notify_new_reminder);
            break;
        case Util.M_REMIND_ALARM:
            builder.setSmallIcon(R.drawable.notify_alarm_small);
            break;
        }

        Intent resultIntent = new Intent(context,
                com.yidianhulian.ydmemo.activity.Splash.class);
        resultIntent.putExtra(Util.PUSH_PARAMS_KEY, custom_params);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
                0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);

        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        nm.notify(0, notification);
    }

    /**
     * 闹钟响的时候,也发一个通知,点击通知打开闹钟页面
     * 
     * @param context
     * @param memo_subject
     * @param reminder
     */
    public static void sendAlarmNotification(Context context,
            String memo_subject, Reminder reminder) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context);
        BitmapDrawable bitmap = (BitmapDrawable)context.getResources().getDrawable(R.drawable.ic_launcher);
        builder.setSmallIcon(R.drawable.notify_alarm_small)
            .setLargeIcon(bitmap.getBitmap())
            .setContentTitle(reminder.title())
            .setContentText(String.format(context.getString(R.string.notification_reminder_content), memo_subject))
                .setTicker(context.getString(R.string.remind_ticker))
                .setWhen(System.currentTimeMillis()).setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_ALL);

        Intent resultIntent = new Intent(context,
                com.yidianhulian.ydmemo.activity.RemindAlert.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.putExtra("subject", memo_subject);
        resultIntent.putExtra("reminder", reminder);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
                0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(resultPendingIntent);

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        

        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int) reminder.id(), notification);
    }

    /**
     * 创建备忘的提醒闹钟,主要是接受邀请时调用
     * 
     * @author HuJinhao
     * @since 2014-10-30
     * @param memo
     */
    public static void createMemoAlarms(Context context, Memo memo) {
        if (memo == null || memo.reminders() == null)
            return;

        for (Reminder reminder : memo.reminders()) {
            createAlarmReminder(context, memo.subject(), reminder);
        }
    }

    /**
     * 创建提醒闹钟
     * 
     * @param context
     * @param memo_subject
     * @param reminder
     */
    public static void createAlarmReminder(Context context,
            String memo_subject, Reminder reminder) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        String remind_time = reminder.calculate_alarm_time(System
                .currentTimeMillis());
        long remind_id = reminder.id();

        if (remind_time.isEmpty()) {
            removeLocalRemind(context, String.valueOf(remind_id));
            return;
        }

        try {
            calendar.setTime(format.parse(remind_time));
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }
        long timeInMillis = calendar.getTimeInMillis();

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION);
        intent.putExtra("subject", memo_subject);
        intent.putExtra("reminder", reminder);

        PendingIntent sender = PendingIntent.getBroadcast(context,
                (int) remind_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context
                .getSystemService(Activity.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, timeInMillis, sender);

        // 把闹钟存在本地,以便关机重启再启动之
        storeLocalReminds(context, memo_subject, reminder);
    }

    /**
     * 
     * 创建延迟多少分钟之后的提醒闹钟
     * 
     * @param memo_subject
     * @param reminder
     * @param alarmAfterMinute
     */
    public static void createDelayAlarmReminder(Context context,
            String memo_subject, Reminder reminder, int alarmAfterMinute) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION);
        intent.putExtra("subject", memo_subject);
        intent.putExtra("reminder", reminder);

        PendingIntent sender = PendingIntent.getBroadcast(context,
                (int) reminder.id(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context
                .getSystemService(Activity.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, alarmAfterMinute);

        am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
    }

    /**
     * 获取本地存储的闹钟提醒
     */
    public static JSONObject getLocalReminds(Context context) {
        if (mLocalReminds != null)
            return mLocalReminds;

        String reminds = new CacheHelper(context).getOption(REMINDS_NAME);
        if (reminds != null) {
            try {
                mLocalReminds = new JSONObject(reminds);
                return mLocalReminds;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 存储提醒到本地
     * 
     * @param context
     * @param title
     * @param content
     * @param remind_time
     */
    public static void storeLocalReminds(Context context, String memo_subject,
            Reminder reminder) {
        JSONObject v = new JSONObject();
        try {
            v.put("reminder", reminder.json());
            v.put("subject", memo_subject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        CacheHelper cacheHelper = new CacheHelper(context);
        if (mLocalReminds == null) {
            try {
                String reminds = cacheHelper.getOption(REMINDS_NAME);
                if (reminds == null)
                    reminds = "{}";
                mLocalReminds = new JSONObject(reminds);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String remind_id = String.valueOf(reminder.id());
        if (mLocalReminds != null && !mLocalReminds.has(remind_id)) {
            try {
                mLocalReminds.put(remind_id, v);
                cacheHelper.setOption(REMINDS_NAME, mLocalReminds.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 移除指定的本地提醒
     * 
     * @param context
     * @param title
     * @param content
     * @param remind_time
     */
    public static void removeLocalRemind(Context context, String remind_id) {
        mLocalReminds = getLocalReminds(context);
        if (mLocalReminds != null) {
            if (mLocalReminds.has(remind_id)) {
                mLocalReminds.remove(remind_id);
                new CacheHelper(context).setOption(REMINDS_NAME,
                        mLocalReminds.toString());
            }
        }
    }

    /**
     * 删除提醒
     * 
     * @param context
     * @param reminder
     */
    public static void removeAlarm(Context context, String remind_id) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                Integer.valueOf(remind_id), intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context
                .getSystemService(Activity.ALARM_SERVICE);

        am.cancel(pendingIntent);
    }

    /**
     * 播放声音
     * 
     * @author HuJinhao
     * @since 2014-11-03
     * @param context
     * @return
     */
    public static int PlaySound(Context context) {
        NotificationManager mgr = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification nt = new Notification();
        nt.defaults = Notification.DEFAULT_SOUND;
        int soundId = new Random(System.currentTimeMillis())
                .nextInt(Integer.MAX_VALUE);
        mgr.notify(soundId, nt);

        return soundId;
    }

    /**
     * 震动
     * 
     * @author HuJinhao
     * @since 2014-11-03
     * @param context
     * @return
     */
    public static int PlayVibrate(Context context) {
        NotificationManager mgr = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification nt = new Notification();
        nt.defaults |= Notification.DEFAULT_VIBRATE;
        int id = new Random(System.currentTimeMillis())
                .nextInt(Integer.MAX_VALUE);
        mgr.notify(id, nt);

        return id;
    }

    public static String dateFormat(Context context, String from) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
       if(from==null)return "";
        try {
            Date fromDate = format.parse(from);
            Date now = new Date();
            long diff = (now.getTime() - fromDate.getTime()) / 1000;
            if (diff < 60) {
                return "刚才";
            } else if (diff < 3600) {
                return (int) Math.floor(diff / 60) + "分钟前";
            } else if (diff < 86400) {
                return (int) Math.floor(diff / 3600) + "小时前";
            } else {
                return DateUtils.formatDateTime(context, fromDate.getTime(),
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
                                | DateUtils.FORMAT_ABBREV_ALL);
            }

        } catch (Exception e) {
            // e.printStackTrace();
        }

        return from;
    }

    /**
     * 本地产生数据，更新缓存及涉及到的ui；推送的数据更新由MessageReceiver处理
     * 
     * @param Context
     * @param model
     * @return TODO
     */
    public static boolean updateCacheAndUI(Context context, Model model,
            Long loginUid) {
        // 如果context是activity，但activity被销毁了，这出现在activity异步加载数据，但是数据返回前activity就finish了
        if (context != null) {
            new CacheHelper(context).update(model, String.valueOf(loginUid));
        }

        boolean res = false;
        for (Activity activity : YDMemoApplication.getActivities()) {
            res |= ((Refreshable) activity).refresh(model);
        }
        return res;
    }

    /**
     * 从缓存中删除某个数据
     * 
     * @param context
     * @param model
     * @param loginUid
     * @return
     */
    public static boolean removeCacheAndUI(Context context, Model model,
            Long loginUid) {
        model.setWillRemoveFromCache(true);
        return Util.updateCacheAndUI(context, model, loginUid);
    }

    /**
     * 获取指定时间的微秒时间戳
     * 
     * @param date
     * @return
     */
    public static long getTimestampInMillis(String date) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            calendar.setTime(format.parse(date));
            return calendar.getTimeInMillis();
        } catch (ParseException e) {
            return 0;
        }
    }

    /**
     * 获取指定微秒时间戳对应的时间
     * 
     * @param timeInMillis
     * @return
     */
    public static String getTimeByMillis(long timeInMillis) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);

        return format.format(calendar.getTime());
    }

    /**
     * 取得根据id值取得R.string中的名字
     * 
     * @param value
     * @return
     */
    public static String getStringNameById(int value) {
        Field[] fields = R.string.class.getDeclaredFields();
        if (fields == null)
            return null;

        for (Field field : fields) {
            try {
                if (field.getInt(null) == value)
                    return field.getName();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * string 根据字段名取得其值
     * @param name
     * @return
     */
    public static int getStringIdByName(String name) {
        if (name == null)
            return -1;

        Field[] fields = R.string.class.getDeclaredFields();
        if (fields == null)
            return -1;

        for (Field field : fields) {
            try {
                if (field.getName().equals(name))
                    return field.getInt(null);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * drawable 根据字段名取得其值
     * @param name
     * @return
     */
    public static int getDrawableIdByName(String name) {
        if (name == null)
            return -1;

        Field[] fields = R.drawable.class.getDeclaredFields();
        if (fields == null)
            return -1;

        for (Field field : fields) {
            try {
                if (field.getName().equals(name))
                    return field.getInt(null);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }
    
    /**
     * 把comment字符串中的:cellphone:换成标签显示
     * 
     * @param mApp
     * @param comment
     * @param followers
     * @return
     */
    public static SpannableString showAtUser(YDMemoApplication mApp, String comment, 
            Map<String, User> followers){

        Pattern pattern = Pattern.compile(":([^:]+):");
        Matcher mat = pattern.matcher(comment);  
        boolean finded = false;
        
        List<User> users = new ArrayList<User>();
        List<String> placeholders = new ArrayList<String>();
        
        while( mat.find() ){  
            finded = true;

            String cellphone = mat.group(1);
            User user = followers.get(cellphone);
            if(user != null){
                user.initLocalUserInfo(mApp);
                users.add(user);
            }else{
                users.add(null);
            }
            
            placeholders.add(mat.group(0));
        }

        
        pattern = null;
        mat = null;
        if( ! finded){
            return new SpannableString(comment);
        }else{
            return Util.showAtUser(mApp, comment, 
                    users.toArray(new User[users.size()]), placeholders.toArray(new String[users.size()]));
        }

    }

    public static SpannableString showAtUser(Context context, String source, 
            User[] users, String[] placeholders){
        SpannableString spannableString  = new SpannableString(source); 
        
        for (int i = 0; i < placeholders.length; i++) {
            User user = users[i];
            String placeholder = placeholders[i];
            
            try{
                TextView textView = new TextView(context);
                if(user != null){
                    textView.setText("@"+user.displayName());
                }else{
                    textView.setText("@"+placeholder.replaceAll(":", ""));
                }

                
                textView.setPadding(2, 2, 2, 2);
                textView.setTextSize(12);
                textView.setGravity(Gravity.CENTER);
                textView.setBackgroundResource(R.drawable.at_user_bg);
                textView.setTextColor(context.getResources().getColor(android.R.color.white));
                textView.setDrawingCacheEnabled(true);
                textView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                textView.layout(0, 0, textView.getMeasuredWidth(),
                        textView.getMeasuredHeight());

            
                Bitmap newBitmap = textView.getDrawingCache();
                ImageSpan imageSpan = new ImageSpan(context, newBitmap);
                
                
                
                int start = source.indexOf(placeholder);
                
                spannableString.setSpan(imageSpan, start, start + placeholder.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        
        return spannableString;
    }
    
    /**
     * 根据星期几计算对应的日期,主要是提醒用
     * @author HuJinhao
     * @param weekday 1-7
     * @return
     */
    public static Date calculateDateByWeekday(int weekday) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(new Date());
    	
    	int dayofweek = calendar.get(Calendar.DAY_OF_WEEK);
		if (dayofweek != 1) { //不是星期日
			int diff_days = weekday - dayofweek + 1;
			if (diff_days < 0) {
				diff_days = 7 - dayofweek + 1 + weekday;
			}
			if (diff_days > 0) {
				calendar.add(Calendar.DAY_OF_YEAR, diff_days);
			}
		} else {
			if (weekday != 7) {
				calendar.add(Calendar.DAY_OF_YEAR, weekday);
			}
		}
		
		return calendar.getTime();
    }
    
    /**
     * 根据日期计算对应的星期几
     * @author HuJinhao
     * @param Date date 
     * @return
     */
    public static int calculateWeekdayByDate(Date date) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(date);

    	int weekday = calendar.get(Calendar.DAY_OF_WEEK);
    	if (weekday == 1) { //星期天
    		return 7;
    	} else {
    		return weekday - 1;
    	}
    }
    
    public static void openConfirmDialog(Activity context, 
            int titleRes, int iconRes, int msgRes, DialogInterface.OnClickListener confirmListener){
      //xialinchong 弹出确认对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleRes)
                .setIcon(iconRes)
                .setMessage(msgRes)
                .setNegativeButton(R.string.ok, confirmListener)
                .setPositiveButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
    }
    
    public static String getAppVersionName(YDMemoApplication app){
        PackageInfo pi;
        try {
            pi = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
            return pi.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
