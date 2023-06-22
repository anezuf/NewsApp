package com.mirea.kt.trandafilka;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class NewsList extends AppCompatActivity {

    private static final String TAG = "NewsList";

    private static final String[] categories = {
            "https://news.rambler.ru/rss/moscow_city/",
            "https://news.rambler.ru/rss/politics/",
            "https://news.rambler.ru/rss/community/",
            "https://news.rambler.ru/rss/incidents/",
            "https://news.rambler.ru/rss/tech/"
    };

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_list);

        viewPager = findViewById(R.id.viewPager);
        CategoryPagerAdapter pagerAdapter = new CategoryPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        Log.d(TAG, "NewsList activity created");
    }

    private class CategoryPagerAdapter extends FragmentPagerAdapter {

        public CategoryPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            String categoryUrl = categories[position];
            Log.d(TAG, "Creating CategoryFragment for category: " + categoryUrl);
            return CategoryFragment.newInstance(categoryUrl);
        }

        @Override
        public int getCount() {
            return categories.length;
        }
    }
}
