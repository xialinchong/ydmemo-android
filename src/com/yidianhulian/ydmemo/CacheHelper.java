package com.yidianhulian.ydmemo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;

import com.yidianhulian.framework.db.KVHandler;
import com.yidianhulian.ydmemo.fragment.Profile;
import com.yidianhulian.ydmemo.model.Comment;
import com.yidianhulian.ydmemo.model.Memo;
import com.yidianhulian.ydmemo.model.Model;
import com.yidianhulian.ydmemo.model.Option;
import com.yidianhulian.ydmemo.model.Reminder;
import com.yidianhulian.ydmemo.model.User;

@SuppressLint("UseSparseArrays")
public class CacheHelper {
    private Context mContext;
    /**
     * 本地缓存memo列表的key
     */
    public static final HashMap<Integer, String> MEMOS_URL = new HashMap<Integer, String>() {
        private static final long serialVersionUID = 1L;
        {
            put(Profile.MEMO_TYPE_MY_FOLLOW, Util.URI_MY_FOLLOW_MEMO);
            put(Profile.MEMO_TYPE_MY_MEMO, Util.URI_MY_MEMO);
            put(Profile.MEMO_TYPE_REFUSED_MEMO, Util.URI_MEMO_REFUSE_TO_ME);
            put(Profile.MEMO_TYPE_RECEIVED_INVITE, Util.URI_MEMO_SHARE_TO_ME);
            put(Profile.MEMO_TYPE_RECEIVED_MEMO, Util.URI_MEMO_ASSIGN_TO_ME);
            put(Profile.MEMO_TYPE_CLOSED_MEMO, Util.URI_MEMO_CLOSED);
        }
    };

    public static final String MEMO_URI = Util.URI_LOAD_MEMO;

    public CacheHelper(Context context) {
        super();
        this.mContext = context;
    }

