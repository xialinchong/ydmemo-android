package com.yidianhulian.ydmemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.yidianhulian.ydmemo.widget.SlideDeleteListView;

/**
 * 上下拉刷新且列表item能够滑动删除
 */

public class SlideDelete extends PullToRefreshListView {
	
	public SlideDelete(Context context) {
		super(context);
	}

	public SlideDelete(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SlideDelete(Context context, Mode mode) {
		super(context, mode);
	}

	public SlideDelete(Context context, Mode mode, AnimationStyle style) {
		super(context, mode, style);
	}
	
	protected ListView createListView(Context context, AttributeSet attrs) {
		return new SlideDeleteListView(context, attrs);
	}
}
