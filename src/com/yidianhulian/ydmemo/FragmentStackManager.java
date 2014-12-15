package com.yidianhulian.ydmemo;

import android.app.Fragment;


/**
 * mainactivity 的fragment管理
 * @author leeboo
 *
 */
public interface FragmentStackManager {
        void pushFragment(Fragment fragment, String backTag);
        boolean isTop(Fragment fragment, String backTag);
        boolean isOpen();
}
