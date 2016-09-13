# NestRefreshLayout

# Demo Apk

<a href="https://github.com/anzewei/NestRefreshLayout/blob/master/ext/sample-debug.apk?raw=true">DOWNLOAD</a>

#Usage

##Step 1
- Add these lines to your build.gradle

``` groovy
compile 'cn.appsdream.nestrefresh:refreshLayout:0.2'
``` 
##Step 2

``` java
NestRefreshLayout loader = new NestRefreshLayout(mView);
loader.setRefreshEnable(true)
loader.setLoadMoreEnable(true)
``` 
OR
``` xml
<cn.appsdream.nestrefresh.normalstyle.NestRefreshLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    xmlns:android="http://schemas.android.com/apk/res/android">

    <android.support.v7.widget.RecyclerView
        android:id="@+id/can_content_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@android:color/holo_blue_light"
        android:cacheColorHint="@android:color/transparent"
        android:clipToPadding="false"
        android:scrollingCache="false"/>
</cn.appsdream.nestrefresh.normalstyle.NestRefreshLayout>
``` 
##Step 2

-after get data

``` java
loader.onLoadFinished();
``` 
``` java
loader.onLoadAll();
``` 

#Customer Style
## Theme
```xml
  <style name="my_loader_style">
        <item name="headerNestLayout">@layout/refresh_layout</item>
        <item name="footerNestLayout">@layout/more_layout</item>
  </style>
``` 
  Add to theme
  
```xml
  <item name"nestRefreshLayoutStyle">@style/my_loader_style</item>
```
OR 
##Add attr

```xml
<cn.appsdream.nestrefresh.normalstyle.NestRefreshLayout
    app:headerNestLayout="@layout/layout_header"
    xmlns:app="http://schemas.android.com/apk/res-auto">
</cn.appsdream.nestrefresh.normalstyle.NestRefreshLayout>
``` 

#Notice
  You can customer Header and Footer only implement LoaderDecor
  setState with 3 status
  STATE_NORMAL     
  STATE_READY       
  STATE_REFRESHING 
  
  and scroll will call function
  scrollRate
  More info NestFooter NestHeader
