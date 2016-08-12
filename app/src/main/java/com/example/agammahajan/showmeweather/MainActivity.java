package com.example.agammahajan.showmeweather;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        AdapterView.OnItemClickListener  {

    public ListView listView;
    public Spinner spinner ;
    private ArrayAdapter<String> adapter1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.list);
        spinner = (Spinner) findViewById(R.id.spinner);


        String[] values = new String[]{
        };

        List<String> data = new ArrayList<>(Arrays.asList(values));
        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data

        adapter1 = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, data);
        listView.setAdapter(adapter1);
        listView.setOnItemClickListener(this);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.Cities, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter2);
        spinner.setOnItemSelectedListener(this);



    }



    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

        String itemValue = (String) spinner.getItemAtPosition(position);

//        // Show Alert
//        Toast.makeText(getApplicationContext(),
//                "Position :" + position + "  City : " + itemValue, Toast.LENGTH_LONG)
//                .show();
        Fetch ft = new Fetch();
        ft.execute(itemValue);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {


        String itemValue = (String) listView.getItemAtPosition(position);

        // Show Alert
        Toast.makeText(getApplicationContext(),
                "Position :" + position + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                .show();
    }
















    class Fetch extends AsyncTask<String, Void, String[]> {

        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {


            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                String day;
                String description;

                JSONObject dayForecast = weatherArray.getJSONObject(i);

                GregorianCalendar gc = new GregorianCalendar();
                gc.add(GregorianCalendar.DATE, i);
                Date time = gc.getTime();
                SimpleDateFormat shortDateFormat = new SimpleDateFormat("EEE MMM dd");
                day = shortDateFormat.format(time);
                System.out.println(day);

                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getInt(OWM_MAX);
                double low = temperatureObject.getInt(OWM_MIN);

                resultStrs[i] = day + " - " + description + " - " + high+"/"+low;
            }


            return resultStrs;

        }
        @Override
        protected String[] doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;


            String forecastJsonStr = null;

            String format = "json";
            String units = "metric";
            int numDays = 14;
            String key="fa7fa10b738f5c0309a0c0be7b336e2c";

            try {

                final String FORECAST_BASE_URL =
                        "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "APPID";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM,key)
                        .build();

                URL url = new URL(builtUri.toString());




                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();


                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {

                    return null;
                }
                forecastJsonStr = buffer.toString();


            }
            catch (IOException e) {
                return null;
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                return getWeatherDataFromJson(forecastJsonStr, numDays);
            } catch (JSONException e) {

                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {

            if (result != null) {
                adapter1.clear();
                for(String dayForecastStr : result) {
                    System.out.println(dayForecastStr);
                    adapter1.add(dayForecastStr);
                }


            }


        }
    }

}






