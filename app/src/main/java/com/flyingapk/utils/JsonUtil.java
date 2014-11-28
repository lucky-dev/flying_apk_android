package com.flyingapk.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class JsonUtil {

    public static String getStringByKeyJson(JSONObject json, String key) throws JSONException {
        if (json != null) {
            return (json.isNull(key) ? null : json.getString(key));
        } else {
            return null;
        }
    }

    public static int getIntByKeyJson(JSONObject json, String key) throws JSONException {
        if (json != null) {
            return (json.isNull(key) ? 0 : json.getInt(key));
        } else {
            return 0;
        }
    }

    public static double getDoubleByKeyJson(JSONObject json, String key) throws JSONException {
        if (json != null) {
            return (json.isNull(key) ? 0.0 : json.getDouble(key));
        } else {
            return 0.0;
        }
    }

    public static JSONObject getObjectByKeyJson(JSONObject json, String key) throws JSONException {
        if (json != null) {
            return (json.isNull(key) ? null : json.getJSONObject(key));
        } else {
            return null;
        }
    }

    public static JSONArray getArrayByKeyJson(JSONObject json, String key) throws JSONException {
        if (json != null) {
            return (json.isNull(key) ? null : json.getJSONArray(key));
        } else {
            return null;
        }
    }

    public static boolean getBooleanByKeyJson(JSONObject json, String key) throws JSONException {
        if (json != null) {
            return (!json.isNull(key) && json.getBoolean(key));
        } else {
            return false;
        }
    }

    public static Date getDateByKeyJson(JSONObject json, String key, String format) throws JSONException {
        Date date = null;

        try {
            String dateAsString = getStringByKeyJson(json, key);
            if (dateAsString != null) {
                date = new SimpleDateFormat(format).parse(dateAsString);
            } else {
                date = new Date();
            }
        } catch (ParseException e) {
            date = new Date();
        }

        return date;
    }

    public static Date getUtcDateByKeyJson(JSONObject json, String key, String format) throws JSONException {
        Date date = null;

        try {
            String dateAsString = getStringByKeyJson(json, key);
            if (dateAsString != null) {
                SimpleDateFormat dsf = new SimpleDateFormat(format);
                dsf.setTimeZone(TimeZone.getTimeZone("UTC"));
                date = dsf.parse(dateAsString);
            } else {
                date = new Date();
            }
        } catch (ParseException e) {
            date = new Date();
        }

        return date;
    }

    private JsonUtil() {
    }

}
