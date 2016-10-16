package com.warmweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.warmweather.app.R;
import com.warmweather.app.model.City;
import com.warmweather.app.model.County;
import com.warmweather.app.model.Province;
import com.warmweather.app.model.WarmWeatherDB;
import com.warmweather.app.util.HttpCallbackListener;
import com.warmweather.app.util.HttpUtil;
import com.warmweather.app.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private WarmWeatherDB warmWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	/*省、市、县列表*/
	private List<Province> provinceList;
	private List<City> cityList;
	private List<County> countyList;
	
	/*选中的省份、城市*/
	private Province selectedProvince;
	private City selectedCity;
	/*当前选中的级别*/
	private int currentLevel;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView)findViewById(R.id.list_view);
		titleText = (TextView)findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		warmWeatherDB = WarmWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?>arg0,View view,int index,long arg3){
				if(currentLevel == LEVEL_PROVINCE){
					selectedProvince = provinceList.get(index);
					queryCities();
				}else if(currentLevel == LEVEL_CITY){
					selectedCity = cityList.get(index);
					queryCounties();
				}
			}
		});
		queryProvinces();
	}
		
		/*加载省级数据*/
		private void queryProvinces(){
			provinceList = warmWeatherDB.loadProvinces();
			/*查询全国所有省，优先从数据库查询，如果没有再到服务器查询*/
			if(provinceList.size()>0){
				dataList.clear();
				for(Province province:provinceList){
					dataList.add(province.getProvinceName());
				}
				adapter.notifyDataSetChanged();
				listView.setSelection(0);
				titleText.setText("中国");
				currentLevel = LEVEL_PROVINCE;
			}else{
				queryFromServer(null,"province");
			}
	}
		
		
		/**
		 * 查询选中省内所有的⑩，优先从数据库查询，如果没有查询到再去服务器查询
		 */
		private void queryCities(){
			cityList = warmWeatherDB.loadCities(selectedProvince.getId());
			if(cityList.size()>0){
				dataList.clear();
				for(City city:cityList){
					dataList.add(city.getCityName());
					
				}
				adapter.notifyDataSetChanged();
				listView.setSelection(0);
				titleText.setText(selectedProvince.getProvinceName());
				currentLevel = LEVEL_CITY;
			}else{
				queryFromServer(selectedProvince.getProvinceCode(),"city");
			}
		}
		
		/**
		 * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器查询
		 */
		
		private void queryCounties(){
			countyList = warmWeatherDB.loadCounties(selectedCity.getId());
			if(countyList.size()>0){
				dataList.clear();
				for(County county:countyList){
					dataList.add(county.getCountyName());
				}
				
				adapter.notifyDataSetChanged();
				listView.setSelection(0);
				titleText.setText(selectedCity.getCityName());
				currentLevel = LEVEL_COUNTY;
			}else {
				queryFromServer(selectedCity.getCityCode(),"county");
			}	
		}
		/**
		 * 根据传入的代号和类型从服务器上获取查询省市数据
		 **/
		
		private void queryFromServer(final String code,final String type){
			String address;
			if(!TextUtils.isEmpty(code)){
				address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
			}else{
				address = "http://www.weather.com.cn/data/list3/city.xml";
			}
			showProgressDialog();
			HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
				
				public void onFinish(String response) {
					boolean result = false;
					if("province".equals(type)){
						result = Utility.handleProvincesResponse(warmWeatherDB, response);
					}else if("city".equals(type)){
						result = Utility.handleCitiesResponse(warmWeatherDB, response, selectedProvince.getId());
					}else if("county".equals(type)){
						result = Utility.handleCountiesResponse(warmWeatherDB, response, selectedCity.getId());
					}
					if(result){
						runOnUiThread(new Runnable(){
							@Override
							public void run(){
								closeProgressDialog();
								if("province".equals(type)){
									queryProvinces();
								}else if("city".equals(type)){
									queryCities();
								}else if("county".equals(type)){
									queryCounties();
								}
							}
						});
					}
				}
				
				public void onError(Exception e) {
					runOnUiThread(new Runnable(){
						
						@Override
						public void run(){
							closeProgressDialog();
							Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
						}
					});
					
				}
			});
		}
		/**
		 * 显示进度对话框
		 * */
		private void showProgressDialog(){
			if(progressDialog == null){
				
				progressDialog = new ProgressDialog(this);
				progressDialog.setMessage("正在加载");
				progressDialog.setCanceledOnTouchOutside(false);
			}
			progressDialog.show();
		}
		
		/*
		 * 关闭进度对话框
		 * **/
		
		private void closeProgressDialog(){
			if(progressDialog!=null){
				progressDialog.dismiss();
			}
		}
		/**
		 * 捕获返回键，根据当前的级别来判断，此时应该返回市列表，省列表，还是直接退出
		 * */
		
		@Override
		public void onBackPressed(){
			if(currentLevel == LEVEL_COUNTY){
				queryCities();
			}else if(currentLevel == LEVEL_CITY){
				queryProvinces();
			}else{
				finish();
			}
		}
}
