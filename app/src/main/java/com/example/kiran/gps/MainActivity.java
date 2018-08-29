package com.example.kiran.gps;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.example.kiran.gps.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements LocationServiceDelegate {
    ActivityMainBinding activityMainBinding;
    ListAdapter adapter;

    List<String> citiesList = new ArrayList<>();
    private final WeatherService weatherService = new WeatherService(this);
    private LocationService locationService;
SearchView searchView;
    @BindView(R.id.pullToRefresh)
    SwipeRefreshLayout pullToRefresh;
    @BindView(R.id.city)
    TextView place;
    @BindView(R.id.day)
    TextView date;
    @BindView(R.id.celcius)
    TextView temp;
    @BindView(R.id.wind)
    TextView air;
    @BindView(R.id.humidity)
    TextView humid;
    @BindView(R.id.list_view)
    ListView listView;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        ButterKnife.bind(this);
        setupSearchBar();
        setupInitialUI();
        initScreenRefresh();
        locationService = new LocationService(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupLocationServices();
    }

    private void setupInitialUI() {
        showCurrentDate();
    }

    private void setupLocationServices() {
        if (locationService.requestReadLocationPermissionIfRequired()) {
            if (locationService.showGPSSettingDialogIfRequired()) {
                locationService.fetchLocation();
            }
        }
    }

    private void setupSearchBar() {
        activityMainBinding.searchBar.setIconifiedByDefault(true);
        activityMainBinding.searchBar.setMaxWidth(Integer.MAX_VALUE);
        activityMainBinding.searchBar.setFocusable(false);
        activityMainBinding.searchBar.setFocusableInTouchMode(true);
        activityMainBinding.searchBar.setQueryHint("Type your keyword here");
        setupCityList();
        adapter = new ListAdapter(citiesList);
        activityMainBinding.listView.setAdapter(adapter);
        activityMainBinding.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                listView.setVisibility(View.GONE);
                String city = adapter.mData.get(position);
                updateWeather(city);
            }
        });
        setupOnQueryTextListener();
    }

    @Override
    public void onBackPressed() {
        if (!activityMainBinding.searchBar.isIconified()) {
            activityMainBinding.searchBar.clearFocus();
            listView.setVisibility(View.INVISIBLE);
            activityMainBinding.searchBar.setIconified(true);
            return;
        }
        super.onBackPressed();
    }


    private void setupOnQueryTextListener() {
        activityMainBinding.searchBar.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listView.setVisibility(View.VISIBLE);
            }
        });

        activityMainBinding.searchBar.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                listView.setVisibility(View.GONE);
                return true;
            }
        });
        activityMainBinding.searchBar.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String Query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                listView.setVisibility(View.VISIBLE);
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    @SuppressWarnings("SpellCheckingInspection")
    private void setupCityList() {
        citiesList.add("Kolkata");
        citiesList.add("Bangalore");
        citiesList.add("Hyderabad");

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initScreenRefresh() {

        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                locationService.showGPSSettingDialogIfRequired();
                locationService.fetchLocation();
                updateWeather(locationService.latitude,locationService.longitude);
                pullToRefresh.setRefreshing(false);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationService.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void updateWeather(String city) {
        weatherService.findWeather(city, new WeatherService.MyCallBack() {
            @Override
            public void updateMyText(String city, String temperature, String wind, String humidity) {
                place.setText(city);
                humid.setText(humidity);
                air.setText(wind);
                temp.setText(temperature);
            }
        });
    }

    private void updateWeather(double latitude, double longitude) {
        weatherService.findWeather(latitude, longitude, new WeatherService.MyCallBack() {
            @Override
            public void updateMyText(String city, String temperature, String wind, String humidity) {
                place.setText(city);
                humid.setText(humidity);
                air.setText(wind);
                temp.setText(temperature);
            }
        });
    }

    private void showCurrentDate() {
        Date day = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, MMM dd", Locale.getDefault());
        String formattedDate = simpleDateFormat.format(day);
        date.setText(formattedDate);
    }

    @Override
    public void locationUpdated() {
        updateWeather(locationService.latitude, locationService.longitude);
    }

}




