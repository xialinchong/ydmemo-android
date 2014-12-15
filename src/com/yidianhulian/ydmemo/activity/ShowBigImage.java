package com.yidianhulian.ydmemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.yidianhulian.ydmemo.CommentContextHandler;
import com.yidianhulian.ydmemo.CommentContextHandler.OnCommentChanged;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.model.Comment;
import com.yidianhulian.ydmemo.model.Memo;

/**
 * @显示留言图片支持缩放
 * @author xialinchong
 * @2014-11-28
 */
public class ShowBigImage extends Activity {

    private WebView mShowImage;
    double startx = 0;
    double starty = 0;
    private ImageButton mMark;
    private ImageButton mBack;
    private ProgressBar mProgressbar;
    private Memo mMemo;
    private Comment mComment;
    public static final String ARG_MEMO = "memo";
    public static final String ARG_COMMENT = "comment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_big_image);
        
        mShowImage = (WebView) findViewById(R.id.show_image);
        mBack = (ImageButton) findViewById(R.id.back);
        mMark = (ImageButton) findViewById(R.id.mark);
        mProgressbar = (ProgressBar) findViewById(R.id.progressBar);
        Intent intent = getIntent();

        mMemo = getIntent().getParcelableExtra(ARG_MEMO);
        mComment = getIntent().getParcelableExtra(ARG_COMMENT);
        String url = "<!doctype html> <html lang=\"en\"> <head> <meta charset=\"UTF-8\"> <title></title><style type=\"text/css\"> html,body{width:100%;height:100%;margin:0;padding:0;background-color:black;} *{ -webkit-tap-highlight-color: rgba(0, 0, 0, 0);}#box{ width:100%;height:100%; display:table; text-align:center; background-color:black;} body{-webkit-user-select: none;user-select: none;-khtml-user-select: none;}#box span{ display:table-cell; vertical-align:middle;} #box img{  width:100%;} </style> </head> <body> <div id=\"box\"><span><img src=\"img_url\" alt=\"\"></span></div></body> </html>".replace("img_url", mComment.oriFilePath());
//        String url = "<!doctype html><html>"
//                + "<meta name='viewport' content='minimum-scale=1;"
//                + "maximum-scale=5;initial-scale=1;user-scalable=yes;'>"
//                + "<body style='background:#000000'>"
//                + "<p style='text-align:center;margin-top:50%;'>"
//                + "<img src='" + mComment.oriFilePath() + "' "
//                + " style='vertical-align:middle;'/></p></body></html>";

        mShowImage.getSettings().setSupportZoom(true);
        mShowImage.getSettings().setBuiltInZoomControls(true);
        mShowImage.getSettings().setDisplayZoomControls(false);
        mShowImage.loadDataWithBaseURL("", url, "text/html", "UTF-8", "");
        mShowImage.setWebChromeClient(new WebChromeClient(){

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    mProgressbar.setVisibility(View.GONE);
                } else {
                    if (mProgressbar.getVisibility() == View.GONE)
                        mProgressbar.setVisibility(View.VISIBLE);
                    mProgressbar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
            
        });
        
        mBack.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        mMark.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if(mMemo==null)return;
               CommentContextHandler holder = new CommentContextHandler(ShowBigImage.this, mMemo, new OnCommentChanged(){
                @Override
                public void beforeChange(Comment comment) {
                    
                }

                @Override
                public void changeFail(Comment comment) {
                    
                }});
               holder.showCommentContextMenu(mComment);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
