package com.warmweather.app.util;

import com.warmweather.app.model.City;
import com.warmweather.app.model.County;
import com.warmweather.app.model.Province;
import com.warmweather.app.model.WarmWeatherDB;

import android.text.TextUtils;

public class Utility {
	/*解析和处理服务器返回的省级数据*/
	public synchronized static boolean handleProvincesResponse(WarmWeatherDB warmWeatherDB,String response){
		if(!TextUtils.isEmpty(response)){
			String[] allProvinces = response.split(",");
			if(allProvinces!=null&&allProvinces.length>0){
			for(String p:allProvinces){
				String[] array = p.split("\\|");
				Province province = new Province();
				province.setProvinceCode(array[0]);
				province.setProvinceName(array[1]);
				//将解析出来的数据存储到Province表
				warmWeatherDB.saveProvince(province);
			}
			return true;
			}
		}
		return false;
	}
	
	/*解析和处理服务器返回的市级数据*/
	
	public  static boolean handleCitiesResponse(WarmWeatherDB warmWeatherDB,String response,int provinceId){
		if(!TextUtils.isEmpty(response)){
			String[] allCities = response.split(",");
			if(allCities!=null&&allCities.length>0){
			for(String w:allCities){
				String[] array = w.split("\\|");
				City city = new City();
				city.setCityCode(array[0]);
				city.setCityName(array[1]);
				city.setProvinceId(provinceId);
				//将解析出来的数据存储到City表
				warmWeatherDB.saveCity(city);
			}
			return true;
			}
		}
		return false;
	}
	
	
/*解析和处理服务器返回的市级数据*/
	
	public  static boolean handleCountiesResponse(WarmWeatherDB warmWeatherDB,String response,int cityId){
		if(!TextUtils.isEmpty(response)){
			String[] allCounties = response.split(",");
			if(allCounties!=null&&allCounties.length>0){
			for(String e:allCounties){
				String[] array = e.split("\\|");
				County county = new County();
				county.setCountyCode(array[0]);
				county.setCountyName(array[1]);
				county.setCityId(cityId);
				//将解析出来的数据存储到County表
				warmWeatherDB.saveCounty(county);
			}
			return true;
			}
		}
		return false;
	}

}