    /**
     * 异步更新缓存数据库ydhlcache
     * 
     * @param mModel
     * @param mUserId
     */
    public void update(final Model mModel, final String mUserId) {
        if (mUserId == null)
            return;
        
        new Thread("Update Cache:"+mModel.getClass().getSimpleName()+":"+mModel.id()) {
            KVHandler db = new KVHandler(mContext, "ydhlcache", null, 1);
            @Override
            public void run() {

                try {
                    if (mModel instanceof Memo)
                        updateMemo(db, (Memo) mModel, mUserId);
                    if (mModel instanceof Comment)
                        updateComment(db, (Comment) mModel, mUserId);
                    if (mModel instanceof Reminder)
                        updateReminder(db, (Reminder) mModel, mUserId);
                    if (mModel instanceof User)
                        saveUser((User) mModel);
                    if (mModel instanceof Option)
                        saveSetting(Long.valueOf(mUserId), (Option) mModel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                db.close();
            }

        }.start();
    }
    
    public String getOption(String name) {
        KVHandler db = new KVHandler(mContext, "memo", null, 1);
        String value = db.getValue(name);
        db.close();
        return value;
    }

    public void setOption(String name, String value) {
        KVHandler db = new KVHandler(mContext, "memo", null, 1);
        db.setValue(name, value);
        db.close();
    }
    public User loginUser() {
        KVHandler db = new KVHandler(mContext, "memo", null, 1);
        String user = db.getValue("loginUser");
        if (user == null || "".equalsIgnoreCase(user))
            return null;
        try {
            return new User(new JSONObject(user));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        db.close();
        return null;
    }

    public void saveUser(User user) {
        KVHandler db = new KVHandler(mContext, "memo", null, 1);
        if (user == null) {
            db.setValue("loginUser", "");
        } else {
            db.setValue("loginUser", user.json().toString());
        }
        db.close();
        return;
    }

    /**
     * 
     * 保存指定用户的设置,比如保存背景时需用来更新本地缓存
     * @author HuJinhao
     * @param uid
     * @param data
     * @return
     */
    public void saveSetting(Long uid, Option option) {
    	KVHandler db = new KVHandler(mContext, "ydhlcache", null, 1);    	
    	try {
    		JSONObject json = new JSONObject();
			json.put("data", option.json());
			db.setValue(String.format("%s?uid=%s", Util.URI_MISC, uid), json.toString());   	
		} catch (JSONException e) {
			e.printStackTrace();
		}
			
        db.close();      
    }
    public Option getSetting(Long uid) {
        KVHandler db    = new KVHandler(mContext, "ydhlcache", null, 1);
        try {
            JSONObject json = new JSONObject(db.getValue(String.format("%s?uid=%s", Util.URI_MISC, uid)));
            return new Option(json.getJSONObject("data"));
        } catch (Exception e) {
        }
        db.close();
        return null;
    }
    
    private void updateReminder(KVHandler mDb, Reminder reminder, String mUserId) {
        String memoId = String.valueOf(reminder.memo_id());
        String reminderId = String.valueOf(reminder.id());

        // 更新列表
        for (Entry<Integer, String> entry : MEMOS_URL.entrySet()) {
            String key = String.format("%s?uid=%s", entry.getValue(), mUserId);
            String value = mDb.getValue(key);
            if (value == null || value.isEmpty())
                continue;

            try {

                JSONObject json = new JSONObject(value);
                if (json.getJSONObject("data").has(memoId)) {
                    JSONObject memo = json.getJSONObject("data").getJSONObject(memoId);
                    
                    if(reminder.isWillRemoveFromCache()){
                        memo.getJSONObject("reminds").remove(reminderId);
                    }else{
                        memo.getJSONObject("reminds").put(reminderId, reminder.json());
                    }

                    json.getJSONObject("data").put(memoId, memo);
                    mDb.setValue(key, json.toString());
                }
                
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // 更新详情
        String memoKey = String.format("%s?uid=%s&memo_id=%s",
                CacheHelper.MEMO_URI, mUserId, memoId);
        String memoString = mDb.getValue(memoKey);
        if (memoString != null) {
            try {
                JSONObject json = new JSONObject(memoString);
                if(reminder.isWillRemoveFromCache()){
                    json.getJSONObject("data").getJSONObject("reminds").remove(reminderId);
                }else{
                    json.getJSONObject("data").getJSONObject("reminds")
                    .put(reminderId, reminder.json());
                }
                
                mDb.setValue(memoKey, json.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateComment(KVHandler mDb, Comment comment, String mUserId) {
        String memoId = String.valueOf(comment.memo_id());
        
        String commentId;
        if(comment.id()==0){
            commentId = String.valueOf(comment.getPostToken());
        }else{
            commentId = String.valueOf(comment.id());
        }

        // 更新列表
        for (Entry<Integer, String> entry : MEMOS_URL.entrySet()) {
            String key = String.format("%s?uid=%s", entry.getValue(), mUserId);
            String value = mDb.getValue(key);
            if (value == null || value.isEmpty())
                continue;

            try {

                JSONObject json = new JSONObject(value);
                if (json.getJSONObject("data").has(memoId)) {
                    JSONObject memo = json.getJSONObject("data").getJSONObject(memoId);
                    
                    if(comment.isWillRemoveFromCache()){
                        memo.getJSONObject("comments").remove(commentId);
                    }else{
                        memo.getJSONObject("comments").put(commentId,comment.json());
                    }

                    json.getJSONObject("data").put(memoId, memo);
                    mDb.setValue(key, json.toString());
                }
                
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // 更新详情
        String memoKey = String.format("%s?uid=%s&memo_id=%s",
                CacheHelper.MEMO_URI, mUserId, memoId);
        String memoString = mDb.getValue(memoKey);
        if (memoString != null) {
            try {
                JSONObject json = new JSONObject(memoString);
                if(comment.isWillRemoveFromCache()){
                    json.getJSONObject("data").getJSONObject("comments").remove(commentId);
                }else{
                    json.getJSONObject("data").getJSONObject("comments").put(commentId, comment.json());
                }
                mDb.setValue(memoKey, json.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public Memo getMemo(Long memoId, Long uid){
        KVHandler db = new KVHandler(mContext, "ydhlcache", null, 1);    
        String memoKey = String.format("%s?uid=%s&memo_id=%s",
                CacheHelper.MEMO_URI, uid, memoId);
        String memoString = db.getValue(memoKey);
        try {
            JSONObject json = new JSONObject(memoString);
            return new Memo(json.getJSONObject("data"));
        } catch (Exception e) {
            return null;
        }
    }
    
    private void updateMemo(KVHandler mDb, Memo memo, String mUserId) {
        String memoId = String.valueOf(memo.id());
        long uid =Long.valueOf(mUserId);

        //先拿出memo并合并
        String memoKey = String.format("%s?uid=%s&memo_id=%s",CacheHelper.MEMO_URI, mUserId, memoId);
        String memoString = mDb.getValue(memoKey);
        if (memoString != null) {
            try {
                JSONObject json = new JSONObject(memoString);
                JSONObject oldComments = json.getJSONObject("data")
                        .getJSONObject("comments");
                JSONObject newMemoJson = memo.json();

                mergeMemo(oldComments, newMemoJson);
                json.put("data", newMemoJson);
                mDb.setValue(memoKey, json.toString());
                memo  = new Memo(json.getJSONObject("data"));//合并后的memo
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        // 更新详情
        if( (!memo.isAssigner(uid) && !memo.isFollower(uid)) || memo.isWillRemoveFromCache()){
            mDb.removeKey(memoKey);
        }
        
        
        //先从列表中移除
        for (Entry<Integer, String> entry : MEMOS_URL.entrySet()) {
            String key = String.format("%s?uid=%s", entry.getValue(), mUserId);
            String value = mDb.getValue(key);
            if (value == null || value.isEmpty()) continue;

            try {
                JSONObject json = new JSONObject(value);
                if (json.getJSONObject("data").has(memoId)) {
                    json.getJSONObject("data").remove(memoId);
                    mDb.setValue(key, json.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        //在归类到合适的
        
        if(memo.isClosed() && (memo.isAssigner(uid) || memo.isFollower(uid))){
            String key = String.format("%s?uid=%s", MEMOS_URL.get(Profile.MEMO_TYPE_CLOSED_MEMO), mUserId);
            addMemoAtCache(mDb, key, memo);
        }else if(memo.isAssigner(uid) && "accept".equals(memo.assignerStatus())){
            String key = String.format("%s?uid=%s", MEMOS_URL.get(Profile.MEMO_TYPE_MY_MEMO), mUserId);
            addMemoAtCache(mDb, key, memo);
        }else if(memo.isFollower(uid) && "accept".equals(memo.followerStatus(uid))){
            String key = String.format("%s?uid=%s", MEMOS_URL.get(Profile.MEMO_TYPE_MY_FOLLOW), mUserId);
            addMemoAtCache(mDb, key, memo);
        }else if(memo.isFollower(uid) && "pending".equals(memo.followerStatus(uid))){
            String key = String.format("%s?uid=%s", MEMOS_URL.get(Profile.MEMO_TYPE_RECEIVED_INVITE), mUserId);
            addMemoAtCache(mDb, key, memo);
        }

    }
    
    private void addMemoAtCache(KVHandler mDb, String key, Memo memo){
        String memoId = String.valueOf(memo.id());
        String value  = mDb.getValue(key);
        if (value == null || value.isEmpty()) return;

        try {

            JSONObject json = new JSONObject(value);
            if(memo.isWillRemoveFromCache()){
                json.getJSONObject("data").remove(memoId);
            }else{
                json.getJSONObject("data").put(memoId, memo.json());
            }
            mDb.setValue(key, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    
    private void mergeMemo(JSONObject oldComments, JSONObject newMemoJson) {
        Iterator<?> keys = oldComments.keys();

        while (keys.hasNext()) {
            String key = keys.next().toString();
            try {
                newMemoJson.getJSONObject("comments").put(key,
                        oldComments.getJSONObject(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
