package com.ceiv.signpost;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnCameraChangeListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLongClickListener;
import com.amap.api.maps.AMap.OnMapTouchListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.offlinemap.OfflineMapManager.OfflineLoadedListener;
import com.amap.api.maps.offlinemap.OfflineMapManager.OfflineMapDownloadListener;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.WalkRouteResult;
import com.ceiv.data.StationData;
import com.ceiv.signpost.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class fragment_map extends Fragment {

    private final static String TAG = "fragment_map";
    private ImageView stationImage;
    private ImageView hospitalImage;
    private ImageView bankImage;
    private ImageView backImage;
    private MapView mapView;
    private UiSettings mUiSettings;
    private AMapLocation aMapLocation = null;
    //设备当前的位置
    private LatLonPoint latLonPoint;
    //设备当前所在的城市编码
    private String cityCode;
    /* 搜索相关 */
    private PoiSearch.Query query;
    private PoiSearch poiSearch;
    private PoiResult poiResult;
    private Marker locationMarker;
    private Marker detailMarker;
    private Marker mlastMarker;
    private RelativeLayout mPoiDetail;
    //默认搜索范围
    private int area = 0;
    private static GeocodeSearch geocoderSearch;
    private String addressName;
    private Double position_tv_x;    //起始经纬度
    private Double position_tv_y;
    private String x;  //全局的marker终止经纬度
    private String y;
    private RouteSearch routeSearch;
    private AMap aMap;
    private MyLocationStyle myLocationStyle;
    private BitmapDescriptor bitmapDescriptor, bitmapDescriptor2;
    private Polyline polyline;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (MapView) view.findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mapView.onCreate(savedInstanceState);
        init();
        return view;
    }

    private void init() {
        //初始化地图控制器对象
        if (aMap == null) {
            aMap = mapView.getMap();
        }

        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        /**
         * myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);//只定位一次。
         * myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE) ;//定位一次，且将视角移动到地图中心点。
         * myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW) ;//连续定位、且将视角移动到地图中心点，定位蓝点跟随设备移动。（1秒1次定位）
         * myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE);//连续定位、且将视角移动到地图中心点，地图依照设备方向旋转，定位点会跟随设备移动。（1秒1次定位）
         * myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）默认执行此种模式。
         * //以下三种模式从5.1.0版本开始提供
         * myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);//连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
         * myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER);//连续定位、蓝点不会移动到地图中心点，并且蓝点会跟随设备移动。
         * myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE_NO_CENTER);//连续定位、蓝点不会移动到地图中心点，地图依照设备方向旋转，并且蓝点会跟随设备移动。
         */
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.showMyLocation(true);//设置是否显示定位小蓝点，用于满足只想使用定位，不想使用定位小蓝点的场景，设置false以后图面上不再有定位蓝点的概念，但是会持续回调位置信息。
        myLocationStyle.strokeWidth(50);//设置定位蓝点精度圈的边框宽度的方法。
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色
        aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.moveCamera(CameraUpdateFactory.zoomTo(12));

        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                ToastUtil.show(getContext(), location.getLongitude() + "," + location.getLatitude());
            }
        });

        //绘制标记点marker
        LatLng latLng = new LatLng(39.906901, 116.397972);
        final Marker marker1 = aMap.addMarker(new MarkerOptions().position(new LatLng(39.906901, 116.397972)).title("标记1").snippet(StationData.busMessage[0]));
        marker1.showInfoWindow();

        //绘制标记线
        List<LatLng> latLngs = new ArrayList<LatLng>();
        latLngs.add(new LatLng(39.906901, 116.397972));//北京

        latLngs.add(new LatLng(35.124259, 112.923045));//西沁阳---
        latLngs.add(new LatLng(35.128675, 112.923928));//拐点
        latLngs.add(new LatLng(35.129432, 112.916712));//拐点
        latLngs.add(new LatLng(35.129854, 112.915837));//西义合---
        latLngs.add(new LatLng(35.132139, 112.912726));//秘涧---
        latLngs.add(new LatLng(35.132374, 112.912341));//拐点
        latLngs.add(new LatLng(35.133302, 112.904723));//魏村---
        latLngs.add(new LatLng(35.133652, 112.901885));//拐点
        latLngs.add(new LatLng(35.134411, 112.899659));//拐点
        latLngs.add(new LatLng(35.13449, 112.899069));//龙泉---
        latLngs.add(new LatLng(35.136388, 112.88276));//屯头---
        latLngs.add(new LatLng(35.136897, 112.878323));//拐点
        latLngs.add(new LatLng(35.1367, 112.876993));//拐点
        latLngs.add(new LatLng(35.136393, 112.876349));//拐点
        latLngs.add(new LatLng(35.135498, 112.874981));//拐点
        latLngs.add(new LatLng(35.135156, 112.873383));//解住---
        latLngs.add(new LatLng(35.136477, 112.863131));//东高村---
        latLngs.add(new LatLng(35.137644, 112.854731));//西高村---
        latLngs.add(new LatLng(35.138539, 112.847194));//清河---
        latLngs.add(new LatLng(35.139561, 112.83985));//北鲁村---
        latLngs.add(new LatLng(35.139894, 112.83905));//拐点
        latLngs.add(new LatLng(35.14157, 112.837387));//拐点
        latLngs.add(new LatLng(35.142452, 112.835762));//拐点
        latLngs.add(new LatLng(35.142816, 112.834024));//常乐---
        latLngs.add(new LatLng(35.143373, 112.830441));//拐点
        latLngs.add(new LatLng(35.144044, 112.829142));//拐点
        latLngs.add(new LatLng(35.145966, 112.827914));//拐点
        latLngs.add(new LatLng(35.146755, 112.826423));//拐点
        latLngs.add(new LatLng(35.147812, 112.818355));//长沟村---
        latLngs.add(new LatLng(35.148624, 112.80857));//范村---
        latLngs.add(new LatLng(35.148839, 112.798721));//王村---
        latLngs.add(new LatLng(35.149343, 112.794692));//拐点
        latLngs.add(new LatLng(35.141579, 112.794853));//拐点
        latLngs.add(new LatLng(35.141601, 112.793474));//拐点
        latLngs.add(new LatLng(35.141755, 112.793077));//拐点
        latLngs.add(new LatLng(35.142312, 112.787793));//坞头---
        latLngs.add(new LatLng(35.143044, 112.780948));//拐点
        latLngs.add(new LatLng(35.143702, 112.780846));//拐点
        latLngs.add(new LatLng(35.144005, 112.778363));//东庄---
        latLngs.add(new LatLng(35.144484, 112.775118));//拐点
        latLngs.add(new LatLng(35.147348, 112.775569));//拐点
        latLngs.add(new LatLng(35.147637, 112.773418));//后庄---
        latLngs.add(new LatLng(35.147852, 112.771927));//拐点
        latLngs.add(new LatLng(35.14783, 112.771138));//拐点
        latLngs.add(new LatLng(35.148032, 112.769432));//拐点
        latLngs.add(new LatLng(35.146137, 112.769116));//窑头---

        polyline = aMap.addPolyline(new PolylineOptions().
                addAll(latLngs).width(5).color(Color.argb(100, 0, 0, 255)));

        List<LatLng> latLngs2 = new ArrayList<LatLng>();
        latLngs2.add(new LatLng(35.124259, 112.923045));//西沁阳---
        latLngs2.add(new LatLng(35.129854, 112.915837));//西义合---
        latLngs2.add(new LatLng(35.132139, 112.912726));//秘涧---
        latLngs2.add(new LatLng(35.133302, 112.904723));//魏村---
        latLngs2.add(new LatLng(35.13449, 112.899069));//龙泉---
        latLngs2.add(new LatLng(35.136388, 112.88276));//屯头---
        latLngs2.add(new LatLng(35.135156, 112.873383));//解住---
        latLngs2.add(new LatLng(35.136477, 112.863131));//东高村---
        latLngs2.add(new LatLng(35.137644, 112.854731));//西高村---
        latLngs2.add(new LatLng(35.138539, 112.847194));//清河---
        latLngs2.add(new LatLng(35.139561, 112.83985));//北鲁村---
        latLngs2.add(new LatLng(35.142816, 112.834024));//常乐---
        latLngs2.add(new LatLng(35.147812, 112.818355));//长沟村---
        latLngs2.add(new LatLng(35.148624, 112.80857));//范村---
        latLngs2.add(new LatLng(35.148839, 112.798721));//王村---
        latLngs2.add(new LatLng(35.142312, 112.787793));//坞头---
        latLngs2.add(new LatLng(35.144005, 112.778363));//东庄---
        latLngs2.add(new LatLng(35.147637, 112.773418));//后庄---
        latLngs2.add(new LatLng(35.146137, 112.769116));//窑头---

        List<LatLng> latLngs3 = new ArrayList<LatLng>();
        latLngs3.add(new LatLng(35.126613, 112.923515));
        latLngs3.add(new LatLng(35.13528, 112.892389));
        latLngs3.add(new LatLng(35.138306, 112.84957));
        latLngs3.add(new LatLng(35.147216, 112.823043));
        latLngs3.add(new LatLng(35.142689, 112.784044));

        //自定义标记图标marker
        bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bus_station));
        bitmapDescriptor2 = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bus_location));
        for (int i = 0; i < latLngs2.size(); i++) {
            aMap.addMarker(new MarkerOptions().position(latLngs2.get(i)).anchor(0.5f, 0.5f).title("沁阳←→窑头").snippet(StationData.stationName[i]).draggable(false).icon(bitmapDescriptor).setFlat(false));
        }
        for (int i = 0; i < 5; i++) {
            aMap.addMarker(new MarkerOptions().position(latLngs3.get(i)).anchor(0.5f, 0.5f).title(StationData.busDirection[i]).snippet(StationData.busMessage[i]).draggable(false).icon(bitmapDescriptor2).setFlat(false));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



}
