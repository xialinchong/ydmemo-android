package com.yidianhulian.ydmemo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * 标记列表适配器
 * 
 * @author leeboo
 *
 */
public class MarkItemAdapter extends SimpleAdapter {

    private static final int SECTION = 0;
    private static final int MENU = 1;
    private List<Map<String, ?>> mMenus = new ArrayList<Map<String, ?>>();
    private List<String> mCheckedMenus  = new ArrayList<String>();
    private Context mContext;
    private boolean mCheckable;
    
    public MarkItemAdapter(Context context, List<Map<String, ?>> menus, boolean checkable, List<String> checkedMenus) {
        super(context, menus, R.layout.cell_comment_context_menu, new String[] {
                "icon", "label" }, new int[] { R.id.context_menu_icon,
                R.id.context_menu_label });
        mMenus = menus;
        mContext = context;
        mCheckable = checkable;
        if(checkedMenus!=null)mCheckedMenus = checkedMenus;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Map<String, Object> data = (Map<String, Object>) mMenus.get(position);
        Holder holder;
        if (convertView==null) {
            holder = new Holder();
            if(getItemViewType(position)==MENU){
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.cell_comment_context_menu, parent, false);
                holder.icon = (ImageView) convertView
                        .findViewById(R.id.context_menu_icon);
                holder.checkbox = (ImageView) convertView
                        .findViewById(R.id.checkbox);
                holder.label = (TextView) convertView
                        .findViewById(R.id.context_menu_label);
            } else {
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.cell_comment_context_section, parent, false);
                holder.label = (TextView) convertView
                        .findViewById(R.id.context_menu_section);
            }
            convertView.setTag(holder);
        }else{
            holder = (Holder)convertView.getTag();
        }
        
        if(getItemViewType(position)==MENU){
            
            if(mCheckedMenus.indexOf(data.get("type")) != -1){
                holder.checkbox.setImageResource(R.drawable.checked);
            }else{
                holder.checkbox.setImageResource(R.drawable.unchecked);
            }

            holder.icon.setImageResource((Integer) data.get("icon"));
            holder.checkbox.setVisibility(mCheckable ? View.VISIBLE : View.GONE);
        }
        holder.label.setText((Integer) data.get("label"));
        return convertView;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getItemViewType(int position) {
        Map<String, Object> data = (Map<String, Object>) mMenus.get(position);
        if ((Boolean) data.get("isSection"))
            return SECTION;// section
        return MENU;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != SECTION;
    }
    
    public void toggleChecked(int position){
        String type = (String)mMenus.get(position).get("type");
        if(mCheckedMenus.indexOf(type) == -1){
            mCheckedMenus.add(type);
        }else{
            mCheckedMenus.remove(type);
        }
        notifyDataSetChanged();
    }

    public List<String> getChecked(){
        return mCheckedMenus;
    }
    
    class Holder{
        ImageView icon;
        ImageView checkbox;
        TextView label;
    }
}