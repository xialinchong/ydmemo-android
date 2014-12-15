package com.yidianhulian.ydmemo.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.CallApiTask;
import com.yidianhulian.framework.CallApiTask.CallApiListener;
import com.yidianhulian.ydmemo.CacheHelper;
import com.yidianhulian.ydmemo.MarkItemAdapter;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.model.Comment;
import com.yidianhulian.ydmemo.model.Option;

/**
 * 标记某条留言 传入参数：comment：被标记的留言对象；config：true为表示配置模式
 * 
 * @author leeboo
 * 
 */
public class MarkComment extends Activity implements CallApiListener {

    public static final String COMMENT = "comment";
    public static final String CONFIG = "CONFIG";
    private MarkItemAdapter mAdapter;
    private List<Map<String, ?>> mMenus = new ArrayList<Map<String, ?>>();
    private TypedArray mMenuIcons;
    private TypedArray mMenuLabels;
    private Comment mComment;
    private YDMemoApplication mApp;
    private ListView mList;
    private static final int SAVE_COMMENT_HANDLE = 0;
    private static final int SAVE_CHECKEDMENUS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list1);
        mApp = (YDMemoApplication) getApplication();

        mList = (ListView) findViewById(R.id.list);
        mComment = getIntent().getParcelableExtra(COMMENT);
        mMenuIcons = getResources().obtainTypedArray(R.array.mark_menu_icons);
        mMenuLabels = getResources().obtainTypedArray(R.array.mark_menu_labels);

        for (int i = 0; i < mMenuIcons.length(); i++) {
            Map<String, Object> item = new HashMap<String, Object>();

            int lableId = mMenuLabels.getResourceId(i, -1);
            String name = Util.getStringNameById(lableId);
            if (name != null)
                item.put("type", name);

            if (mMenuIcons.getResourceId(i, 0) != 0) {
                item.put("icon", mMenuIcons.getResourceId(i, -1));
                item.put("isSection", false);
            } else {
                item.put("isSection", true);
            }

            item.put("label", lableId);
            mMenus.add(item);
        }
        mMenuIcons.recycle();
        mMenuLabels.recycle();

        List<String> checkedMenus = null;
        if(getIntent().getBooleanExtra(CONFIG, false)){
            CacheHelper helper = new CacheHelper(mApp);
            Option option = helper.getSetting(mApp.loginUser().id());
            checkedMenus = option.marks();
        }
        
        mAdapter = new MarkItemAdapter(this, mMenus, getIntent()
                .getBooleanExtra(CONFIG, false), checkedMenus);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                if (getIntent().getBooleanExtra(CONFIG, false)) {
                    mAdapter.toggleChecked(position);
                } else {
                    Map<String, String> args = new HashMap<String, String>();
                    args.put("type",
                            ((Map) mAdapter.getItem(position)).get("type")
                                    .toString());
                    Util.showLoading(MarkComment.this,
                            getString(R.string.posting));
                    CallApiTask.doCallApi(SAVE_COMMENT_HANDLE, MarkComment.this,
                            MarkComment.this, args);
                }
            }
        });
        restoreActionBar();

    }

    @SuppressWarnings("unchecked")
    @Override
    public Api getApi(Context context, int what, Object... params) {
        Map<String, String> queryStr = (Map<String, String>) params[0];
        switch (what) {
        case SAVE_COMMENT_HANDLE:
            queryStr.put("uid", String.valueOf(mApp.loginUser().id()));
            queryStr.put("comment_id", String.valueOf(mComment.id()));
            queryStr.put("action", "add");
            return new Api("post", Util.URI_COMMENT_HANDLE, queryStr);
        /**
         * @desc 保存自定义快捷标记
         * @author xialinchong
         * @2014-15-02
         */
        case SAVE_CHECKEDMENUS:
            return new Api("post", Util.URI_SAVE_PROFILE + "?uid="
                    + mApp.loginUser().id(), queryStr);
        }
        return null;
    }

    @Override
    public boolean isCallApiSuccess(Context context, int what,
            JSONObject result, Object... params) {
        return Util.checkResult(result);
    }

    @Override
    public void apiNetworkException(Context context, int what, Exception e,
            Object... params) {

    }

    @Override
    public String getCacheKey(Context context, int what, Object... params) {
        return null;
    }

    @Override
    public void handleResult(Context context, int what, JSONObject result,
            boolean isDone, Object... params) {
        Util.hideLoading();
        if (isCallApiSuccess(context, what, result)) {
            Util.updateCacheAndUI(
                    this,
                    new Comment(Api.getJSONValue(result, "data",
                            JSONObject.class)), mApp.loginUser().id());
            if (what == SAVE_CHECKEDMENUS) {
                Util.showToast(this, "自定义快捷标记保存成功！");
            }
            finish();
        } else {
            Util.showToast(this, getString(R.string.data_post_failed));
        }
    }

    @Override
    public JSONObject handleCache(Context context, int what, JSONObject from,
            JSONObject to, Object... params) {
        return null;
    }

    @Override
    public void updateProgress(Context context, int what, float percent,
            Object... params) {

    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setIcon(R.drawable.logo);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        if (getIntent().getBooleanExtra(CONFIG, false)) {
            actionBar.setTitle(R.string.mark_setting);
        } else {
            actionBar.setTitle(R.string.comment_mark_as);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getIntent().getBooleanExtra(CONFIG, false)) {
            MenuInflater inflater = new MenuInflater(this);
            inflater.inflate(R.menu.ok, menu);
            return true;
        } else {
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.ok) {
            /**
             * @desc 将自定义快捷标记list转换成string
             * @author xialinchong
             * @2014-15-02
             */
            List<String> checkedMenus = mAdapter.getChecked();
            String checkedMenu = listToString(checkedMenus);
            if ( !checkedMenu.equals("")) {
                Map<String, String> args = new HashMap<String, String>();
                args.put("marks", checkedMenu);
                Util.showLoading(MarkComment.this,
                        getString(R.string.posting));
                CallApiTask.doCallApi(SAVE_CHECKEDMENUS, MarkComment.this,
                        MarkComment.this, args);
            }
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @author xialinchong
     * @Description:把list转换为一个用逗号分隔的字符串
     */
    private String listToString(List<?> list) {
        StringBuilder sb = new StringBuilder();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (i < list.size() - 1) {
                    sb.append(list.get(i) + ",");
                } else {
                    sb.append(list.get(i));
                }
            }
        }
        return sb.toString();
    }
}
