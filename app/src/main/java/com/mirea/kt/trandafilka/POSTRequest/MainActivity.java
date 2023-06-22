package com.mirea.kt.trandafilka.POSTRequest;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mirea.kt.trandafilka.NewsList;
import com.mirea.kt.trandafilka.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private EditText editTextLogin;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewResult;

    private String server = "https://android-for-students.ru";
    private String serverPath = "/coursework/login.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextLogin = findViewById(R.id.editTextLogin);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewResult = findViewById(R.id.textViewResult);

        buttonLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String login = editTextLogin.getText().toString();
                String password = editTextPassword.getText().toString();

                if (login.isEmpty()) {
                    editTextLogin.setError("Введите логин");
                    return;
                }

                if (password.isEmpty()) {
                    editTextPassword.setError("Введите пароль");
                    return;
                }

                if (isInternetAvailable()) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("lgn", login);
                    map.put("pwd", password);
                    map.put("g", "RIBO-02-21");

                    HTTPRunnable hTTPRunnable = new HTTPRunnable(server + serverPath, map);
                    Thread th = new Thread(hTTPRunnable);
                    th.start();

                    try {
                        th.join();
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    } finally {
                        String responseBody = hTTPRunnable.getResponseBody();

                        try {
                            JSONObject jSONObject = new JSONObject(responseBody);
                            int resultCode = jSONObject.getInt("result_code");
                            String title = jSONObject.getString("title");
                            String task = jSONObject.getString("task");

                            if (resultCode == 1) {
                                Intent actIntent = new Intent(getApplicationContext(), NewsList.class);
                                startActivity(actIntent);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            textViewResult.setText("Введите корректные данные");
                        }
                    }
                    textViewResult.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            textViewResult.setText("");
                        }
                    }, 1500);
                } else {
                    Toast.makeText(MainActivity.this, "Нет подключения к интернету", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isInternetAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
}
