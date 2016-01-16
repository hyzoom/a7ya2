package com.ishraq.a7ya2;

/**
 * Created by hp on 01/01/2016.
 */

import android.app.Dialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FeedListActivity extends AppCompatActivity {
    private static final String TAG = "RecyclerViewExample";
    private List<FeedItem> feedsList;
    private RecyclerView mRecyclerView;
    private MyRecyclerAdapter adapter;
    private ProgressBar progressBar;
    AsyncTask<Void, Void, Void> mRegisterTask;

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    final String url = "http://shaheed.esy.es/get_sub_categ.php?categ_id=1";//http://ensiab.netai.net/get_all_subjects.php
    SwipeRefreshLayout swipeView;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_list);

        // Initialize recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeView = (SwipeRefreshLayout) findViewById(R.id.swipe);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        dialog = new Dialog(FeedListActivity.this);
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new AsyncHttpTask().execute(url);
                swipeView.setRefreshing(false);
            }
        });

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        // Downloading data from below url
        //http://javatechig.com/?json=get_recent_posts&count=45
        new AsyncHttpTask().execute(url);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        //SearchView searchView =
        //       (SearchView) menu.findItem(R.id.search).getActionView();
        MenuItem mSearchMenuItem = menu.findItem(R.id.action_search);
        SearchView searchView =
                (SearchView) MenuItemCompat.getActionView(mSearchMenuItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(
                new ComponentName(getApplicationContext(),
                        SearchResultsActivity.class)));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addDialog();
            }
        });

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            shareText("بل أحياءٌ", "تطبيق بل أحياء");
        }
        return super.onOptionsItemSelected(item);
    }

    public void addDialog() {
        dialog.setContentView(R.layout.add_shaheed);
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        Display display = this.getWindowManager().getDefaultDisplay();
        window.setLayout(display.getWidth() * 5 / 6,
                display.getHeight() * 2 / 3);
        dialog.show();
    }

    public void shareText(String subject, String body) {
        Intent txtIntent = new Intent(Intent.ACTION_SEND);
        txtIntent.setType("text/plain");
        txtIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        txtIntent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(txtIntent, "Share"));
    }

    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            Integer result = 0;
            HttpURLConnection urlConnection;
            try {
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                int statusCode = urlConnection.getResponseCode();

                // 200 represents HTTP OK
                if (statusCode == 200) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    Log.e(">>>>> ", response + "");
                    String line;
                    while ((line = r.readLine()) != null) {
                        response.append(line);
                    }
                    parseResult(response.toString());
                    result = 1; // Successful
                } else {
                    result = 0; //"Failed to fetch data!";
                }
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
            return result; //"Failed to fetch data!";
        }

        @Override
        protected void onPostExecute(Integer result) {
            // Download complete. Let us update UI
            progressBar.setVisibility(View.GONE);

            if (result == 1) {
                adapter = new MyRecyclerAdapter(FeedListActivity.this, feedsList);
                mRecyclerView.setAdapter(adapter);
            } else {
                Toast.makeText(FeedListActivity.this, "حدثت مشكلة\n تأكد من اتصالك بالانترنت", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void parseResult(String result) {
        try {
            JSONObject response = new JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1));
            JSONArray posts = response.optJSONArray("subjects");
            feedsList = new ArrayList<>();

            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = posts.optJSONObject(i);
                FeedItem item = new FeedItem();
                item.setTitle(post.optString("title"));
                item.setContent(post.optString("content"));
                item.setThumbnail(post.optString("image_url"));

                feedsList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}