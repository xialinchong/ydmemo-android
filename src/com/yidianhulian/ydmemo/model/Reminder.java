package com.yidianhulian.ydmemo.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;

import android.content.res.Resources;

import com.yidianhulian.framework.Api;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Util;


public class Reminder extends Model{

    private User mCreater;

    public Reminder(JSONObject json) {
        super(json);
    }

    @Override
    public void buildAttrsFromJson(JSONObject json) {
        if(json==null) return;
        setAttr("date", Api.getStringValue(json, "date"));
        setAttr("id", Api.getStringValue(json, "id"));
        setAttr("status", Api.getStringValue(json, "status"));
        setAttr("title", Api.getStringValue(json, "title"));
        setAttr("memo_id", Api.getStringValue(json, "memo_id"));
        setAttr("created_on", Api.getStringValue(json, "created_on"));
        setAttr("gps", Api.getStringValue(json, "gps"));
        setAttr("repeat_start_on", Api.getStringValue(json, "repeat_start_on"));
        setAttr("repeat_end_on", Api.getStringValue(json, "repeat_end_on"));
        setAttr("repeat_every", Api.getStringValue(json, "repeat_every"));
        setAttr("repeat_type", Api.getStringValue(json, "repeat_type"));
        setAttr("repeat_on", Api.getStringValue(json, "repeat_on"));
        
        mCreater = new User(Api.getJSONValue(json, "creater", JSONObject.class));
    }
	
    public String date(){
        return getAttr("date");
    }
    
    public String title(){
        return getAttr("title");
    }
    
    public boolean isCreater(Long id){
        if(mCreater==null)return false;
        return mCreater.id()==id;
    }
    
    public long memo_id(){
        try{
            return Long.valueOf(getAttr("memo_id"));
        }catch(Exception e){
            return -1;
        }
    }
    
    public String gps(){
    	String gps = getAttr("gps");
    	if (gps.isEmpty()) return "";
    	if (gps.equalsIgnoreCase("null")) return "";
    	
        return gps;
    }
    
    public String repeat_start_on(){
        return getAttr("repeat_start_on");
    }
    
    public String repeat_end_on(){
        String str = getAttr("repeat_end_on");
        if(str==null || "null".equalsIgnoreCase(str))return "";
        return str;
    }
    
    public int repeat_every(){
    	try{
            return Integer.valueOf(getAttr("repeat_every"));
        }catch(Exception e){
            return 0;
        }       
    }
    
    public String repeat_type(){
    	String repeat_type = getAttr("repeat_type");
        return repeat_type != null ? repeat_type : "";
    }
    
    public int repeat_on(){
    	try{
            return Integer.valueOf(getAttr("repeat_on"));
        }catch(Exception e){
            return 0;
        }
    }
    
