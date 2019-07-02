package com.ceiv.signpost;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ceiv.communication.utils.SystemInfoUtils;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
天气预报南宁城市编码：
南宁城区：101300101

1:http://www.weather.com.cn/data/sk/101300101.html
{"weatherinfo":{"city":"南宁","cityid":"101300101","temp":"33.9","WD":"东南风","WS":"小于3级","SD":"61%","AP":"986.3hPa","njd":"暂无实况","WSE":"<3","time":"17:00","sm":"3.3","isRadar":"1","Radar":"JC_RADAR_AZ9771_JB"}}

2:http://wthrcdn.etouch.cn/WeatherApi?citykey=101300101

*/


public class fragment_date extends Fragment {

    private final static String TAG = "fragment_date";
    private final String weatherAddr = "http://wthrcdn.etouch.cn/WeatherApi?citykey=";
    private final String timeServer = "http://124.227.197.82:1001/BusService/Query_ServerTime";
    private String cityCode = "101300101";
    private String cityName = "南宁";

    public final static int WEATHER_INFO = 0x05;
    private TextView text_weather;
    private TextView text_tmp;
    private TextView text_tmp_cur;
    private TextView text_wind;


    public Handler dateHandler;

    public class WeatherInfo {
        public String cityName;
        public String weather;
        public String windDir;
        public String windLevel;
        public int tmpCur;
        public int tmpHigh;
        public int tmpLow;
        private int infoFlag;       //bit0:weather, bit1:windDir, bit2:windLevel, bit3:tmpCur, bit4: tmpHigh, bit5:tmpLow
        public WeatherInfo(String cityName) {
            this.cityName = cityName;
            this.infoFlag = 0x0;
        }
    }

    private void setTextValue(TextView view, String value) {
        view.setText(value);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_date, container, false);
        text_weather = view.findViewById(R.id.text_weather);
        text_tmp = view.findViewById(R.id.text_tmp);
        text_tmp_cur = view.findViewById(R.id.text_tmp_cur);
        text_wind = view.findViewById(R.id.text_wind);

        dateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case WEATHER_INFO:
                        Log.d(TAG, "Receive weather info!");
                        text_weather.setText(msg.getData().getString("weather"));
                        text_tmp.setText(msg.getData().getString("tmp"));
                        text_tmp_cur.setText(msg.getData().getString("tmp_cur"));
                        text_wind.setText(msg.getData().getString("wind"));
                        break;
                    default:
                        break;
                }
            }
        };
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        android.provider.Settings.System.putString(MainActivity.context.getContentResolver(), Settings.System.TIME_12_24, "24");

        //开启获取天气信息线程
        new Thread(new Runnable() {
            @Override
            public void run() {

                boolean exption = false;

                while (true) {
                    HttpURLConnection urlConnection = null;
                    try {
                        URL url = new URL(weatherAddr + cityCode);
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setConnectTimeout(8000);
                        urlConnection.setReadTimeout(8000);
                        InputStream in = urlConnection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuffer sb = new StringBuffer();
                        String str;
                        while ((str = reader.readLine()) != null) {
                            sb.append(str);
                            Log.d(TAG, "data from url: " + str);
                        }
                        String response = sb.toString();
                        Log.d(TAG, "response: " + response);

                        WeatherInfo weatherInfo = parseXML(response);
                        if (weatherInfo != null) {
                            Message msg = Message.obtain();
                            msg.what = WEATHER_INFO;
                            Bundle data = new Bundle();
                            data.putString("weather", "天气：" + weatherInfo.weather);
                            data.putString("tmp", weatherInfo.tmpLow + "~" + weatherInfo.tmpHigh + "°C");
                            data.putString("tmp_cur", "实时：" + weatherInfo.tmpCur + "°C");
                            data.putString("wind", weatherInfo.windDir + weatherInfo.windLevel);
                            msg.setData(data);
                            dateHandler.sendMessage(msg);

                            exption = false;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "获取天气信息失败！");
                        exption = true;
                        e.printStackTrace();
                    } finally {
                        try {
                            if (exption) {
                                Log.d(TAG, "获取天气信息失败，5秒后尝试再次获取");
                                Thread.sleep(1000 * 5);
                            } else {
                                Log.d(TAG, "获取天气信息成功，5分钟后后再更新信息");
                                Thread.sleep(1000 * 60 * 5);
                            }
                        } catch (InterruptedException e) {
                            Log.e(TAG, "获取天气线程sleep异常！");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();

        //开启校准时间线程
        new Thread(new Runnable() {
            boolean needReqTime = true;
            @Override
            public void run() {
                while (needReqTime) {
                    try {
                        URL url = new URL(timeServer);
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setConnectTimeout(8000);
                        urlConnection.setReadTimeout(8000);
                        InputStream in = urlConnection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuffer sb = new StringBuffer();
                        String str;
                        while ((str = reader.readLine()) != null) {
                            sb.append(str);
                            Log.d(TAG, "data from url: " + str);
                        }
                        String respone = sb.toString();

                        Log.d(TAG, "respone: " + respone);
                        urlConnection.disconnect();
                        String timeString = parseJsonTime(respone);

                        String[] data = timeString.split(" ");
                        String[] date = data[0].split("-");
                        String[] time = data[1].split(":");
                        int year = Integer.valueOf(date[0]);
                        int month = Integer.valueOf(date[1]);
                        int day = Integer.valueOf(date[2]);
                        int hour = Integer.valueOf(time[0]);
                        int minute = Integer.valueOf(time[1]);
                        int second = Integer.valueOf(time[2]);
                        //进行简单的检查
                        if (year < 2018 || year > 2118 || month > 12 || month < 1 || day < 1 || day > 31 ||
                                hour < 0 || hour > 24 || minute < 0 || minute > 59 || second < 0 || second > 59) {
                            Log.d(TAG, "Invalid Time Value!");
                            return;
                        }
                        SystemInfoUtils.setSystemTime(getActivity(), year, month, day, hour, minute, second);
                        needReqTime = false;
                    } catch (IOException ioException) {
                        Log.e(TAG, " failed!");
                        ioException.printStackTrace();
                    } finally {
                        try {
                            Thread.sleep(5 * 1000);
                        } catch (Exception e) {
                            Log.d(TAG, "Time thread sleep error!");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }


    private WeatherInfo parseXML(String xmlData) {

        WeatherInfo weatherInfo = new WeatherInfo(cityName);

        String tmpCityName;

        String regEX = "[^0-9]";
        Pattern p = Pattern.compile(regEX);

        String dayOrNight;
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour < 18) {
            dayOrNight = "day";
        } else {
            dayOrNight = "night";
        }
        Log.d(TAG, "当前时间是：" + dayOrNight);

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));

            int eventType = xmlPullParser.getEventType();
            Log.d(TAG, "start parse xml");

            while (eventType != xmlPullParser.END_DOCUMENT && weatherInfo.infoFlag != 0x3f) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("city")) {
                            tmpCityName = xmlPullParser.nextText();
                            if (!weatherInfo.cityName.equals(tmpCityName)) {
                                Log.d(TAG, "获取的城市出错：" + tmpCityName);
                                return null;
                            }
                        } else if (xmlPullParser.getName().equals("wendu")) {
                            weatherInfo.tmpCur = Integer.valueOf(xmlPullParser.nextText());
                            Log.d(TAG, "当前温度为：" + weatherInfo.tmpCur);
                            weatherInfo.infoFlag |= 0x8;
                            if (weatherInfo.tmpCur > 50.0 || weatherInfo.tmpCur < -20) {
                                Log.d(TAG, "获取的温度值异常：" + weatherInfo.tmpCur);
                                return null;
                            }
                        } else if (xmlPullParser.getName().equals("fengli")) {
                            weatherInfo.windLevel = xmlPullParser.nextText();
                            weatherInfo.infoFlag |= 0x4;
                            Log.d(TAG, "风力：" + weatherInfo.windLevel);
                        } else if (xmlPullParser.getName().equals("fengxiang")) {
                            weatherInfo.windDir = xmlPullParser.nextText();
                            weatherInfo.infoFlag |= 0x2;
                            Log.d(TAG, "风向：" + weatherInfo.windDir);
                        } else if (xmlPullParser.getName().equals("low")) {
                            String tmp = xmlPullParser.nextText();
                            Log.d(TAG, "<low>" + tmp + "</low>");
                            Matcher m = p.matcher(tmp);
                            weatherInfo.tmpLow = Integer.valueOf(m.replaceAll("").trim());
                            weatherInfo.infoFlag |= 0x20;
                            if (weatherInfo.tmpLow < -50 || weatherInfo.tmpLow > 70) {
                                Log.e(TAG, "获取的最低温度值异常：" + weatherInfo.tmpLow);
                                return null;
                            }
                        } else if (xmlPullParser.getName().equals("high")) {
                            String tmp = xmlPullParser.nextText();
                            Log.d(TAG, "<high>" + tmp + "</high>");
                            Matcher m = p.matcher(tmp);
                            weatherInfo.tmpHigh = Integer.valueOf(m.replaceAll("").trim());
                            weatherInfo.infoFlag |= 0x10;
                            if (weatherInfo.tmpHigh < -50 || weatherInfo.tmpHigh > 70) {
                                Log.e(TAG, "获取的最高温度值异常：" + weatherInfo.tmpHigh);
                                return null;
                            }
                        } else if (xmlPullParser.getName().equals("day") || xmlPullParser.getName().equals("night")){
                            if (dayOrNight.equals(xmlPullParser.getName())) {
                                eventType = xmlPullParser.next();
                                if (xmlPullParser.getName().equals("type")) {
                                    weatherInfo.weather = xmlPullParser.nextText();
                                    Log.d(TAG, "天气：" + weatherInfo.weather);
                                    weatherInfo.infoFlag |= 0x1;
                                } else {
                                    Log.d(TAG, "<day> or <night>标签后面没有<type>标签！！！");
                                    return null;
                                }
                            }
                        } else {

                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                    default:
                        //其他的标志直接跳过
                        break;
                }
                eventType = xmlPullParser.next();
            }
            if (weatherInfo.infoFlag != 0x3f) {
                Log.e(TAG, "获取的天气信息不完整！");
                return null;
            } else if (weatherInfo.tmpLow > weatherInfo.tmpHigh) {
                Log.e(TAG, "最低气温大于最高气温！");
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "parseXML 异常！");
            e.printStackTrace();
        }
        return weatherInfo;
    }


    private void getNetTime() {

    }

//    private boolean is24Hour(Context context) {
//        return DateFormat.is24Hour;
//    }

    private String parseJsonTime(String jsonData) {
        try {
            JSONObject jsonObject=new JSONObject(jsonData);
            String time=jsonObject.get("ErrorInfo").toString();
            System.out.println(time);
            return time;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }






}











