package dhruv.example.redditdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import dhruv.example.redditdemo.adapter.List_Adapter;
import dhruv.example.redditdemo.model.Reddit;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";

    /*jsonItem array*/
    String[] spinItem = {"AndroidGaming", "Gaming", "Androiddev", "Androidapps", "root_android"};

    /*initData*/
    Spinner spinner;
    ListView listView;
    TextView textView;
    LinearLayout llProgress;

    /*arrayList*/
    List<Reddit> redditList;

    /*global var filename*/
    String filename = "AndroidGaming.txt";

    /*global var url*/
    String  url;

    /*jsonUrl*/
    private static final String urlAndroidGaming = "https://www.reddit.com/r/AndroidGaming/hot.json";
    private static final String urlGaming = "https://www.reddit.com/r/Gaming/hot.json";
    private static final String urlAndroidDev = "https://www.reddit.com/r/androiddev/hot.json";
    private static final String urlAndroidApps = "https://www.reddit.com/r/androidapps/hot.json";
    private static final String urlRoot_Android = "https://www.reddit.com/r/root_android/hot.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        redditList = new ArrayList<>();

        initView();
        spin_Item();
        setupClick();
    }

    public void initView() {
        spinner = findViewById(R.id.spinner);
        listView = findViewById(R.id.list);
        textView = findViewById(R.id.tv);
        llProgress = findViewById(R.id.llProgress);
    }

    public void spin_Item() {
        ArrayAdapter adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_spinner_item, spinItem);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void setupClick() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String urlSpin = urlAndroidGaming;

                switch (spinItem[position]) {
                    case "AndroidGaming":
                        filename = "AndroidGaming.txt";
                        urlSpin = urlAndroidGaming;
                        break;

                    case "Gaming":
                        filename = "Gaming.txt";
                        urlSpin = urlGaming;
                        break;

                    case "Androiddev":
                        filename = "Androiddev.txt";
                        urlSpin = urlAndroidDev;
                        break;

                    case "Androidapps":
                        filename = "Androidapps.txt";
                        urlSpin = urlAndroidApps;
                        break;

                    case "root_android":
                        filename = "root_android.txt";
                        urlSpin = urlRoot_Android;
                        break;

                    default:
                }
                /*manage permission & load jsonData */
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Permission Granted ");
                    Toast.makeText(getApplicationContext(), spinItem[position], Toast.LENGTH_SHORT).show();

                    if (isNetworkAvailable()) {
                        textView.setVisibility(View.GONE);
                        loadUrl(urlSpin);
                    } else {
                        boolean isFilePresent = isFilePresent(getApplicationContext(), filename);

                        if (isFilePresent) {
                            textView.setVisibility(View.GONE);
                            redditList.clear();
                            List_Adapter adapter = new List_Adapter(redditList, getApplicationContext());
                            listView.setAdapter(adapter);
                            jsonResponse(readFromFile(MainActivity.this));
                        } else {
                            redditList.clear();
                            List_Adapter adapter = new List_Adapter(redditList, getApplicationContext());
                            listView.setAdapter(adapter);
                            textView.setVisibility(View.VISIBLE);
                            Toast.makeText(MainActivity.this, "File isn't available", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    url=urlSpin;
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(MainActivity.this, "Please select", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*manage jsonResponse & load data*/
    public void loadUrl(String url) {
        llProgress.setVisibility(View.VISIBLE);
        Log.e(TAG, "Url-->" + url);
        redditList.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        llProgress.setVisibility(View.GONE);
                        jsonResponse(response);
                        writeToFile(response, MainActivity.this);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error -->" + error.getMessage());
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    /*parse jsonResponse*/
    private void jsonResponse(String response) {
        try {
            JSONObject object = new JSONObject(response);
            JSONArray array = object.getJSONObject("data").getJSONArray("children");

            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i).getJSONObject("data");

                Reddit reddit = new Reddit(jsonObject.getInt("score"),
                        jsonObject.getString("author"),
                        jsonObject.getString("title"));
                redditList.add(reddit);
            }

            List_Adapter adapter = new List_Adapter(redditList, getApplicationContext());
            listView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Response Exception --> " + e.getMessage());
        }
    }

    /*check file is available or  not in storage */
    public boolean isFilePresent(Context context, String fileName) {
        String filePath = context.getFilesDir().getAbsolutePath() + "/" + fileName;
        File file = new File(filePath);
        return file.exists();
    }

    /*save file into storage*/
    private void writeToFile(String data, Context context) {
        try {
            String filePath = context.getFilesDir().getAbsolutePath() + "/" + filename;
            Log.e(TAG, "filePath --> " + filePath);
            File file = new File(filePath);
            file.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(data);
            outputStreamWriter.close();

            Log.e(TAG, "File Successfully download");
        } catch (IOException e) {
            Log.e(TAG, "File write failed: -->  " + e.toString());
            Toast.makeText(getApplicationContext(), "File write failed", Toast.LENGTH_LONG).show();
        }
    }

    /*read file from storage*/
    private String readFromFile(Context context) {

        String ret = "";

        try {
            String filePath = context.getFilesDir().getAbsolutePath() + "/" + filename;
            InputStream inputStream = new FileInputStream(new File(filePath));

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: --> " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: --> " + e.toString());
        }
        return ret;
    }

    /*check network is On/Off*/
    private boolean isNetworkAvailable() {
        ConnectivityManager manager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            // Network is present and connected
            isAvailable = true;
            Log.e(TAG, "deviceStatus --> " + isAvailable);
        } else {
            Log.e(TAG, "deviceStatus --> " + isAvailable);
            Toast.makeText(getApplicationContext(), "You are Offline now", Toast.LENGTH_SHORT).show();
        }
        return isAvailable;
    }

    /*manage permission result*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==200){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if (isNetworkAvailable()) {
                    loadUrl(url);
                } else {
                    boolean isFilePresent = isFilePresent(getApplicationContext(), filename);

                    if (isFilePresent) {
                        jsonResponse(readFromFile(MainActivity.this));
                    } else {
                        Toast.makeText(MainActivity.this, "File isn't available", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}
