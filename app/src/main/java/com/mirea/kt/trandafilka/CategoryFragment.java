package com.mirea.kt.trandafilka;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class CategoryFragment extends Fragment {
    private static final String TAG = "CategoryFragment";
    private ListView lvRss;
    private ArrayList<String> titles;
    private ArrayList<String> descriptions;
    private ArrayList<String> links;
    private ArrayList<String> pubDates;
    private ArrayList<String> authors;
    private ArrayList<String> filteredTitles;
    private ArrayList<String> filteredDescriptions;

    private String selectedCategoryUrl;

    public static CategoryFragment newInstance(String categoryUrl) {
        CategoryFragment fragment = new CategoryFragment();
        Bundle args = new Bundle();
        args.putString("categoryUrl", categoryUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedCategoryUrl = getArguments().getString("categoryUrl");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View view = inflater.inflate(R.layout.fragment_category, container, false);
        lvRss = view.findViewById(R.id.lvRss);

        TextView textViewCategoryTitle = view.findViewById(R.id.textViewCategoryTitle);

        String categoryName = getCategoryName(selectedCategoryUrl);
        textViewCategoryTitle.setText(categoryName);

        titles = new ArrayList<>();
        links = new ArrayList<>();
        descriptions = new ArrayList<>();
        pubDates = new ArrayList<>();
        authors = new ArrayList<>();
        filteredTitles = new ArrayList<>();
        filteredDescriptions = new ArrayList<>();

        EditText searchEditText = view.findViewById(R.id.searchEditText);

        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String query = searchEditText.getText().toString();
                    filterNews(query);
                    return true;
                }
                return false;
            }
        });


        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = searchEditText.getText().toString();
                filterNews(query);
            }
        });

        new ProcessInBackground().execute();

        lvRss.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title = filteredTitles.get(position);
                String link = links.get(position);
                String pubDate = pubDates.get(position);
                String description = filteredDescriptions.get(position);
                openNewsDescription(title, pubDate, description, link);
            }
        });

        return view;
    }


    private String getCategoryName(String categoryUrl) {
        switch (categoryUrl) {
            case "https://news.rambler.ru/rss/moscow_city/":
                return "Новости Москвы";
            case "https://news.rambler.ru/rss/politics/":
                return "Политика";
            case "https://news.rambler.ru/rss/community/":
                return "Общество";
            case "https://news.rambler.ru/rss/incidents/":
                return "Происшествия";
            case "https://news.rambler.ru/rss/tech/":
                return "Наука и техника";
            default:
                return "";
        }
    }

    private InputStream getInputStream(URL url) {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error opening input stream: " + e.getMessage());
            return null;
        }
    }

    private class ProcessInBackground extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), "Loading", "Loading RSS feed... Please wait.", true, false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                URL url = new URL(selectedCategoryUrl);

                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);

                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(getInputStream(url), "UTF-8");

                boolean insideItem = false;
                String title = "";
                String description = "";
                String pubDate = "";
                String author = "";
                String link = "";

                titles.clear();
                descriptions.clear();
                links.clear();
                pubDates.clear();
                authors.clear();

                int eventType = xpp.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equalsIgnoreCase("item")) {
                            insideItem = true;
                        } else if (xpp.getName().equalsIgnoreCase("title")) {
                            if (insideItem) {
                                title = xpp.nextText();
                            }
                        } else if (xpp.getName().equalsIgnoreCase("description")) {
                            if (insideItem) {
                                description = xpp.nextText();
                            }
                        } else if (xpp.getName().equalsIgnoreCase("pubDate")) {
                            if (insideItem) {
                                pubDate = xpp.nextText();
                                pubDate = pubDate.substring(0, pubDate.indexOf("+0300"));
                            }
                        } else if (xpp.getName().equalsIgnoreCase("author")) {
                            if (insideItem) {
                                author = xpp.nextText();
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("link")) {
                            if (insideItem) {
                                link = xpp.nextText();
                            }
                        }
                    } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {
                        insideItem = false;
                        titles.add(title);
                        descriptions.add(description);
                        links.add(link);
                        pubDates.add(pubDate);
                        authors.add(author);
                    }

                    eventType = xpp.next();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing XML: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            filterNews("");
        }
    }

    private void filterNews(String query) {
        filteredTitles.clear();
        filteredDescriptions.clear();

        String[] searchWords = query.toLowerCase().split(" ");

        for (int i = 0; i < titles.size(); i++) {
            String title = titles.get(i);
            String description = descriptions.get(i);

            boolean matchesQuery = true;

            for (String word : searchWords) {
                if (!title.toLowerCase().contains(word) && !description.toLowerCase().contains(word)) {
                    matchesQuery = false;
                    break;
                }
            }

            if (matchesQuery) {
                filteredTitles.add(title);
                filteredDescriptions.add(description);
            }
        }

        populateListView();
    }


    private void populateListView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_news, filteredTitles) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.list_item_news, parent, false);
                }

                TextView textViewTitle = convertView.findViewById(R.id.textViewTitle);
                TextView textViewPubDate = convertView.findViewById(R.id.textViewPubDate);
                TextView textViewAuthor = convertView.findViewById(R.id.textViewAuthor);

                String title = getItem(position);
                String pubDate = pubDates.get(position);
                String author = authors.get(position);

                textViewTitle.setText(title);
                textViewPubDate.setText(pubDate);
                textViewAuthor.setText(author);

                return convertView;
            }
        };

        lvRss.setAdapter(adapter);
    }

    private void openNewsDescription(String title, String pubDate, String description, String link) {
        Log.d(TAG, "Opening news description: " + title);
        Intent intent = new Intent(getActivity(), NewsDescription.class);
        intent.putExtra("title", title);
        intent.putExtra("pubDate", pubDate);
        intent.putExtra("description", description);
        intent.putExtra("link", link);
        startActivity(intent);
    }
}