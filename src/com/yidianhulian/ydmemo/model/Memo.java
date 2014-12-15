package com.yidianhulian.ydmemo.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.yidianhulian.framework.Api;


public class Memo extends Model{
    private User mAssigner;
    private User mCreater;
    private List<User> mFollowers;
    private SortedMap<Long, Reminder> mReminders;
    private SortedMap<Long, Comment> mComments;
    
    public Memo(JSONObject json) {
        super(json);
    }
    @Override
    public void buildAttrsFromJson(JSONObject json) {
        mFollowers  = new ArrayList<User>();
        mReminders  = new TreeMap<Long, Reminder>();
        mComments   = new TreeMap<Long, Comment>();
        
        
        if(json==null) return;
        setAttr("accept_status", Api.getStringValue(json, "accept_status"));
        setAttr("created_on",    Api.getStringValue(json, "created_on"));
        setAttr("desc",          Api.getStringValue(json, "desc"));
        setAttr("id",            Api.getStringValue(json, "id"));
        setAttr("subject",       Api.getStringValue(json, "subject"));
        setAttr("is_closed",     Api.getStringValue(json, "is_closed"));
        setAttr("unread_comment",  Api.getStringValue(json, "unread_comment"));
        
        mAssigner = new User(Api.getJSONValue(json, "assigner", JSONObject.class));
        mCreater  = new User(Api.getJSONValue(json, "creater", JSONObject.class));
        
        
        
        JSONObject cs = Api.getJSONValue(json, "comments", JSONObject.class);
        if(cs != null){
            Iterator<?> keys = cs.keys();
            while(keys.hasNext()){
                String key = keys.next().toString();
                try {
                    mComments.put(Long.valueOf(key), new Comment(cs.getJSONObject(key)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        
        JSONObject rs = Api.getJSONValue(json, "reminds", JSONObject.class);
        if(rs!=null){
            Iterator<?> keys = rs.keys();
            while(keys.hasNext()){
                String key = keys.next().toString();
                try {
                    mReminders.put(Long.valueOf(key), new Reminder(rs.getJSONObject(key)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        
        JSONArray fs = Api.getJSONValue(json, "followers", JSONArray.class);
        if(fs !=null ){
            for(int i=0; i<fs.length(); i++){
                try {
                    mFollowers.add(new User(fs.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public boolean isAssigner(long uid){
        return mAssigner.id()==uid;
    }
    public User creater(){
        return mCreater;
    }
    public User assigner(){
        return mAssigner;
    }
    public List<Reminder> reminders(){
        return new ArrayList<Reminder>(mReminders.values());
    }

    public List<Comment> comments() {
        return new ArrayList<Comment>(mComments.values());
    }
    
    public List<User> followers() {
        return new ArrayList<User>(mFollowers);
    }
    public Comment getLastComment(){
        if(mComments==null || mComments.size()==0)return null;
        return mComments.get(mComments.lastKey());
    }

    public int unread_comment(){
        try{
            return Integer.valueOf(getAttr("unread_comment"));
        }catch(Exception e){
            return 0;
        }
    }
    
    public int unread_reminder(){
        int rst = 0;
        for (Reminder reminder : reminders()) {
            if( ! reminder.isClosed()){
                rst++;
            }
        }
        return rst;
    }
    public String subject(){
        return getAttr("subject");
    }
    public String desc(){
        return getAttr("desc");
    }

    public String date() {
        return getAttr("created_on");
    }
    
    public boolean isFollower(long id){
        for (User user : mFollowers) {
            if(user.id()==id) return true;
        }
        return false;
    }
    
    public String followerStatus(long uid){
        JSONArray fs = Api.getJSONValue(mJson, "followers", JSONArray.class);
        if(fs == null)return "";
        for(int i=0; i<fs.length(); i++){
            try {
                String status = Api.getJSONValue(fs.getJSONObject(i), "status", String.class);
                Long id = Long.valueOf(Api.getStringValue(fs.getJSONObject(i), "id"));
                if(id.equals(uid)){
                    return status;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
    
    public String assignerStatus(){
        return getAttr("accept_status");
    }
    
    public boolean isClosed(){
        return "1".equals(getAttr("is_closed"));
    }
    
    /**
     * 加入刚提交的留言（必须有posttoken）
     * @param comment
     */
    public void addPostComment(Comment comment){
        if(comment.getPostToken()==null)return;
        
        JSONObject cs = Api.getJSONValue(this.mJson, "comments", JSONObject.class);
        try {
            cs.put(comment.getPostToken(), comment.json());
            mComments.put(Long.valueOf(comment.getPostToken()), comment);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /**
     * 再memo中加入新的提醒, 如果存在，则替换;如果reminder标记为删除，则删除
     * @param comment
     */
    public void addReminder(Reminder reminder){
        JSONObject rs = Api.getJSONValue(this.mJson, "reminds", JSONObject.class);
        try {
            if(reminder.isWillRemoveFromCache()){
                rs.remove(String.valueOf(reminder.id()));
                mReminders.remove(reminder.id());
            }else{
                rs.put(String.valueOf(reminder.id()), reminder);
                mReminders.put(reminder.id(), reminder);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 再memo中加入新的留言, 如果留言存在，则替换
     * @param comment
     */
    public void addComment(Comment comment){
        JSONObject cs = Api.getJSONValue(this.mJson, "comments", JSONObject.class);
        try {
            cs.put(String.valueOf(comment.id()), comment.json());
            mComments.put(comment.id(), comment);
            if(comment.commenter().id() != assigner().id()){
                setAttr("unread_comment",  String.valueOf(unread_comment() + 1));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 根据comment中的postToken更新comment，如果comment没有postToken或者没有在comments中没有匹配的，则直接add到comments
     * 主要用于提交留言成功后用新comment更新本地comment
     * 
     * @param comment
     */
    public void updateCommentByPostToken(Comment comment){
        JSONObject cs = Api.getJSONValue(this.mJson, "comments", JSONObject.class);
        
        //更新JSON缓存,更新排序好的json
        
        try {
            cs.remove(comment.getPostToken());
            mComments.remove(Long.valueOf(comment.getPostToken()));
            
            comment.setPostToken(null);
            
            cs.put(String.valueOf(comment.id()), comment.json());
            mComments.put(comment.id(), comment);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        
    }
    
    /**
     * 新留言没有提交成功，则标识他需要重新提交
     * 
     * @param postToken
     */
    public void setCommentNeedRepost(String postToken){
        JSONObject cs = Api.getJSONValue(this.mJson, "comments", JSONObject.class);
        if(cs.has(postToken)){
            try {
                cs.getJSONObject(postToken).put("needReposted", "1");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Long id = Long.valueOf(postToken);
        if(mComments.containsKey(id)){
            mComments.get(id).setNeedReposted("1");
        }
    }
    public boolean isCreater(Long uid) {
        return creater().id() == uid;
    }
    public void uploadCommentProgress(String postToken, float percent) {
        System.out.println("ydhl---------:"+percent);
        JSONObject cs = Api.getJSONValue(this.mJson, "comments", JSONObject.class);
        if(cs.has(postToken)){
            try {
                cs.getJSONObject(postToken).put("upload_progress", String.valueOf(percent));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Long id = Long.valueOf(postToken);
        if(mComments.containsKey(id)){
            mComments.get(id).uploadProgress(percent);
        }
    }
}
