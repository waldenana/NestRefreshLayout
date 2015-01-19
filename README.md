# PageListLoader
1，使用方法
PageListLoader loader = new PageListLoader(mView);
loader.setRefreshEnable(true)//下拉刷新
loader.setLoadMoreEnable(true)//底部加载更多

在加载完成后你需要调用
loader.stopRefresh();//完成刷新
或者
loader.stopLoading();//完成加载

2，自定义下拉样式
  定义style 
  <style name="my_loader_style">
        <item name="refreshLayout">@layout/下拉刷新的layout</item>
        <item name="loadMoreLayout">@layout/加载更多的layout</item>
  </style>
  在程序的theme里增加
  <item name"pageListLoaderStyle">@style/my_loader_style</item>
  
  需要注意
  自定义的layout需要实现LoaderDecor接口，里面有两个方法
  setState 有3个状态
  STATE_NORMAL初始
  STATE_READY 触发
  STATE_REFRESHING 开始刷新或者加载更多
  和
  scrollRate，这里会返回移动的距离，你可以用这个值进行自定义动画
  更多详情，可以参见PullFooter和PullHeader