package com.yidianhulian.ydmemo.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;

import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;

/**
 * 启动界面，第一次启动需要看到欢迎引导图
 * 
 * @author leeboo
 * 
 */
public class Welcome extends Activity {

    /** Viewpager对象 */
    private ViewPager viewPager;
    private ImageView imageView;
    /** 创建一个数组，用来存放每个页面要显示的View */
    private ArrayList<View> pageViews;
    /** 创建一个imageview类型的数组，用来表示导航小圆点 */
    private ImageView[] imageViews;
    /** 装显示图片的viewgroup */
    private ViewGroup viewPictures;
    /** 导航小圆点的viewgroup */
    private ViewGroup viewPoints;

    // private Button touch_enter;
    private YDMemoApplication mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getLayoutInflater();

        mApp = (YDMemoApplication) getApplication();
        
        pageViews = new ArrayList<View>();
        pageViews.add(inflater.inflate(R.layout.viewpager_start, null));
        pageViews.add(inflater.inflate(R.layout.viewpager_mid, null));
        pageViews.add(inflater.inflate(R.layout.viewpager_end, null));

        Button touchEnter = (Button) pageViews.get(pageViews.size() - 1)
                .findViewById(R.id.touch_enter);
        touchEnter.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mApp.setOption("hasLaunched", Util.getAppVersionName(mApp));
                Intent intent = new Intent();
                intent.setClass(Welcome.this, Login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                Welcome.this.startActivity(intent);
            }
            
        });

        // 小圆点数组，大小是图片的个数
        imageViews = new ImageView[pageViews.size()];
        // 从指定的XML文件中加载视图
        viewPictures = (ViewGroup) inflater.inflate(R.layout.welcome, null);

        viewPager = (ViewPager) viewPictures.findViewById(R.id.img_view_pager);
        viewPoints = (ViewGroup) viewPictures.findViewById(R.id.img_view_group);

        // 添加小圆点导航的图片
        for (int i = 0; i < pageViews.size(); i++) {
            imageView = new ImageView(Welcome.this);
            imageView.setLayoutParams(new LayoutParams(30, 30));
            imageView.setPadding(5, 0, 5, 0);
            // 吧小圆点放进数组中
            imageViews[i] = imageView;
            // 默认选中的是第一张图片，此时第一个小圆点是选中状态，其他不是
            if (i == 0) {
                imageViews[i].setImageDrawable(getResources().getDrawable(
                        R.drawable.img_indicator_focused));
            } else {
                imageViews[i].setImageDrawable(getResources().getDrawable(
                        R.drawable.img_indicator_unfocused));
            }
            // 将imageviews添加到小圆点视图组
            viewPoints.addView(imageViews[i]);
        }

        setContentView(viewPictures);

        viewPager.setAdapter(new NavigationPageAdapter());
        // 为viewpager添加监听，当view发生变化时的响应
        viewPager.setOnPageChangeListener(new NavigationPageChangeListener());
    }

    // 导航图片view的适配器，必须要实现的是下面四个方法
    class NavigationPageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return pageViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        // 初始化每个Item
        @Override
        public Object instantiateItem(View container, int position) {
            ((ViewPager) container).addView(pageViews.get(position));
            return pageViews.get(position);
        }

        // 销毁每个Item
        @Override
        public void destroyItem(View container, int position, Object object) {
            ((ViewPager) container).removeView(pageViews.get(position));
        }

    }

    // viewpager的监听器，主要是onPageSelected要实现
    class NavigationPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int position) {
            // 循环主要是控制导航中每个小圆点的状态
            for (int i = 0; i < imageViews.length; i++) {
                // 当前view下设置小圆点为选中状态
                imageViews[i].setImageDrawable(getResources().getDrawable(
                        R.drawable.img_indicator_focused));
                // 其余设置为飞选中状态
                if (position != i)
                    imageViews[i].setImageDrawable(getResources().getDrawable(
                            R.drawable.img_indicator_unfocused));
            }
        }

    }

}
