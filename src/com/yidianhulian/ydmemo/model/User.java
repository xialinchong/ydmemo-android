package com.yidianhulian.ydmemo.model;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;

import com.yidianhulian.framework.Api;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;


public class User extends Model {

    private Bitmap mData;
    private String mLocalName;
    private Long mLocalId;

    public User(JSONObject json) {
        super(json);
    }
    public User(YDMemoApplication app, JSONObject json) {
        super(json);
        initLocalUserInfo(app);
    }
    public String getLocalName() {
        return mLocalName;
    }

    public Long getLocalId() {
        return mLocalId;
    }
    public void setLocalId(Long localId) {
        this.mLocalId = localId;
    }
    public void setLocalName(String localName) {
        this.mLocalName = localName;
    }

    public void setLocalAvatar(Bitmap data){
        this.mData = data;
    }
    
    public Bitmap getLocalAvatar(){
        return this.mData;
    }

    @Override
    public void buildAttrsFromJson(JSONObject json) {
        if(json==null) return;
        setAttr("avatar",       Api.getStringValue(json, "avatar"));
        setAttr("cellphone",    Api.getStringValue(json, "cellphone"));
        setAttr("id",           Api.getStringValue(json, "id"));
        setAttr("name",         Api.getStringValue(json, "name"));
        
        //memo的状态
        setAttr("status",       Api.getStringValue(json, "status"));
        
        //comment的trace_type
        setAttr("trace_type",       Api.getStringValue(json, "trace_type"));
    }
    public String traceType(){
        if(getAttr("trace_type")==null) return "";
        return getAttr("trace_type");
    }
    public void traceType(String type){
        
        try {
            mJson.put("trace_type", type);
            setAttr("trace_type", type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public String status(){
        if(getAttr("status")==null) return "";
        return getAttr("status");
    }
    
    public String avatar(){
        if(getAttr("avatar")==null) return "";
        return getAttr("avatar");
    }
    
    public String displayName(){
        if(mLocalName==null || "".equals(mLocalName)) return name();
        return mLocalName.trim();
    }
    
    public String name(){
        if(getAttr("name")==null) return "";
        return getAttr("name").trim();
    }
    
    public String cellphone(){
        if(getAttr("cellphone")==null) return "";
        return getAttr("cellphone");
    }

    @Override
    public boolean equals(Object o) {
        if(o==null)return false;
        if( ! (o instanceof User)) return false;
        User o1 = (User)o;
        if(Util.isEmpty(o1.cellphone()))return false;
        return o1.cellphone().equals(cellphone());
    }
    
    /**
     * 初始化本地用户信息，主要是用户在本地的头像，名字
     * 
     * @param app
     */
    public void initLocalUserInfo(YDMemoApplication app){
        Map<String, User> locals = app.localContacts();
        if(locals ==null )return;
        User user = locals.get(cellphone());
        if(user==null)return;
        initLocalUserInfo(user);
        
    }
    public void initLocalUserInfo(User localUser){
        if(localUser ==null )return;
        setLocalAvatar(localUser.getLocalAvatar());
        setLocalName(localUser.getLocalName());
        setLocalId(localUser.getLocalId());
        
    }
}
