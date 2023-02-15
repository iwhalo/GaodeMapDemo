package com.example.gaodemapdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import android.icu.text.CaseMap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItemV2;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.interfaces.IGeocodeSearch;
import com.amap.api.services.poisearch.PoiResultV2;
import com.amap.api.services.poisearch.PoiSearchV2;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements
        AMapLocationListener, LocationSource, PoiSearchV2.OnPoiSearchListener,
        AMap.OnMapClickListener, AMap.OnMapLongClickListener,
        GeocodeSearch.OnGeocodeSearchListener, EditText.OnKeyListener {
    //    请求权限码
    private static final int REQUEST_PERMISSIONS = 9527;

    //    声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //    声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    //    内容
//    private TextView tvContent;
//    activity_main.xml中的TextView删除后，这里相应要改变
    private MapView mapView;

    //    地图控制器
    private AMap aMap = null;
    //    位置更改监听
    private LocationSource.OnLocationChangedListener mListener;

    //    定位样式
    private MyLocationStyle myLocationStyle = new MyLocationStyle();



    //    定义一个UiSettings对象
    private UiSettings mUiSettings;

    //    POI查询对象
    private PoiSearchV2.Query query;
    //    POI搜索对象
    private PoiSearchV2 poiSearchV2;
    //    城市码
    private String cityCode = null;
    //    浮动按钮
    private FloatingActionButton fabPOI;

    //    地理编码搜索
    private GeocodeSearch geocodeSearch;

    //    解析成功标识码
    private static final int PARSE_SUCCESS_CODE = 1000;

    //    输入框
    private EditText etAddress;

//    城市
    private String city;

//    浮动按钮，清空地图标点
    private FloatingActionButton fabClearMaker;

//    标点列表
    private List<Marker> markerList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        activity_main.xml中的TextView删除后，这里相应要改变
//        绑定控件id
//        tvContent = findViewById(R.id.tv_content);
        mapView = findViewById(R.id.map_view);
//        在activity执行onCreate时执行MapView.onCreate(savedInstance)
        mapView.onCreate(savedInstanceState);

        fabPOI = findViewById(R.id.fab_poi);

        etAddress = findViewById(R.id.et_address);
//        键盘按键监听
        etAddress.setOnKeyListener(this);

//        初始化定位
        initLocation();

//        初始化地图
        initMap(savedInstanceState);

//        检查Android版本
        checkingAndroidVersion();
    }


    /*
     * @Desc : 获取权限
     * @Author : xiaoyun
     * @Created_Time : 2023/2/9 21:31
     * @Project_Name : MainActivity.java
     * @PACKAGE_NAME : com.example.gaodemapdemo
     * @Params :
     */
    private void checkingAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            android 6.0及以上，先获取权限再定位
            requestPermission();
        } else {
//            android 6.0以下直接定位
            mLocationClient.startLocation();
        }
    }

    /*
     * @Desc : 动态请求权限
     * @Author : xiaoyun
     * @Created_Time : 2023/2/9 21:36
     * @Project_Name : MainActivity.java
     * @PACKAGE_NAME : com.example.gaodemapdemo
     * @Params :
     */
    @AfterPermissionGranted(REQUEST_PERMISSIONS)
    private void requestPermission() {
        String[] permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        if (EasyPermissions.hasPermissions(this, permissions)) {
//            true  有权限，开始定位
            showMsg("已获得权限，可以定位啦！");

//            启动定位
            mLocationClient.startLocation();
        } else {
//            false 无权限
            EasyPermissions.requestPermissions(this, "需要权限", REQUEST_PERMISSIONS, permissions);
        }
    }

    /*
     * @Desc : Toast提示
     * @Author : xiaoyun
     * @Created_Time : 2023/2/13 17:40
     * @Project_Name : MainActivity.java
     * @PACKAGE_NAME : com.example.gaodemapdemo
     * @Params : msg-提示内容
     */
    private void showMsg(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        设置权限请求结果
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    /*
     * @Desc : 初始化定位
     * @Author : xiaoyun
     * @Created_Time : 2023/2/13 17:47
     * @Project_Name : MainActivity.java
     * @PACKAGE_NAME : com.example.gaodemapdemo
     * @Params :
     */
    private void initLocation() {
//        初始化定位
        try {
            mLocationClient = new AMapLocationClient(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mLocationClient != null) {
//            设置定位回调监听
            mLocationClient.setLocationListener(this);
//            初始化AMapLocationClientOption对象
            mLocationOption = new AMapLocationClientOption();
//            设置定位模式为AMapLocationMode.Hight_Accuracy,高精度模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//            获取最近3s内精度最高的一次定位结果
//            设置setOnceLocationLatest(boolean b)接口为True启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
            mLocationOption.setOnceLocationLatest(true);
//            设置是否返回地址信息(默认返回地址信息)
            mLocationOption.setNeedAddress(true);
//            设置定位请求超时时间，单位是毫秒，默认3000毫秒，建议超时时间不要低于8000毫秒
            mLocationOption.setHttpTimeOut(20000);
//            关闭缓存机制，高精度定位会产生缓存
            mLocationOption.setLocationCacheEnable(false);
//            给定位客户端对象设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
        }
    }

    /*
     * @Desc : 接收异步返回的定位结果
     * @Author : xiaoyun
     * @Created_Time : 2023/2/13 19:36
     * @Project_Name : MainActivity.java
     * @PACKAGE_NAME : com.example.gaodemapdemo
     * @Params : aMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
//                地址
                String address = aMapLocation.getAddress();
//                城市赋值
                city = aMapLocation.getCity();
//                获取纬度
                double latitude = aMapLocation.getLatitude();
//                获取经度
                double longitude = aMapLocation.getLongitude();
//                获取楼层
                String floor = aMapLocation.getFloor();
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("纬度：" + latitude + "\n");
                stringBuffer.append("经度：" + longitude + "\n");
//                stringBuffer.append("楼层：" + floor + "\n");
                stringBuffer.append("地址：" + address + "\n");

                Log.d("MainActivity", stringBuffer.toString());
                showMsg(address);

//                tvContent.setText(address == null ? "无地址" : address);
//                tvContent.setText(stringBuffer.toString());

//                停止定位后，本地定位服务并不会被销毁
                mLocationClient.stopLocation();

//                显示地图定位结果
                if (mListener != null) {
//                    显示系统图标
                    mListener.onLocationChanged(aMapLocation);
                }

//                显示浮动按钮
                fabPOI.show();
//                赋值
                cityCode = aMapLocation.getCityCode();
            } else {
//                定位失败时，可通过ErrorCode(错误码)信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "Location Erroe, ErrorCode: "
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }


    /*
     * @Desc : 初始化地图
     * @Author : xiaoyun
     * @Created_Time : 2023/2/14 10:31
     * @Project_Name : MainActivity.java
     * @PACKAGE_NAME : com.example.gaodemapdemo
     * @Params : savedInstanceState
     */
    private void initMap(Bundle savedInstanceState) {
        mapView = findViewById(R.id.map_view);
//        在activity执行onCreate时执行MapView.onCreate(savedInstanceState)，创建地图
        mapView.onCreate(savedInstanceState);
//        初始化地图控制器对象
        aMap = mapView.getMap();

//        设置最小缩放等级为16，缩放级别范围为[3,20]
        aMap.setMinZoomLevel(12);

//        开启室内定位
        aMap.showIndoorMap(true);

        //    实例化UiSettings类对象
        mUiSettings = aMap.getUiSettings();
//        隐藏缩放按钮
        mUiSettings.setZoomControlsEnabled(false);

//        显示比例尺，默认不显示
        mUiSettings.setScaleControlsEnabled(true);

//        自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.gps_point));
//        自定义精度范围的圆形边框颜色，都为0则透明
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
//        自定义精度范围的圆形边框宽度  0  无宽度
        myLocationStyle.strokeWidth(0);
//        设置圆形的填充颜色，都为0则透明
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));

//        设置定位蓝点的Style
        aMap.setMyLocationStyle(myLocationStyle);

//        设置定位监听
        aMap.setLocationSource(this);
//        设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);

//        设置地图点击事件
        aMap.setOnMapClickListener(this);
//        设置地图长按事件
        aMap.setOnMapLongClickListener(this);

//        构造GeoCodeSearch对象
        try {
            geocodeSearch = new GeocodeSearch(this);
        } catch (AMapException e) {
            e.printStackTrace();
        }
//        设置监听
        geocodeSearch.setOnGeocodeSearchListener(this);
    }


    /*
     * @Desc : 在页面销毁时同时销毁本地服务
     * @Author : xiaoyun
     * @Created_Time : 2023/2/13 21:16
     * @Project_Name : MainActivity.java
     * @PACKAGE_NAME : com.example.gaodemapdemo
     * @Params :
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        销毁定位客户端，同时销毁本地定位服务
        mLocationClient.onDestroy();
//        在activity执行onDestroy时执行MapView.onDestroy()，销毁地图
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        在activity执行onResume时执行MapView.onResume()，重新绘制加载地图
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        在activity执行onPause时执行MapView.onPause()，暂停地图的绘制
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        在activity执行onSaveInstanceState时执行MapView.onSaveInstanceState(outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }

    /*
     * @Desc : 激活定位
     * @Author : xiaoyun
     * @Created_Time : 2023/2/14 14:45
     * @Project_Name : MainActivity.java
     * @PACKAGE_NAME : com.example.gaodemapdemo
     * @Params :
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient == null) {
            mLocationClient.startLocation();    //启动定位
        }
    }

    /*
     * @Desc : 停止定位
     * @Author : xiaoyun
     * @Created_Time : 2023/2/14 14:47
     * @Project_Name : MainActivity.java
     * @PACKAGE_NAME : com.example.gaodemapdemo
     * @Params :
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }

    /*
     * @Desc : 浮动按钮点击查询附近POI
     * @Author : xiaoyun
     * @Created_Time : 2023/2/14 21:23
     * @Project_Name : MainActivity.java
     * @PACKAGE_NAME : com.example.gaodemapdemo
     * @Params : view
     */
    public void queryPOI(View view) throws AMapException {
//        构造query对象
        query = new PoiSearchV2.Query("购物", "", cityCode);
//        设置每页最多返回多少条poiitem
        query.setPageSize(10);
//        设置查询页码
        query.setPageNum(1);
//        构造PoiSearch对象
        poiSearchV2 = new PoiSearchV2(this, query);
//        设置搜索回调监听
        poiSearchV2.setOnPoiSearchListener(this);
//        发起搜索附近POI异步请求
        poiSearchV2.searchPOIAsyn();
    }

    /*
     * @Desc : POI搜索返回
     * @Author : xiaoyun
     * @Created_Time : 2023/2/14 21:34
     * @Project_Name : MainActivity.java
     * @PACKAGE_NAME : com.example.gaodemapdemo
     * @Params : poiResultV2 POI所有数据
     */
    @Override
    public void onPoiSearched(PoiResultV2 poiResultV2, int i) {
//        解析result获取POI信息

//        获取POI数组列表
        ArrayList<PoiItemV2> poiItems = poiResultV2.getPois();
        for (PoiItemV2 poiItem : poiItems) {
            Log.d("MainActivity"," Title："+poiItem.getTitle()+" Snippet："+poiItem.getSnippet());
        }
    }

    /*
     * @Desc : POI中的项目搜索返回
     * @Author : xiaoyun
     * @Created_Time : 2023/2/14 21:39
     * @Project_Name : MainActivity.java
     * @PACKAGE_NAME : com.example.gaodemapdemo
     * @Params : poiItemV2  获取POI item
     */
    @Override
    public void onPoiItemSearched(PoiItemV2 poiItemV2, int i) {

    }

    /*
     * @Desc : 地图单机事件
     * @Author : xiaoyun
     * @Created_Time : 2023/2/14 21:59
     * @Project_Name : MainActivity.java
     * @PACKAGE_NAME : com.example.gaodemapdemo
     * @Params : latLng
     */
    @Override
    public void onMapClick(LatLng latLng) {
        showMsg("点击了地图，经度： "+latLng.latitude+"，纬度："+latLng.longitude);
//        通过经纬度获取地址
        latlonToAddress(latLng);

//        添加标点
//        aMap.addMarker(new MarkerOptions().position(latLng).snippet("DefaultMarker"));
        addMarker(latLng);
    }

    /*
     * @Desc : 地图长按事件
     * @Author : xiaoyun
     * @Created_Time : 2023/2/14 22:01
     * @Project_Name : MainActivity.java
     * @PACKAGE_NAME : com.example.gaodemapdemo
     * @Params : latLng
     */
    @Override
    public void onMapLongClick(LatLng latLng) {
        showMsg("长按了地图，经度： " + latLng.latitude + "，纬度：" + latLng.longitude);
//        通过经纬度获取地址
        latlonToAddress(latLng);
    }

    /*
     * @Desc : 坐标转地址
     * @Author : 22494
     * @Created_Time : 2023/2/15 1:11
     * @Project_Name : MainActivity.java
     * @PACKAGE_NAME : com.example.gaodemapdemo
     * @Params : regeocodeResult, i
     */
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int rCode) {
//        解析result获取地址描述信息
        if (rCode == PARSE_SUCCESS_CODE) {
            RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
//            显示解析后的地址
            showMsg("地址：" + regeocodeAddress.getFormatAddress());
        } else {
            showMsg("获取地址失败！");
        }
    }

    /*
     * @Desc : 地址转坐标
     * @Author : 22494
     * @Created_Time : 2023/2/15 1:12
     * @Project_Name : MainActivity.java
     * @PACKAGE_NAME : com.example.gaodemapdemo
     * @Params :
     */
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int rCode) {
        if (rCode == PARSE_SUCCESS_CODE) {
            List<GeocodeAddress> geocodeAddressList = geocodeResult.getGeocodeAddressList();
            if (geocodeAddressList != null && geocodeAddressList.size() > 0) {
                LatLonPoint latLonPoint = geocodeAddressList.get(0).getLatLonPoint();
//                显示解析后的坐标
                showMsg("坐标：" + latLonPoint.getLatitude() + "，" + latLonPoint.getLongitude());
            }
        } else {
            showMsg("获取坐标失败！");
        }
    }

    /*
    * @Desc : 通过经纬度获取地址
    * @Author : xiaoyun
    * @Created_Time : 2023/2/15 17:35
    * @Project_Name : MainActivity.java
    * @PACKAGE_NAME : com.example.gaodemapdemo
    * @Params : latLng  表示一个具有纬度(lat)和经度(lng)的地理坐标(以度为单位)
    */
    private void latlonToAddress(LatLng latLng) {
//        位置点，通过经纬度构建
        LatLonPoint latLonPoint = new LatLonPoint(latLng.latitude, latLng.longitude);
//        逆编码查询 第一个参数表示一个LatLng，第二个参数表示范围多少米，第三个参数表示是火星坐标系还是GPS坐标系
//        LatLng是表示一个具有纬度(lat)和经度(lng)的地理坐标(以度为单位)
//        Point指的是用像素表示x和y的坐标点。
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 0, GeocodeSearch.AMAP);
//        异步获取地址信息
        geocodeSearch.getFromLocationAsyn(query);
    }

    /*
    * @Desc : 键盘点击
    * @Author : xiaoyun
    * @Created_Time : 2023/2/15 20:16
    * @Project_Name : MainActivity.java
    * @PACKAGE_NAME : com.example.gaodemapdemo
    * @Params :
    */
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
//            获取输入框的值
            String address = etAddress.getText().toString().trim();
            if (address == null || address.isEmpty()) {
                showMsg("请输入地址");
            } else {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                隐藏软键盘
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);

//                name表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode
                GeocodeQuery query = new GeocodeQuery(address, city);
                geocodeSearch.getFromLocationNameAsyn(query);
            }
            return true;
        }
        return false;
    }

    /*
     * @Desc : 添加地图标点
     * @Author : xiaoyun
     * @Created_Time : 2023/2/15 22:07
     * @Project_Name : MainActivity.java
     * @PACKAGE_NAME : com.example.gaodemapdemo
     * @Params :
     */
    private void addMarker(LatLng latLng) {
//        显示浮动按钮
        fabClearMaker.show();
//        添加标点
        Marker marker = aMap.addMarker(new MarkerOptions().position(latLng).snippet("DefaultMarker"));
        markerList.add(marker);
    }

    /*
     * @Desc : 清空地图标点
     * @Author : xiaoyun
     * @Created_Time : 2023/2/15 22:13
     * @Project_Name : MainActivity.java
     * @PACKAGE_NAME : com.example.gaodemapdemo
     * @Params :
     */
    public void clearAllMarker(View view) {
        if (markerList != null && markerList.size() > 0) {
            for (Marker markerItem:markerList
                 ) {
                markerItem.remove();
            }
        }
        fabClearMaker.hide();
    }
}
