package com.yidianhulian.ydmemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Refreshable;
import com.yidianhulian.ydmemo.fragment.MemoDetail;
import com.yidianhulian.ydmemo.fragment.MemoReminders.RefreshMemoInterface;
import com.yidianhulian.ydmemo.model.Model;
import com.yidianhulian.ydmemo.model.Notify;

public class MemoDetailActivity extends Activity implements RefreshMemoInterface,Refreshable {

    private MemoDetail mMemoDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.memodetail);

        Intent data = getIntent();
        Notify notify = data.getParcelableExtra("result_notification");

        mMemoDetail = (MemoDetail) getFragmentManager().findFragmentById(
                R.id.memodetail);
        mMemoDetail.setUp(null);
        mMemoDetail.openMemo(notify.memoId(), notify.openMemoType());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void reloadMemo() {
        mMemoDetail.reloadData();
    }

    @Override
    public boolean refresh(Model model) {
        return mMemoDetail.refresh(model);
    }
}