    /**
     * 从指定时间开始计算下一次闹钟响的时间
     * @param timeInMillis 微秒时间戳
     * @return String
     */
    public String calculate_alarm_time(long timeInMillis) {
    	String start_time		= date();
    	String repeat_type 		= repeat_type();
//    	String repeat_start_on 	= repeat_start_on();
    	String repeat_end_on 	= repeat_end_on();
    	int repeat_every 		= repeat_every();
    	int repeat_on 			= repeat_on();
    	long oneDayInMillis		= 86400000; //一天的微秒数
    	if (timeInMillis == 0) {
    		timeInMillis = System.currentTimeMillis();
    	}
    	      
    	long startTimeInMillis 	= Util.getTimestampInMillis(start_time);
    	if (timeInMillis < startTimeInMillis) {
    		return start_time;
    	}
    	
    	if (repeat_type.equalsIgnoreCase("no")) {
    		return "";
    	}
    	
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTimeInMillis(startTimeInMillis);

    	long endTimeInMillis 	= 0;
    	if (repeat_end_on == null || repeat_end_on.isEmpty()) {
    		endTimeInMillis = Long.MAX_VALUE ;
		} else {	
			//日期后面的时间对应的微秒时间戳
	    	long leftTimeInMillis = (calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60 + 
					calendar.get(Calendar.SECOND)) * 1000;
			endTimeInMillis = Util.getTimestampInMillis(repeat_end_on + " 00:00:00") + leftTimeInMillis;
			if (timeInMillis > endTimeInMillis) {
	    		return "";
	    	}
		}
    	
    	if ("week".equalsIgnoreCase(repeat_type)) { //按周
    		int dayofweek = calendar.get(Calendar.DAY_OF_WEEK);
    		if (dayofweek != 1) { //不是星期日
    			int left_dayofweek = repeat_on - dayofweek + 1;
    			if (left_dayofweek < 0) {
    				left_dayofweek = 7 - dayofweek + 1 + repeat_on;
    			}
    			startTimeInMillis += (left_dayofweek + (repeat_every - 1) * 7) * oneDayInMillis;
    		} else {
    			startTimeInMillis += ((repeat_every - 1) * 7 + repeat_on) * oneDayInMillis;
    		}
    		
    		while (true) {    			
    			if (startTimeInMillis > timeInMillis && startTimeInMillis <= endTimeInMillis) {
    				return Util.getTimeByMillis(startTimeInMillis);
    			} else if (startTimeInMillis > endTimeInMillis) {
    				return Util.getTimeByMillis(endTimeInMillis);
    			}
    			startTimeInMillis += repeat_every * 7 * oneDayInMillis;
    		}
    	} else {
    		int calendar_field = 0;
    		if ("day".equalsIgnoreCase(repeat_type)) { //按天
    			calendar_field = Calendar.DAY_OF_YEAR;
    		} else if ("month".equalsIgnoreCase(repeat_type)) { //按月
    			calendar_field = Calendar.MONTH;
    		} else if ("year".equalsIgnoreCase(repeat_type)) { //按年
    			calendar_field = Calendar.YEAR;
    		}
    		
    		if (calendar_field == 0) return "";
    		
    		while (true) {
    			calendar.add(calendar_field, repeat_every);
    			long tempInMillis = calendar.getTimeInMillis();
    			if (tempInMillis  > timeInMillis && tempInMillis <= endTimeInMillis) {
    				return Util.getTimeByMillis(tempInMillis);
    			} else if (tempInMillis > endTimeInMillis) {
    				return Util.getTimeByMillis(endTimeInMillis);
    			}
    		}
    	}	
    }

    public String repeatDesc(Resources res) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calender = Calendar.getInstance();
        Date start;
        try {
            start = format.parse(date());
            calender.setTime(start);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        String time = calender.get(Calendar.HOUR_OF_DAY)+":"+calender.get(Calendar.MINUTE);
        String every = repeat_every()==1 ? "" : ""+repeat_every();
        
        if ("day".equalsIgnoreCase(repeat_type())) { //按天
            return String.format(res.getString(R.string.repeat_by_day), every, time); 
        } else if ("month".equalsIgnoreCase(repeat_type())) { //按月
            return String.format(res.getString(R.string.repeat_by_month), every, calender.get(Calendar.DAY_OF_MONTH), time);
        } else if ("year".equalsIgnoreCase(repeat_type())) { //按年
            return String.format(res.getString(R.string.repeat_by_year), every, calender.get(Calendar.MONTH)+1, calender.get(Calendar.DAY_OF_MONTH), time);
        } else if("week".equalsIgnoreCase(repeat_type())){
        	String repeat_on = getWeekDayDesc(repeat_on(), res);
            return String.format(res.getString(R.string.repeat_by_week), every, repeat_on, time);
        }
        return "";
    }

    public boolean isRepeat() {
        return !"no".equals(repeat_type());
    }
    
    public String getWeekDayDesc(int weekday, Resources res) {
    	switch (weekday) {
    		case 1:
    			return res.getString(R.string.monday);
    		case 2:
    			return res.getString(R.string.tuesday);
    		case 3:
    			return res.getString(R.string.wednesday);
    		case 4:
    			return res.getString(R.string.thursday);
    		case 5:
    			return res.getString(R.string.friday);
    		case 6:
    			return res.getString(R.string.saturday);
    		case 7:
    			return res.getString(R.string.sunday);
    	}
    	return "";
    }
    
    /**
     * 是否已经通知
     * @return
     */
    public boolean isClosed(){
        
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date remindDate=null;
        try {
            remindDate = format.parse(date());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        return ! isRepeat() && (remindDate==null || date.getTime() > remindDate.getTime());
    }

    public String createdOn() {
        return getAttr("created_on");
    }

    public User creater() {
        return mCreater;
    }
}
