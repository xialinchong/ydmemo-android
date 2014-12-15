package com.yidianhulian.ydmemo.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapTouchListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.yidianhulian.ydmemo.R;
import com.yidianhulian.ydmemo.Util;

/**
 * 提醒地点选择界面
 * 
 * @author xialinchong 2014-19-19
 */

public class MapForRemind extends Activity implements OnGeocodeSearchListener,
        AMapLocationListener {

    private MapView mapView;
    private AMap aMap;
    public static final int GET_ADDRESS = 6;
    public static final int CANCEL_ADDRESS = 7;
    /**
     * 判断坐标是否已经选择过 author xialinchong
     */
    private String mPoint = "";
    private double mLatitude;
    private double mLongitude;

    private GeocodeSearch geocoderSearch;
    private LatLonPoint latLonPoint;
    private String mAddressName;

    private LocationManagerProxy mLocationManagerProxy;// 默认定位

    /**
     * 初始化定位
     */
    private void initLocation() {
        Intent itn = getIntent();
        mPoint = itn.getStringExtra("point");
        if (!mPoint.equals("")) {
            mAddressName = itn.getStringExtra("addressName");
            String point[] = mPoint.split(",");
            mLatitude = Double.valueOf(point[0]);
            mLongitude = Double.valueOf(point[1]);
            LatLng latLng = new LatLng(mLatitude, mLongitude);
            locationDev(latLng);
        } else {
            Util.showLoading(this, "正在获取当前位置...");

            // 初始化定位，只采用网络定位
            mLocationManagerProxy = LocationManagerProxy
                    .getInstance(getBaseContext());
            mLocationManagerProxy.setGpsEnable(false);
            mLocationManagerProxy.requestLocationData(
                    LocationProviderProxy.AMapNetwork, -1, 15, this);
        }
    }

    private void locationDev(LatLng latLng) {
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
        aMap.moveCamera(CameraUpdateFactory.zoomTo(12));
        addMarker(latLng);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_for_remind);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setIcon(R.drawable.logo);
        actionBar.setTitle(R.string.remind_addr);

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            // aMap.set
            initLocation();
            // aMap.setOnMapClickListener(new OnMapClickListener() {
            //
            // @Override
            // public void onMapClick(LatLng point) {
            // latLonPoint = new LatLonPoint(point.latitude,
            // point.longitude);
            // getAddress(latLonPoint);
            // mPoint = point.latitude + "," + point.longitude;
            // addMarker(point);
            // locationDev(point);
            // }
            // });
            aMap.setOnMapTouchListener(new OnMapTouchListener() {

                @Override
                public void onTouch(MotionEvent arg0) {
                    if (arg0.getAction() == MotionEvent.ACTION_UP) {
                        LatLng point = aMap.getCameraPosition().target;
                        mPoint = point.latitude + "," + point.longitude;
                        latLonPoint = new LatLonPoint(point.latitude,
                                 point.longitude);
                        getAddress(latLonPoint);
                    }
                }
            });
        }

        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
    }

    /**
     * 响应逆地理编码
     */
    public void getAddress(final LatLonPoint latLonPoint) {
        Util.showLoading(this, "正在获取位置坐标...");
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
                GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        geocoderSearch.getFromLocationAsyn(query);// 设置同步逆地理编码请求
    }

    private void addMarker(LatLng latlng) {
        aMap.clear();
        Marker marker = aMap.addMarker(new MarkerOptions()
                .position(latlng)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .perspective(true).draggable(true));
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        marker.setPositionByPixels(Integer.valueOf(width / 2),
                Integer.valueOf(height / 2));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.ok, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent();
            setResult(CANCEL_ADDRESS, intent);
            finish();
            return true;
        }
        if (item.getItemId() == R.id.ok) {
            if (check_value())
                return true;
            Intent intent = new Intent();
            intent.putExtra("point", mPoint);
            intent.putExtra("addressName", mAddressName);
            setResult(GET_ADDRESS, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean check_value() {
        if (mPoint.equals("")) {
            Util.showToast(this, "请选择提醒地点！");
            return true;
        }
        return false;
    }

    @Override
    public void onGeocodeSearched(GeocodeResult arg0, int arg1) {

    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        Util.hideLoading();
        if (rCode == 0) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
                mAddressName = result.getRegeocodeAddress().getFormatAddress();
            } else {
                Util.showToast(this, "没有找到匹配地址");
            }
        } else if (rCode == 27) {
            Util.showToast(this, "网络错误，请检查网络连接");
        } else if (rCode == 32) {
            Util.showToast(this, "key验证无效");
        } else {
            Util.showToast(this, "未知错误，请联系开发人员");
        }
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        Util.hideLoading();
        if (amapLocation != null
                && amapLocation.getAMapException().getErrorCode() == 0) {
            // 获取位置信息
            mLatitude = amapLocation.getLatitude();
            mLongitude = amapLocation.getLongitude();
        } else {
            mLatitude = 26.574231;
            mLongitude = 106.717876;
        }
        latLonPoint = new LatLonPoint(mLatitude, mLongitude);
        getAddress(latLonPoint);
        mPoint = mLatitude + "," + mLongitude;
        LatLng latLng = new LatLng(mLatitude, mLongitude);
        locationDev(latLng);
    }
}
