package com.softdream.pagelistloader;
/**
 * ClassName : IPageListener <br>
 * 功能描述： <br>
 * History <br>
 * Create User: An Zewei <br>
 * Create Date: 2013-12-31 下午1:29:19 <br>
 * Update User: <br>
 * Update Date:
 */
public interface IPageListener {

    public void onRefresh(PageListLoader loader);

    void onLoading(PageListLoader loader);
}
