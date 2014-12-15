package com.yidianhulian.ydmemo.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.Toast;

import com.yidianhulian.framework.Api;
import com.yidianhulian.framework.ImageLoader;
import com.yidianhulian.framework.ImageLoader.ImageLoaded;
import com.yidianhulian.ydmemo.CacheHelper;
import com.yidianhulian.ydmemo.FragmentStackManager;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Refreshable;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.fragment.MemoDetail;
import com.yidianhulian.ydmemo.fragment.MemoReminders.RefreshMemoInterface;
import com.yidianhulian.ydmemo.fragment.Profile;
import com.yidianhulian.ydmemo.model.Model;
import com.yidianhulian.ydmemo.model.Option;

/**
 * 主界面
 * 
 * @author leeboo
 * 
 */
public class MainActivity extends Activity implements FragmentStackManager,Refreshable, RefreshMemoInterface {

    private Profile mProfile;
    private DrawerLayout mDrawerLayout;
    private List<Fragment> openedFragments = new ArrayList<Fragment>();
    private long mExitTime;
    private boolean mIsVisiable;
    private MemoDetail mMemoDetail;
    private ActionBarDrawerToggle mDrawerToggle;
    private YDMemoApplication mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        showBackground();

        // init mDrawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);

        mProfile = (Profile) getFragmentManager().findFragmentById(R.id.profile);
        mProfile.setUp((DrawerLayout) findViewById(R.id.main));
        LayoutParams lp = mProfile.getView().getLayoutParams();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        lp.width = (int)(dm.widthPixels - 50);
        mProfile.getView().setLayoutParams(lp);
        
        mMemoDetail = (MemoDetail) getFragmentManager().findFragmentById(R.id.detail);
        mMemoDetail.setUp((DrawerLayout) findViewById(R.id.main));
        LayoutParams lp2 = mMemoDetail.getView().getLayoutParams();
        lp2.width = (int)(dm.widthPixels - 50);
        mMemoDetail.getView().setLayoutParams(lp2);
        
        getFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                FragmentManager fm = getFragmentManager();
                Fragment topFragment = (Fragment)fm.findFragmentById(R.id.container);
                if(topFragment !=null ){
                    fm.beginTransaction().show(topFragment).commit();
                }
            }
        });
        
        updateNormalDrawerToggle();
    }

    public void updateNormalDrawerToggle() {
        mDrawerToggle = new MyActionBarDrawerToggle(this, mDrawerLayout,R.drawable.ic_drawer, 
                R.string.navigation_drawer_open,R.string.navigation_drawer_close
        );
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }
    
    public void updateMessageDrawerToggle() {
        mDrawerToggle = new MyActionBarDrawerToggle(this, mDrawerLayout,R.drawable.msg_drawer, 
                R.string.navigation_drawer_open,R.string.navigation_drawer_close
        );
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //处理打开通知栏消息事件
        processOpenMessage();
        mIsVisiable = true;
    }
    @Override
    protected void onPause() {
        super.onPause();
        mIsVisiable = false;
    }

    public Profile getProfile() {
        return mProfile;
    }
    
    public MemoDetail getMemoDetail() {
        return mMemoDetail;
    }

    @Override
    public void pushFragment(Fragment fragment, String tagName) {
        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            Fragment exist = fragmentManager.findFragmentByTag(tagName);
            
            if(exist==null){
                //压栈前把栈中的设置为不可见，避免显示重叠及事件穿透 leeboo
                Fragment topFragment = (Fragment)getFragmentManager().findFragmentById(R.id.container);
                if(topFragment !=null ){
                    ft.hide((Fragment)topFragment);
                }
                //这里用add不用replace，是为了保持之前所有打开的fragment的状态 leeboo
                ft.add(R.id.container, fragment, tagName).addToBackStack(tagName).commit();
            }else{
                ft.show(exist).commit();
                fragmentManager.popBackStack(tagName, 0);
            }

            if(openedFragments.indexOf(fragment) == -1) openedFragments.add(fragment);
        }
    }
    
    public boolean isTop(Fragment fragment, String tagName){
        return getFragmentManager().findFragmentById(R.id.container) == fragment;
    }

    public boolean isOpen() {
        return mProfile.isDrawerOpen() || mMemoDetail.isOpen();
    }
    
    @Override
    public void onBackPressed() {
       
        if (getFragmentManager().getBackStackEntryCount() <= 1) {
            if ((System.currentTimeMillis() - mExitTime) > 3000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return;
        }
        super.onBackPressed();
    }


    /**
     * 处理打开通知栏消息的事件
     * @author HuJinhao
     * @since 2014-10-28
     */
    private void processOpenMessage() {
    	YDMemoApplication app = (YDMemoApplication)getApplication();
    	JSONObject customContent = app.getMessageCustomContent();
		if (customContent == null) return ;
		
		if (Util.ACTION_OPEN_MEMO.equals(Api.getStringValue(customContent, "action"))) { //打开备忘详情
			Long memoid = Long.valueOf(Api.getJSONValue(customContent, "mid").toString());
			if(memoid > 0){
			    String type;
			    switch(Api.getIntegerValue(customContent, "type")){
			    case Util.M_NEW_COMMENT:
			        type = MemoDetail.MEMO_DETAIL_TYPE_COMMENTS;break;
			    case Util.M_NEW_REMIND:
			        type = MemoDetail.MEMO_DETAIL_TYPE_TRACE;break;
			    default:
			        type = MemoDetail.MEMO_DETAIL_TYPE_CONTENT;
			    }
			    getMemoDetail().openMemo(memoid, type);
			}
            
            app.clearMessageCustomContent();
		}
    }

	@Override
	public boolean refresh(Model model) {
	    if(model instanceof Option){
	        showBackground();
	    }
	    
		boolean consumed = false;
		for (Fragment fragment : openedFragments) {
		    if( ! fragment.isAdded() )continue;
		    if(fragment instanceof Refreshable) consumed |= ((Refreshable)fragment).refresh(model);
        }
		consumed |= mMemoDetail.refresh(model);
        consumed |= mProfile.refresh(model);
        return mIsVisiable && consumed;
	}
	
	private void showBackground() {
	    ImageLoader imageLoader = new ImageLoader(this);
        CacheHelper helper = new CacheHelper(this);
        mApp = (YDMemoApplication)getApplication();
        Option misc = helper.getSetting(mApp.loginUser().id());
        if( misc != null && ! Util.isEmpty(misc.background()) ){
            imageLoader.loadImage(null, misc.background(), new ImageLoaded(){
                @Override
                public void imageLoaded(ImageView imageView, Drawable imageDrawable) {
                  getWindow().setBackgroundDrawable(imageDrawable);
                }
            });
        }
        
    }

    /**
	 * 关闭侧边栏
	 * @author HuJinhao
	 * @since 2014-11-26
	 */
	public void closeDrawers() {
		if (mDrawerLayout != null) mDrawerLayout.closeDrawers();
	}
		
	/**
	 * 启用或禁用ActionBar左上角图标
	 * @param enabled 
	 * @author HuJinhao
	 * @since 2014-11-26
	 */
	public void enabledDrawerToggleIndicatorIcon(boolean enabled) {
		if (mDrawerToggle != null) mDrawerToggle.setDrawerIndicatorEnabled(enabled);
	}
	
	class MyActionBarDrawerToggle extends ActionBarDrawerToggle{
	    private DrawerLayout mDrawerLayout;
        public MyActionBarDrawerToggle(Activity activity,
                DrawerLayout drawerLayout, int drawerImageRes,
                int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, drawerImageRes, openDrawerContentDescRes,
                    closeDrawerContentDescRes);
            mDrawerLayout = drawerLayout;
        }
        
        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);
            this.setDrawerIndicatorEnabled(true);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mMemoDetail.getView());
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, mProfile.getView());
            invalidateOptionsMenu();
            if(drawerView==mMemoDetail.getView()){
                mMemoDetail.closeMemo();
            }
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            //lock后back键盘将不起作用
            if(drawerView==mProfile.getView()){
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mMemoDetail.getView());
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, mProfile.getView());
                this.setDrawerIndicatorEnabled(true);
            }else{
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, mMemoDetail.getView());
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mProfile.getView());
                this.setDrawerIndicatorEnabled(false);//leeboo 禁用切换按钮
            }
            invalidateOptionsMenu();
        }
	    
	}

    @Override
    public void reloadMemo() {
        getMemoDetail().reloadData();
    }
}
