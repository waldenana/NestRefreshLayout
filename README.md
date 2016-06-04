# NestRefreshLayout
#使用方法

##1,配置gradle

``` groovy
compile 'cn.appsdream.nestrefresh:refreshLayout:0.1'
``` 
##2.设置属性
``` java
NestRefreshLayout loader = new NestRefreshLayout(mView);
loader.setRefreshEnable(true)//下拉刷新
loader.setLoadMoreEnable(true)//底部加载更多
``` 
##3.更新完成

在加载完成后你需要调用

``` java
loader.onLoadFinished();//完成加载
``` 
``` java
loader.onLoadAll();//没有更多了
``` 

#自定义下拉样式
  定义style 
  
```xml
  <style name="my_loader_style">
        <item name="headerNestLayout">@layout/下拉刷新的layout</item>
        <item name="footerNestLayout">@layout/加载更多的layout</item>
  </style>
``` 
  在程序的theme里增加
  
```xml
  <item name"nestRefreshLayoutStyle">@style/my_loader_style</item>
``` 
  
#需要注意
  自定义的layout需要实现LoaderDecor接口，里面有两个方法
  setState 有3个状态
  STATE_NORMAL初始
  STATE_READY 触发
  STATE_REFRESHING 开始刷新或者加载更多
  和
  scrollRate，这里会返回移动的距离，你可以用这个值进行自定义动画
  更多详情，可以参见NestFooter和NestHeader
