package com.example.gaodemapdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.MapView;

import java.security.PublicKey;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements AMapLocationListener {
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

//        初始化定位
        initLocation();

//        检查Android版本
        checkingAndroidVersion();
    }


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
     * 动态请求权限
     * */
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
                String city = aMapLocation.getCity();
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

                Log.d("MainActivity",stringBuffer.toString());
                showMsg(address);

//                tvContent.setText(address == null ? "无地址" : address);
//                tvContent.setText(stringBuffer.toString());

//                停止定位后，本地定位服务并不会被销毁
                mLocationClient.stopLocation();
            } else {
//                定位失败时，可通过ErrorCode(错误码)信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "Location Erroe, ErrorCode: "
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
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
    protected void onDestroy(){
        super.onDestroy();
//        销毁定位客户端，同时销毁本地定位服务
        mLocationClient.onDestroy();
//        在activity执行onDestroy时执行MapView.onDestroy()，销毁地图
        mapView.onDestroy();
    }

    @Override
    protected void onResume(){
        super.onResume();
//        在activity执行onResume时执行MapView.onResume()，重新绘制加载地图
        mapView.onResume();
    }
    @Override
    protected void onPause(){
        super.onPause();
//        在activity执行onPause时执行MapView.onPause()，暂停地图的绘制
        mapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
//        在activity执行onSaveInstanceState时执行MapView.onSaveInstanceState(outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }
}