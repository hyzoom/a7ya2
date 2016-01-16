package com.ishraq.a7ya2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hp on 08/01/2016.
 */
public class SearchResultsActivity extends Activity {
    private static final String TAG = "RecyclerViewExample";
    private RecyclerView mRecyclerView;
    private MyRecyclerAdapter adapter;
    AsyncTask<Void, Void, Void> mRegisterTask;

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    final String url = "http://shaheed.esy.es/search_subject.php?search_word=";//http://ensiab.netai.net/get_all_subjects.php
    private List<FeedItem> feedsList;
    private ProgressDialog pDialog;
    boolean b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        // Initialize recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        // Downloading data from below url
        //http://javatechig.com/?json=get_recent_posts&count=45

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            searchAsync(query);
        }

    }

    public void searchAsync(final String query) {
        mRegisterTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(SearchResultsActivity.this);
                pDialog.setMessage("جاري البحث  \n شكرا لصبرك");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                // Register on our server
                // On server creates a new user
                searchResult(query);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                if (b) {
                    b = false;
                    adapter = new MyRecyclerAdapter(SearchResultsActivity.this, feedsList);
                    mRecyclerView.setAdapter(adapter);

                } else {
                    b = false;
                    Toast.makeText(SearchResultsActivity.this, "لايوجد نتيجة بحث \n حاول مجددا بكلمات اخرى", Toast.LENGTH_LONG).show();
                }
                pDialog.dismiss();
                mRegisterTask = null;
            }
        };
        mRegisterTask.execute(null, null, null);
    }

    void searchResult(String query) {
        String randomUserURL = "http://shaheed.esy.es/search_subject.php";
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("search_word", query));

        JSONObject json = makeHttpRequest(randomUserURL, "GET", params);
        feedsList = new ArrayList<>();
        if (json != null) {
            try {
                JSONArray jsonArray = json
                        .getJSONArray("subjects");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject c = jsonArray.getJSONObject(i);

                    FeedItem item = new FeedItem();
                    item.setTitle(c.getString("title"));
                    item.setContent(c.getString("content"));
                    item.setThumbnail(c.getString("image_url"));
                    feedsList.add(item);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            b = true;
        } else {
            b = false;
        }

    }

    public static JSONObject makeHttpRequest(String url, String method,
                                             List<NameValuePair> params) {
        // Making HTTP request
        try {

            // check for request method
            if (method == "POST") {
                // request method is POST
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params));

                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();

            } else if (method == "GET") {
                // request method is GET
                DefaultHttpClient httpClient = new DefaultHttpClient();
                String paramString = URLEncodedUtils.format(params, "utf-8");
                url += "?" + paramString;
                HttpGet httpGet = new HttpGet(url);

                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
            json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;

    }
}
