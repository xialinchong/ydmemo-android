package com.yidianhulian.ydmemo.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;

import com.yidianhulian.framework.Api;


public class Comment extends Model{
    private User mCommenter;
    private Map<Long, User> mTraceUsers;
    private Comment mReply;
    public enum CommentType{
        COMMENT_TEXT,
        COMMENT_PIC
    }
    public boolean needRepost(){
        return "1".equals(getAttr("needReposted"));
    }
    public void setNeedReposted(String isNeed){
        setAttr("needReposted", isNeed);
    }
    public String getPostToken() {
        return getAttr("post_token");
    }
    public void setPostToken(String postToken) {
        setAttr("post_token", postToken);
    }
    public void uploadProgress(float percent){
        setAttr("upload_progress", String.valueOf(percent));
    }
    public float uploadProgress(){
        try{
            return Float.valueOf(getAttr("upload_progress"));
        }catch(Exception e){
            return 0f;
        }
    }
    public Comment(JSONObject json) {
        super(json);
    }
    @SuppressLint("UseSparseArrays")
    @Override
    public void buildAttrsFromJson(JSONObject json) {
        mTraceUsers = new HashMap<Long, User>();
        if(json==null) return;
        setAttr("comment",    Api.getStringValue(json, "comment"));
        setAttr("id",         Api.getStringValue(json, "id"));
        setAttr("date",       Api.getStringValue(json, "date"));
        setAttr("post_token", Api.getStringValue(json, "post_token"));
        setAttr("trace_type", Api.getStringValue(json, "trace_type"));
        setAttr("memo_id",    Api.getStringValue(json, "memo_id"));
        setAttr("file_path",  Api.getStringValue(json, "file_path"));
        setAttr("file_type",  Api.getStringValue(json, "file_type"));
        setAttr("ori_file_path",  Api.getStringValue(json, "ori_file_path"));
        setAttr("local_file_path",  Api.getStringValue(json, "local_file_path"));
        setAttr("needReposted",  Api.getStringValue(json, "needReposted"));
        setAttr("post_token",  Api.getStringValue(json, "post_token"));
        
        mCommenter = new User( Api.getJSONValue(json, "commenter", JSONObject.class));
        JSONObject reply = Api.getJSONValue(json, "reply", JSONObject.class);
        if(reply!=null && reply.length() > 0){
            mReply = new Comment( reply );
        }else{
            mReply = null;
        }

        JSONObject tu = Api.getJSONValue(json, "trace_users", JSONObject.class);
        if(tu != null){
            Iterator<?> keys = tu.keys();
            while(keys.hasNext()){
                String key = keys.next().toString();
                try {
                    mTraceUsers.put(Long.valueOf(key), new User(tu.getJSONObject(key)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public Map<Long, User> getTraceUsers(){
        return mTraceUsers;
    }
    
    public String oriFilePath(){
        return getAttr("ori_file_path");
    }
    public String filePath(){
        return getAttr("file_path");
    }
    public void filePath(String path){
        setAttr("file_path", path);
    }
    public void localFilePath(String path){
        setAttr("local_file_path", path);
    }
    public String localFilePath(){
       return getAttr("local_file_path");
    }
    public String fileType(){
        return getAttr("file_type");
    }
    public CommentType commentType(){
        if("pic".equals(fileType())) return CommentType.COMMENT_PIC;
        return CommentType.COMMENT_TEXT;
    }
    public void fileType(String type){
        setAttr("file_type", type);
    }
    public User commenter(){
        return mCommenter;
    }
    public void setCommenter(User user){
        mCommenter = user;
        try {
            mJson.put("commenter", user.json());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    public String comment(){
        return getAttr("comment");
    }
    public String date(){
        return getAttr("date");
    }
    public long memo_id(){
        try{
            return Long.valueOf(getAttr("memo_id"));
        }catch(Exception e){
            return -1;
        }
    }
    public boolean isDraft() {
        return "1".equals(getAttr("is_draft"));
    }
    public void setIsDraft(String val){
        setAttr("is_draft", val);
    }
    public Comment getReply() {
        return mReply;
    }
}
