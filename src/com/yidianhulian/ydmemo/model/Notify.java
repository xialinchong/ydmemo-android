package com.yidianhulian.ydmemo.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.yidianhulian.framework.Api;
import com.yidianhulian.ydmemo.fragment.MemoDetail;

/**
 * 动态
 * @author leeboo
 *
 */
public class Notify extends Model{
    
    public Notify(JSONObject json) {
        super(json);
    }
    @Override
    public void buildAttrsFromJson(JSONObject json) {
        if(json==null) return;
        setAttr("msg",    Api.getStringValue(json, "msg"));
        setAttr("id",    Api.getStringValue(json, "id"));
        setAttr("type",         Api.getStringValue(json, "type"));
        setAttr("created_on",         Api.getStringValue(json, "created_on"));
        setAttr("extra",       Api.getStringValue(json, "extra"));
        setAttr("clickable",       Api.getStringValue(json, "clickable"));
    }
    
    public JSONObject extra(){
        try{
            return new JSONObject(getAttr("extra"));
        }catch(Exception e){
            return null;
        }
    }
    public boolean isClickable(){
        try{
            return  "1".equalsIgnoreCase(getAttr("clickable"));
        }catch(Exception e){
            return false;
        }
    }
    public String date(){
        try{
            return  getAttr("created_on");
        }catch(Exception e){
            return null;
        }
    }
    public long memoId(){
        JSONObject json = extra();
        if(json==null)return -1;
        try {
            return json.getLong("memo_id");
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }
    public String type(){
        return getAttr("type");
    }
    public String msg(){
        return getAttr("msg");
    }
    
    /**
     * 通知要打开备忘的那个标签卡
     * @return
     */
    public String openMemoType(){
        String type = type();
        if("comment".equals(type) || "reply".equals(type) || "at".equals(type)){
            return MemoDetail.MEMO_DETAIL_TYPE_COMMENTS;
        }
        if("mark".equals(type)){
            return MemoDetail.MEMO_DETAIL_TYPE_TRACE;
        }
        if("edit_memo".equals(type) || "add_follower".equals(type) || "remove_follower".equals(type)){
            return MemoDetail.MEMO_DETAIL_TYPE_CONTENT;
        }
        return "";
    }
}
