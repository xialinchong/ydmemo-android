package com.yidianhulian.ydmemo;


import com.yidianhulian.ydmemo.model.Model;

/**
 * 管理不同地方的数据的同步及刷新
 * @author leeboo
 *
 */
public interface Refreshable {
    /**
     * model有改变，更新ui，如果返回true表示数据已经被处理，如果返回false，将会弹出通知
     * @param model
     * @return TODO
     */
    boolean refresh(Model model);
}
