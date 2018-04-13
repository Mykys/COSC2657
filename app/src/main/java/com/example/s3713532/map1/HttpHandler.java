package com.example.s3713532.map1;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by s3713532 on 4/13/18.
 */

public class HttpHandler {

    public static void main(String[] args) {
        //System.out.println(HttpHandler.get("http://bestlab.us:8080/places"));

        String json = HttpHandler.get("http://bestlab.us:8080/places");

        Gson gson = new Gson();
        Shop[] shops = gson.fromJson(json, Shop[].class);

        for (Shop shop : shops) {
            System.out.println(shop);
        }
    }

    public static String get(String urlStr) {

        try {
            URL url = new URL(urlStr);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            // BufferedReader is responsible for reading all the data
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;
            StringBuilder stringBuilder = new StringBuilder();

            while (true) {
                line = bufferedReader.readLine();
                if (line == null) break;
                stringBuilder.append(line);
            }
            return stringBuilder.toString();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
