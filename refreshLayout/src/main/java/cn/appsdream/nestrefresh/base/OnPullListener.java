package cn.appsdream.nestrefresh.base;

/**
 * ClassName : OnPullListener
 * <br>功能描述：分页加载的回调，包括下拉刷新
 * <br>History
 * <br>Create User: An Zewei
 * <br>Create Date: 2013-12-31 下午3:13:30
 * <br>Update User:
 * <br>Update Date:
 */
public interface OnPullListener {
    public void onRefresh(AbsRefreshLayout listLoader);

    /**
     * 当加载更多时回调
     *
     */
    void onLoading(AbsRefreshLayout listLoader);
}
