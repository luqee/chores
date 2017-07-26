package com.ntatma.tatua;
//
//import android.util.Log;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.params.HttpParams;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.UnsupportedEncodingException;
//import java.util.List;
//
///**
// * Created by luqi on 7/7/17.
// */
//
//public class JSONParser {
//
//    private static  String TAG = "JSONParser";
//
//    private InputStream inputStream = null;
//    private JSONObject jsonObject = null;
//    private JSONArray jsonArray = null;
//    private String json = "";
//
//    public JSONParser() {
//    }
//
//    public JSONObject getJSONFromUrl(String url,List<NameValuePair> params){
//        // Making HTTP request
//        Log.d(TAG, "Inside JSONParser::getJSONFromUrl()");
//        try {
//            DefaultHttpClient client = new DefaultHttpClient();
//            HttpPost httpPost = new HttpPost(url);
//            if (params != null){
//                httpPost.setEntity(new UrlEncodedFormEntity(params));
//                Log.d(TAG, "Inside JSONParser::getJSONFromUrl() setting params::"+ params);
//                Log.d(TAG, "HttpPost is ::"+ httpPost.toString());
//            }
//            HttpResponse response = client.execute(httpPost);
//            Log.d(TAG, "Inside JSONParser::getJSONFromUrl() after executing htttp past");
//            HttpEntity entity = response.getEntity();
//            inputStream = entity.getContent();
//            Log.d(TAG, "InputStream content");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//            Log.d(TAG, "UnsupportedEncodingException");
//        } catch (ClientProtocolException e) {
//            e.printStackTrace();
//            Log.d(TAG, "ClientProtocolException");
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.d(TAG, "IOException");
//        }
//
//        try {
//            Log.d(TAG, "Creating bufferedReader");
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
//            StringBuilder stringBuilder = new StringBuilder();
//            String line = null;
//            while ((line = reader.readLine()) != null){
//                stringBuilder.append(line + "\n");
//            }
//            inputStream.close();
//            json = stringBuilder.toString();
//        } catch (IOException e) {
//            Log.e("Buffer Error", "Error converting result " + e.toString());
//        }
//
//        try {
//            jsonObject = new JSONObject(json);
//        } catch (JSONException e) {
//            Log.e("JSON Parser", "Error parsing data " + e.toString());
//        }
//
//        return jsonObject;
//    }
//
//    public JSONObject getJSONArray(String url, List<NameValuePair> params){
//        try {
//            // defaultHttpClient
//            DefaultHttpClient httpClient = new DefaultHttpClient();
//            HttpGet httpGet = new HttpGet(url);
//            if (params != null){
//                httpGet.setParams((HttpParams) params);
//                Log.d(TAG, "Inside JSONParser::getJSONArray() setting formparams");
//            }
//            HttpResponse httpResponse = httpClient.execute(httpGet);
//            HttpEntity httpEntity = httpResponse.getEntity();
//            inputStream = httpEntity.getContent();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (ClientProtocolException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"), 8);
//            StringBuilder sb = new StringBuilder();
//            String line = null;
//            while ((line = reader.readLine()) != null) {
//                sb.append(line + "\n");
//            }
//            inputStream.close();
//            json = sb.toString();
//            Log.e("JSS",json);
//        } catch (Exception e) {
//            Log.e("Buffer Error", "Error converting result " + e.toString());
//        }
//
//        try {
//            jsonObject = new JSONObject(json);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return jsonObject;
//    }
//}
////////////////////////////////////////////////


import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class JSONParser {

    String charset = "UTF-8";
    HttpURLConnection conn;
    DataOutputStream wr;
    StringBuilder result;
    URL urlObj;
    JSONObject jObj = null;
    StringBuilder sbParams;
    String paramsString;

    public JSONObject makeHttpRequest(String url, String method,
                                      HashMap<String, String> params) {

        sbParams = new StringBuilder();
        int i = 0;
        for (String key : params.keySet()) {
            try {
                if (i != 0){
                    sbParams.append("&");
                }
                sbParams.append(key).append("=")
                        .append(URLEncoder.encode(params.get(key), charset));

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            i++;
        }

        if (method.equals("POST")) {
            // request method is POST
            try {
                urlObj = new URL(url);

                conn = (HttpURLConnection) urlObj.openConnection();

                conn.setDoOutput(true);

                conn.setRequestMethod("POST");

                conn.setRequestProperty("Accept-Charset", charset);

                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);

                conn.connect();

                paramsString = sbParams.toString();

                wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(paramsString);
                wr.flush();
                wr.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(method.equals("GET")){
            // request method is GET

            if (sbParams.length() != 0) {
                url += "?" + sbParams.toString();
            }

            try {
                urlObj = new URL(url);

                conn = (HttpURLConnection) urlObj.openConnection();

                conn.setDoOutput(false);

                conn.setRequestMethod("GET");

                conn.setRequestProperty("Accept-Charset", charset);

                conn.setConnectTimeout(15000);

                conn.connect();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        Log.d("JSONPARSER: ", "request string is : \n"+urlObj);

        try {
            //Receive the response from the server
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            Log.d("JSON Parser", "result: " + result.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

        conn.disconnect();

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(result.toString());
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON Object
        return jObj;
    }
}