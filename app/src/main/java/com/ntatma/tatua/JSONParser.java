package com.ntatma.tatua;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by luqi on 7/7/17.
 */

public class JSONParser {

    private static  String TAG = "JSONParser";

    private InputStream inputStream = null;
    private JSONObject jsonObject = null;
    private JSONArray jsonArray = null;
    private String json = "";

    public JSONParser() {
    }

    public JSONObject getJSONFromUrl(String url,List<NameValuePair> params){
        // Making HTTP request
        Log.d(TAG, "Inside JSONParser::getJSONFromUrl()");
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            if (params != null){
                httpPost.setEntity(new UrlEncodedFormEntity(params));
                Log.d(TAG, "Inside JSONParser::getJSONFromUrl() setting params::"+ params);
                Log.d(TAG, "HttpPost is ::"+ httpPost.toString());
            }
            HttpResponse response = client.execute(httpPost);
            Log.d(TAG, "Inside JSONParser::getJSONFromUrl() after executing htttp past");
            HttpEntity entity = response.getEntity();
            inputStream = entity.getContent();
            Log.d(TAG, "InputStream content");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.d(TAG, "UnsupportedEncodingException");
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            Log.d(TAG, "ClientProtocolException");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "IOException");
        }

        try {
            Log.d(TAG, "Creating bufferedReader");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null){
                stringBuilder.append(line + "\n");
            }
            inputStream.close();
            json = stringBuilder.toString();
        } catch (IOException e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        return jsonObject;
    }

    public JSONObject getJSONArray(String url, List<NameValuePair> params){
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            if (params != null){
                httpGet.setParams((HttpParams) params);
                Log.d(TAG, "Inside JSONParser::getJSONArray() setting formparams");
            }
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            inputStream = httpEntity.getContent();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            inputStream.close();
            json = sb.toString();
            Log.e("JSS",json);
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
