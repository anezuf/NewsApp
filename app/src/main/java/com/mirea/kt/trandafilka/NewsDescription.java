package com.mirea.kt.trandafilka;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class NewsDescription extends AppCompatActivity {

    private static final String TAG = "NewsDescription";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_description);

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvPubDate = findViewById(R.id.tvPubDate);
        TextView tvDescription = findViewById(R.id.tvDescription);
        Button btnShare = findViewById(R.id.btnShare);

        String title = getIntent().getStringExtra("title");
        String pubDate = getIntent().getStringExtra("pubDate");
        String description = getIntent().getStringExtra("description");
        String link = getIntent().getStringExtra("link");

        tvTitle.setText(title);
        tvPubDate.setText(pubDate);
        tvDescription.setText(description);

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareNews(title, link);
            }
        });

        Log.d(TAG, "NewsDescription activity created");
    }

    private void shareNews(String title, String link) {
        String shareText = title + "\n\n" + link;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Поделиться новостью"));

        Log.d(TAG, "News shared: " + title);
    }
}

