package com.yidianhulian.ydmemo.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

abstract public class Model implements Parcelable {
    protected Map<String, String> mAttrs;
	protected JSONObject mJson;
	//是否需要从本地删除
	protected boolean mWillRemoveFromCache = false;

	public boolean isWillRemoveFromCache() {
        return mWillRemoveFromCache;
    }
    public void setWillRemoveFromCache(boolean willRemoveFromCache) {
        this.mWillRemoveFromCache = willRemoveFromCache;
    }
    public static final Map<Class<? extends Model>, Integer> clsMaps = new HashMap<Class<? extends Model>, Integer>(){
        private static final long serialVersionUID = -2505861680358519420L;

        {
	        put(Comment.class,    1);
	        put(Memo.class,       2);
	        put(Reminder.class,   3);
	        put(User.class,       4);
	        put(Option.class,     5);
	        put(Notify.class,     6);
    	}
	};
    public static final Parcelable.Creator<Model> CREATOR = new Parcelable.Creator<Model>() {
        @Override
        public Model createFromParcel(Parcel source) {
            try {
                switch(source.readInt()){
                case 1: return new Comment(new JSONObject(source.readString()));
                case 2: return new Memo(new JSONObject(source.readString()));
                case 3: return new Reminder(new JSONObject(source.readString()));
                case 4: User user =  new User(new JSONObject(source.readString()));
                    return user;
                case 5: return new Option(new JSONObject(source.readString()));
                case 6: return new Notify(new JSONObject(source.readString()));
                }
                
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public Model[] newArray(int size) {
            return null;
        }  
    };

	public Model setAttr(String key, String value){
	    if(mAttrs==null)mAttrs = new HashMap<String, String>();
	    if(mJson==null)mJson = new JSONObject();
	    if(value==null){
	        mAttrs.remove(key);
	        mJson.remove(key);
	        return this;
	    }
	    
		mAttrs.put(key, value);
		try {
            mJson.put(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
		return this;
	}
	public String getAttr(String key){
	    if(mAttrs==null)mAttrs = new HashMap<String, String>();
		return mAttrs.get(key);
	}
	
	public Map<String, String> attrs(){
	    return mAttrs;
	}
	
	public long id(){
	    String id = mAttrs.get("id");
	    if(id==null)return 0;
	    return Long.valueOf(id);
	}
	public Model(JSONObject json){
	    if(json==null)json = new JSONObject();
	    this.mJson = json;
	    buildAttrsFromJson(json);
	}
	
	public JSONObject json(){
	    return mJson;
	}

	public abstract void buildAttrsFromJson(JSONObject json);

	@Override
    public boolean equals(Object o) {
        if(o==null)return false;
        if( ! (o instanceof Model))return false;
        
        if( ! (o.getClass()==getClass())) return false;
        return ((Model)o).id() == id();
    }
	
	public void copyFrom(Model model){
	    mAttrs = model.mAttrs;
	    mJson  = model.mJson;
	    buildAttrsFromJson(mJson);
	}
	
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(clsMaps.get(this.getClass()));
        dest.writeString(mJson==null ? "" : mJson.toString());
    }
    public void buildStringAttrsFromJson(JSONObject json2) {
        if(json2==null)return;
        Iterator<?> ite = json2.keys();
        while(ite.hasNext()){
            String key = (String)ite.next();
            try {
                setAttr(key, json2.getString(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    
}
