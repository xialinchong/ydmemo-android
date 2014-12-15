package com.yidianhulian.ydmemo.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import com.yidianhulian.ydmemo.Util;

/**
 * 用户选项设置对象
 * @author leeboo
 *
 */
public class Option extends Model {
    /**
     * 关注邀请
     */
    public static final String MISC_TOTAL_FOLLOW = "total_follow";
    public static final String MISC_TOTAL_REFUSE = "total_refuse";
    public static final String MISC_TOTAL_CLOSED = "total_closed";
    public static final String MISC_TOTAL_MINE   = "total_mine";
    public static final String MISC_TOTAL_INVITED = "total_invite";
    public static final String MISC_TOTAL_ASSIGN  = "total_assign";
    
    public Option(JSONObject json) {
        super(json);
    }

    @Override
    public void buildAttrsFromJson(JSONObject json) {
        super.buildStringAttrsFromJson(json);
    }

    public Integer totalFollow() {
        try{
            return Integer.valueOf(getAttr(MISC_TOTAL_FOLLOW));
        }catch(Exception e){
            return 0;
        }
    }
    public Integer totalClosed() {
        try{
            return Integer.valueOf(getAttr(MISC_TOTAL_CLOSED));
        }catch(Exception e){
            return 0;
        }
    }
    public Integer totalRefuse() {
        try{
            return Integer.valueOf(getAttr(MISC_TOTAL_REFUSE));
        }catch(Exception e){
            return 0;
        }
    }
    
    public boolean noticeMyMemoHasComment(){
        String temp = getAttr("notice_for_me_my_memo_has_comment");
        return temp==null || "yes".equals(temp);
    }

    public String background(){
        return getAttr("background");
    }
    public void background(String bg){
        if(Util.isEmpty(bg))return;
        setAttr("background", bg);
    }
    public boolean noticeFollowHasComment(){
        String temp = getAttr("notice_for_me_follow_has_comment");
        return temp==null || "yes".equals(temp);
        
    }
    
    public int alertInterval(){
        try{
            return Integer.valueOf(getAttr("alert_interval"));
        }catch(Exception e){
            return 5;
        }
    }

    /**
     * 我的备忘总数
     * @return
     */
    public int totalMine() {
        try{
            return Integer.valueOf(getAttr(MISC_TOTAL_MINE));
        }catch(Exception e){
            return 0;
        }
    }

    /**
     * 受邀总数
     * @return
     */
    public int totalInvited() {
        try{
            return Integer.valueOf(getAttr(MISC_TOTAL_INVITED));
        }catch(Exception e){
            return 0;
        }
    }
    
    public boolean hasNotification(){
        String temp = getAttr("has_notification");
        return "yes".equals(temp);
    }
    
    public List<String> marks(){
        List<String> marks = new ArrayList<String>();
        String str = getAttr("marks");
        if( ! Util.isEmpty(str)){
            String[] arr = str.trim().split(",");
            return new ArrayList<String>(Arrays.asList(arr));
        }
        return marks;
    }
}
