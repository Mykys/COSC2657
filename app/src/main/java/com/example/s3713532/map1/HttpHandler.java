package com.example.s3713532.map1;

import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by s3713532 on 4/13/18.
 */

public class HttpHandler {

    public static void main(String[] args) {
//        System.out.println(HttpHandler.get("http://bestlab.us:8080/places"));
//
//        String json = HttpHandler.get("http://bestlab.us:8080/places");
//
//        Gson gson = new Gson();
//        Shop[] shops = gson.fromJson(json, Shop[].class);
//
//        for (Shop shop : shops) {
//            System.out.println(shop.getName());
//        }

//        String json = {
//                "name": "Phuc Long",
//                "price": 10000,
//                "impression": "Excellent",
//                "address": "Nguyen Van Thap",
//                "lat": 10,
//                "lon": 110,
//                "style": "Modern",
//                "photo1": "",
//                "photo2": ""
//        };
//        "name": "rmit",
//                "price": 10000,
//                "impression": "Very good",
//                "address": "Nguyen Van Linh",
//                "lat": 10.7,
//                "lon": 106.7,
//                "style": "Retro",
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

    public static String post(String urlString, String postDataString) {

        // Jag tittade på https://stackoverflow.com/questions/40574892/how-to-send-post-request-with-x-www-form-urlencoded-body

        String data = "";
        HttpURLConnection urlConnection = null;

        try {
            byte[] postData = postDataString.getBytes( StandardCharsets.UTF_8 );
            int postDataLength = postData.length;
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("charset", "utf-8");
            urlConnection.setRequestProperty("Content-Length", Integer.toString( postDataLength ));
            urlConnection.setUseCaches(false);
            DataOutputStream wr = new DataOutputStream(urlConnection.getOutputStream());
            wr.write( postData );

            wr.flush();
            wr.close();

            InputStream in = urlConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(in);

            int inputStreamData = inputStreamReader.read();
            while (inputStreamData != -1) {
                char current = (char) inputStreamData;
                inputStreamData = inputStreamReader.read();
                data += current;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return data;
    }



}
