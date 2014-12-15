package com.yidianhulian.ydmemo.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Refreshable;
import com.yidianhulian.ydmemo.Util;
import com.yidianhulian.ydmemo.YDMemoApplication;
import com.yidianhulian.ydmemo.model.Model;
/**
 * 关于易点备忘录
 * @author leeboo
 *
 */
public class About extends Activity implements Refreshable{

    private YDMemoApplication mApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        mApp = (YDMemoApplication) getApplication();
        TextView version = (TextView) findViewById(R.id.version);
        version.setText(String.format("Version:%s", Util.getAppVersionName(mApp)));
        restoreActionbar();
    }

    private void restoreActionbar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setIcon(R.drawable.logo);
        actionBar.setTitle(R.string.about_ydmemo);
        
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean refresh(Model model) {
    	return false;
    }
}
