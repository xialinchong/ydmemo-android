package com.yidianhulian.ydmemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.ydmemo.activity.AddRemind;
import com.yidianhulian.ydmemo.activity.MainActivity;
import com.yidianhulian.ydmemo.activity.MarkComment;
import com.yidianhulian.ydmemo.activity.ReplyComment;
import com.yidianhulian.ydmemo.model.Comment;
import com.yidianhulian.ydmemo.model.Comment.CommentType;
import com.yidianhulian.ydmemo.model.Memo;
import com.yidianhulian.ydmemo.model.Option;

/**
 * 用于Comment留言的上下文菜单管理
 * 
 * @author leeboo
 *
 */
@SuppressWarnings("unchecked")
public class CommentContextHandler implements CallApiListener{
    private Activity mActivity;
    Dialog mDlg;
    SimpleAdapter mAdapter;
    ListView mListView;
    private TypedArray mMenuIcons;
    private TypedArray mMenuLabels;
    private List<Map<String, ?>> mMenus = new ArrayList<Map<String,?>>();
    private Comment mComment;
    private Memo mMemo;
    private OnCommentChanged mChangeListener;
    
    
    public CommentContextHandler(Activity activity, Memo memo, OnCommentChanged changeListener){
        mActivity = activity;
        mDlg = new Dialog(activity);
        mDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mChangeListener = changeListener;
        mMemo = memo;
        
        mDlg.setContentView(R.layout.comment_context_menu);
        mDlg.setCancelable(true);
        mDlg.setCanceledOnTouchOutside(true);

        mAdapter = new MarkItemAdapter(mActivity, mMenus, false, null);
        
        mListView = (ListView)mDlg.findViewById(R.id.context_menu_list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                mDlg.dismiss();
                String type = ((Map)mAdapter.getItem(position)).get("type").toString();
                if(type == null)return;
                
                if("copy".equalsIgnoreCase(type) && mComment!=null){
                    ClipboardManager cbm = (ClipboardManager) mActivity
                            .getSystemService(MainActivity.CLIPBOARD_SERVICE);
                    cbm.setPrimaryClip( ClipData.newPlainText( null, mComment.comment() ) );
                    if (cbm.hasPrimaryClip()){  
                        cbm.getPrimaryClip().getItemAt(0).getText();  
                    } 
                    Util.showToast(mActivity, "已成功复制到剪切板");
                    return;
                }else if("reply_comment".equalsIgnoreCase(type) && mComment!=null){
                    Intent intent = new Intent(mActivity, ReplyComment.class);
                    intent.putExtra(ReplyComment.COMMENT,   mComment);
                    intent.putExtra(ReplyComment.MEMO,   mMemo);
                    mActivity.startActivity(intent);
                    return;
                }else if("mark_more".equalsIgnoreCase(type) && mComment!=null){
                    Intent intent = new Intent(mActivity, MarkComment.class);
                    intent.putExtra(MarkComment.COMMENT,   mComment);
                    mActivity.startActivity(intent);
                    return;
                }else if("notify_me_later".equalsIgnoreCase(type) && mComment!=null){
                    Intent intent = new Intent(mActivity, AddRemind.class);
                    intent.putExtra(AddRemind.ARG_MEMO,   mMemo);
                    intent.putExtra(AddRemind.ARG_COMMENT,   mComment);
                    mActivity.startActivity(intent);
                    return;
                }
                else if("download".equalsIgnoreCase(type) && mComment!=null){
                    DownloadManager download = (DownloadManager)mActivity.getSystemService(mActivity.DOWNLOAD_SERVICE);
                    Uri  uri = Uri.parse(mComment.oriFilePath());
                    Request request = new Request(uri);
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE|DownloadManager.Request.NETWORK_WIFI);
                    request.setVisibleInDownloadsUi(true);
                    download.enqueue(request);
                    return;
                }

                Map<String, String> args = new HashMap<String, String>();
                args.put("type", type);
                mChangeListener.beforeChange(mComment);
                CallApiTask.doCallApi(0, CommentContextHandler.this, mActivity, args);
                
            }
        });
         
    }
    public void showCommentContextMenu(Comment comment) {
        mComment = comment;

        if(comment.commentType()==CommentType.COMMENT_PIC){
            mMenuIcons = mActivity.getResources().obtainTypedArray(R.array.pic_comment_menu_icons);
            mMenuLabels = mActivity.getResources().obtainTypedArray(R.array.pic_comment_menu_label);
        }else{
            mMenuIcons = mActivity.getResources().obtainTypedArray(R.array.comment_menu_icons);
            mMenuLabels = mActivity.getResources().obtainTypedArray(R.array.comment_menu_label);
        }
        
        mMenus.clear();

        for (int i=0; i < mMenuIcons.length(); i++) {
            Map<String, Object> item = new HashMap<String, Object>();

            int lableId = mMenuLabels.getResourceId(i, -1);
            String name = Util.getStringNameById(lableId);
            if(name!=null) item.put("type", name);
            
            if(mMenuIcons.getResourceId(i, 0) != 0){
                item.put("icon", mMenuIcons.getResourceId(i, -1));
                item.put("isSection", false);
            }else{
                item.put("isSection", true);
            }
            
            item.put("label", lableId);
            mMenus.add(item);
        }
        
        CacheHelper helper = new CacheHelper(mActivity);
        YDMemoApplication app = (YDMemoApplication)mActivity.getApplication();
        Option misc = helper.getSetting(app.loginUser().id());
        List<String> marks = misc.marks();
        if(marks.size()==0){
            marks.add("mark_as_heart");
        }
        marks.add("mark_more");
        for (String string : marks) {
            Map<String, Object> item = new HashMap<String, Object>();

            int lableId = Util.getStringIdByName(string);
            item.put("type", string);//string value
            item.put("icon", Util.getDrawableIdByName(string));//draw id
            item.put("isSection", false);
            item.put("label", lableId);//string id
            mMenus.add(item);
        }
        
        mMenuIcons.recycle();
        mMenuLabels.recycle();
        mDlg.show();
        mAdapter.notifyDataSetChanged();
    }
    
    @Override
    public Api getApi(Context context, int what, Object... params) {
        Map<String, String> queryStr = (Map<String, String>)params[0];
        YDMemoApplication app = (YDMemoApplication)mActivity.getApplication();
        queryStr.put("uid",         String.valueOf(app.loginUser().id()));
        queryStr.put("comment_id",  String.valueOf(mComment.id()));
        queryStr.put("action",      "add");
        return new Api("post",   Util.URI_COMMENT_HANDLE, queryStr);
    }
    @Override
    public boolean isCallApiSuccess(Context context, int what,JSONObject result, Object... params) {
        return Util.checkResult(result);
    }
    @Override
    public void apiNetworkException(Context context, int what,Exception e, Object... params) {
        
    }
    @Override
    public String getCacheKey(Context context, int what, Object... params) {
        return null;
    }
    @Override
    public void handleResult(Context context, int what, JSONObject result, boolean isDone,
            Object... params) {
        if(isCallApiSuccess(context, what, result)){
            YDMemoApplication app = (YDMemoApplication)mActivity.getApplication();
            Util.updateCacheAndUI(context, new Comment(Api.getJSONValue(result, "data", JSONObject.class)), app.loginUser().id());
        }else{
            mChangeListener.changeFail(mComment);
        }
    }
    
    @Override
    public JSONObject handleCache(Context context,int what, JSONObject from, JSONObject to, Object... params) {
        return null;
    }
    @Override
    public void updateProgress(Context context, int what, float percent, Object... params) {
        
    }
    public interface OnCommentChanged{
        public void beforeChange(Comment comment);
        public void changeFail(Comment comment);
    }
    
    
}
