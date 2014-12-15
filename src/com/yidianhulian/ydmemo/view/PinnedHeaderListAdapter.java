package com.yidianhulian.ydmemo.view;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

import com.yidianhulian.ydmemo.model.User;
import com.yidianhulian.ydmemo.view.PinnedHeaderListView.PinnedHeaderAdapter;

public abstract class PinnedHeaderListAdapter extends BaseAdapter implements 
    SectionIndexer,PinnedHeaderAdapter  {
    protected Context mContext;
    protected YDGroupListView mListView;
    
    protected int mLocationPosition = -1;
    protected int TYPE_NORMAL = 0;
    protected int TYPE_SECTION = 1;


    public PinnedHeaderListAdapter(Context context, YDGroupListView listView, List<Object> contacts) {
        this.mContext = context;
        this.mListView = listView;
    }


    @Override
    public int getCount() {
        return mListView.getDatas().size();
    }

    @Override
    public Object getItem(int position) {
        return mListView.getDatas().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if(getItem(position) instanceof User){
            return TYPE_NORMAL;
        }else{
            return TYPE_SECTION;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(getItemViewType(position) == TYPE_SECTION){
            return getSectionView(position, convertView, parent);
        }
        return getNormalView(position, convertView, parent);
        
    }

    protected abstract View getSectionView(int position, View convertView, ViewGroup parent);

    protected abstract View getNormalView(int position, View convertView, ViewGroup parent);


    @Override
    public Object[] getSections() {
        return mListView.getSections().toArray();
    }

    @Override
    public int getPositionForSection(int section) {
        if (section < 0 || section >= mListView.getSectionIndexs().size()) {
            return -1;
        }
        return mListView.getSectionIndexs().get(section);
    }
    
    @Override
    public int getSectionForPosition(int position) {
        if (position < 0 || position >= getCount()) {
            return -1;
        }
        int index = Arrays.binarySearch(mListView.getSectionIndexs().toArray(), position);
        return index >= 0 ? index : -index - 2;
    }




    @Override
    public int getPinnedHeaderState(int position) {
        int realPosition = position;
        if (realPosition < 0
                || (mLocationPosition != -1 && mLocationPosition == realPosition)) {
            return PINNED_HEADER_GONE;
        }
        mLocationPosition = -1;
        int section = getSectionForPosition(realPosition);
        int nextSectionPosition = getPositionForSection(section + 1);
        if (nextSectionPosition != -1
                && realPosition == nextSectionPosition - 1) {
            return PINNED_HEADER_PUSHED_UP;
        }
        return PINNED_HEADER_VISIBLE;
    }


    @Override
    public abstract void configurePinnedHeader(View header, int position, int alpha);
}
