package com.aditya.girnar.rssparsing.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aditya.girnar.rssparsing.R;
import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FirstActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = FirstActivity.class.getCanonicalName();
    private String finalUrl = "https://api.foxsports.com/v1/rss?partnerKey=zBaFxRyGKCfxBagJG9b8pqLyndmvo7UU&tag=soccer";
    ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private static int news_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        //adding ads...
//        AdView mAdView = (AdView) findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)        // All emulators
//                .addTestDevice("4E7DFB351E1B398953C546172ABE3816")  // An example device ID
//                .addTestDevice("6DC25D750C6C761EA59237250C4818FA")  // An example device ID
//                .build();
//
//        mAdView.loadAd(adRequest);

        loadData();
    }

    @Override
    public void onRefresh() {
//        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
//        List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(100);
//        for (ActivityManager.RunningServiceInfo oneService : services) {
//            Log.v(TAG, oneService.service.getClassName());
//        }
        loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_first, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void loadData() {
        news_count = 0;
        XMLAsyncTask mAsyncTask = new XMLAsyncTask();
        mAsyncTask.execute(finalUrl);
    }

    private class Data {
        String title, link, description, publishedDate, imageUrl;

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getPublishedDate() {
            return publishedDate;
        }

        public void setPublishedDate(String publishedDate) {
            this.publishedDate = publishedDate;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    private class XMLAsyncTask extends AsyncTask<String, Object, List<Data>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(List<Data> data) {
            super.onPostExecute(data);
//            Log.v(TAG, "new count = " + news_count);
            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setAdapter(new RecyclerListAdapter(data));
            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        protected List<Data> doInBackground(String[] params) {
            List<Data> data = new ArrayList<>();
            XmlPullParserFactory xmlFactoryObject;
            String urlString = params[0];
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                // Starts the query
                conn.connect();
                InputStream stream = conn.getInputStream();

                xmlFactoryObject = XmlPullParserFactory.newInstance();
                XmlPullParser myParser = xmlFactoryObject.newPullParser();

                myParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                myParser.setInput(stream, null);

                parseXMLAndStoreIt(myParser, data);
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;
        }
    }

    public void parseXMLAndStoreIt(XmlPullParser myParser, List<Data> data) {
        int event;
        String text = null;
        Data singleNewsData = null;
        try {
            event = myParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String name = myParser.getName();
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if (name.equals("item"))
                            singleNewsData = new Data();
                        else if (name.equals("enclosure")) {
//                            Log.v(TAG, "URL : " + myParser.getAttributeValue(null, "url"));
                            String originalUrl = myParser.getAttributeValue(null, "url");
                            String finalUrl = originalUrl.trim().replace(" ", "%20");
                            singleNewsData.setImageUrl(finalUrl);
                        }
                        break;
                    case XmlPullParser.TEXT:
                        text = myParser.getText().trim();
                        break;
                    case XmlPullParser.END_TAG:
                        if (name.equals("title") && singleNewsData != null) {
                            news_count++;
                            singleNewsData.setTitle(text);
                        } else if (name.equals("link") && singleNewsData != null) {
                            singleNewsData.setLink(text);
                        } else if (name.equals("description") && singleNewsData != null) {
                            singleNewsData.setDescription(text);
                        } else if (name.equals("pubDate") && singleNewsData != null) {
                            singleNewsData.setPublishedDate(text.subSequence(0, 16).toString());
                        } else if (name.equals("item") && singleNewsData != null) {
                            data.add(singleNewsData);
                        }
                        break;
                }
                event = myParser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_about:
                Toast.makeText(FirstActivity.this, "App Developed by Aditya Sharma", Toast.LENGTH_SHORT).show();
                break;
            case R.id.item_share_app_link:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Try this App for Soccer Maniacs\n http://play.google.com/store/apps/details?id=com.aditya.girnar.rssparsing");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class PersonalViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        TextView description;
        ImageView shareNews;
        TextView publishedDate;
        String link;
        SimpleDraweeView imageView;

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public PersonalViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            title = (TextView) itemView.findViewById(R.id.title);
            description = (TextView) itemView.findViewById(R.id.description);
            shareNews = (ImageView) itemView.findViewById(R.id.shareNews);
            imageView = (SimpleDraweeView) itemView.findViewById(R.id.imageView);
            publishedDate = (TextView) itemView.findViewById(R.id.published_date);
            title.setOnClickListener(this);
            description.setOnClickListener(this);
            imageView.setOnClickListener(this);
            shareNews.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.shareNews:
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, link);
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                    break;
                case R.id.title:
                case R.id.description:
                case R.id.imageView:
                    Intent fireIntent = new Intent(FirstActivity.this, SecondActivity.class);
                    fireIntent.putExtra(SecondActivity.URL_STRING, getLink());
                    startActivity(fireIntent);
                    break;
            }
        }
    }

    ControllerListener mDraweeListener = new BaseControllerListener<ImageInfo>() {
        @Override
        public void onFinalImageSet(
                String id,
                @Nullable ImageInfo imageInfo,
                @Nullable Animatable anim) {
            if (imageInfo == null) {
                return;
            }
            QualityInfo qualityInfo = imageInfo.getQualityInfo();
            FLog.d("Final image received! " +
                            "Size %d x %d",
                    "Quality level %d, good enough: %s, full quality: %s",
                    imageInfo.getWidth(),
                    imageInfo.getHeight(),
                    qualityInfo.getQuality(),
                    qualityInfo.isOfGoodEnoughQuality(),
                    qualityInfo.isOfFullQuality());
        }

        @Override
        public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {
            FLog.d("on intermediate", "listener on intermatedate...");
        }

        @Override
        public void onFailure(String id, Throwable throwable) {
            FLog.e(getClass(), throwable, "Error loading %s", id);
        }
    };

    private class RecyclerListAdapter extends RecyclerView.Adapter<PersonalViewHolder> {

        List<Data> newsList = null;

        public RecyclerListAdapter(List<Data> newsList) {
            this.newsList = newsList;
        }

        @Override
        public PersonalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            PersonalViewHolder personalViewHolder = new PersonalViewHolder(itemView);
            return personalViewHolder;
        }

        @Override
        public void onBindViewHolder(PersonalViewHolder holder, int position) {
            holder.title.setText(newsList.get(position).getTitle());
            holder.description.setText(newsList.get(position).getDescription());
            holder.setLink(newsList.get(position).getLink());
            holder.publishedDate.setText(newsList.get(position).getPublishedDate());
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.parse(newsList.get(position).getImageUrl()))
                    .setTapToRetryEnabled(true)
                    .setOldController(holder.imageView.getController())
                    .setControllerListener(mDraweeListener)
                    .build();
            holder.imageView.setController(controller);
            holder.imageView.setImageURI(Uri.parse(newsList.get(position).getImageUrl()));
        }

        @Override
        public int getItemCount() {
            return newsList.size();
        }
    }
}