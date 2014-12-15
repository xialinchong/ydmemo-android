package com.yidianhulian.ydmemo.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;

import com.yidianhulian.ydmemo.view.SideBar.OnItemClickListener;

public class YDGroupListView {
    private PinnedHeaderListView mListView;
    private SideBar mSideBar;
    private YDGroupListViewDelegate mDelegate;
    private ViewGroup mHeader;
    private Context mContext;
    private List<Object> mRawDatas = new ArrayList<Object>();
    private List<Object> mDatas = new ArrayList<Object>();
    /**
     * 分组字母
     */
    private List<String> mSections = new ArrayList<String>();
    /**
     * section在mContacts中的位置
     */
    private List<Integer> mSectionIndexs = new ArrayList<Integer>();
    /**
     * 分组字母在mContacts中的位置
     */
    private Map<String, Integer> mKeyIndexs = new HashMap<String, Integer>();
    
    public YDGroupListView(Context context, List<Object> datas, 
            PinnedHeaderListView mListView, 
            SideBar mSideBar,ViewGroup groupHeader) {
        super();
        this.mListView = mListView;
        this.mSideBar = mSideBar;
        mContext = context;
        mRawDatas = datas;
        mHeader = groupHeader;
    }
    
    public PinnedHeaderListView getListView(){
        return mListView;
    }
    
    public List<String> getSections() {
        return mSections;
    }

    public void setSections(List<String> sections) {
        mSections.clear();
        if(sections !=null)this.mSections.addAll(sections);
    }

    public List<Integer> getSectionIndexs() {
        return mSectionIndexs;
    }

    public void setSectionIndexs(List<Integer> sectionIndexs) {
        mSectionIndexs.clear();
        if(sectionIndexs !=null )this.mSectionIndexs.addAll(sectionIndexs);
    }

    public Map<String, Integer> getKeyIndexs() {
        return mKeyIndexs;
    }

    public void setKeyIndexs(Map<String, Integer> keyIndexs) {
        this.mKeyIndexs.clear();
        if(keyIndexs!=null)this.mKeyIndexs.putAll(keyIndexs);
    }

    public List<Object> getDatas() {
        return mDatas;
    }

    public void setDatas(List<Object> datas) {
        this.mDatas.clear();
        if(datas !=null)this.mDatas.addAll(datas);
    }

    public SideBar getSideBar(){
        return mSideBar;
    }
    
    public YDGroupListViewDelegate getDelegate(){
        return mDelegate;
    }
    
    public void setDelegate(YDGroupListViewDelegate delegate){
        mDelegate = delegate;
        mListView.setOnScrollListener(mDelegate.listScrollListener());
        mListView.setAdapter(mDelegate.getAdapter());
        mListView.setPinnedHeaderView(mHeader);
        mListView.setOnItemClickListener(mDelegate.listItemClickListener());
        mSideBar.setOnItemClickListener(mDelegate.sideBarClickListener());
        
    }
    
    public void refresh(){
        mDelegate.formatData(this, mRawDatas);
        ((PinnedHeaderListAdapter)mListView.getAdapter()).notifyDataSetChanged();
        mSideBar.setSections(getDelegate().sideBarSections());
        mSideBar.setAvailablelSections(mSections.toArray(new String[]{}));
        mSideBar.invalidate();
    }
    
    public interface YDGroupListViewDelegate{
        /**
         * 右边sidebar上的内容
         * @return
         */
        String[] sideBarSections();
       
        
        /**
         * 格式话数据，并通过setSections(List<String> mSections), setSectionIndexs(List<Integer> mSectionIndexs) , setKeyIndexs(Map<String, Integer> mKeyIndexs)
         * 设置数据给listView
         * @param rawDatas
         */
        void formatData(YDGroupListView listView, List<Object> rawDatas);
        
        PinnedHeaderListAdapter getAdapter();
        OnScrollListener listScrollListener();
        OnItemClickListener sideBarClickListener();
        android.widget.AdapterView.OnItemClickListener listItemClickListener();
    }
}
