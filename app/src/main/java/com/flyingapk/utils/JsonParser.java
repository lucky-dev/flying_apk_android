package com.flyingapk.utils;

import com.flyingapk.api.wrappers.ListAndroidAppsResponse;
import com.flyingapk.api.wrappers.ListBuildsResponse;
import com.flyingapk.api.wrappers.UserLogoutResponse;
import com.flyingapk.models.AndroidApp;
import com.flyingapk.api.wrappers.UserAuthorizationResponse;
import com.flyingapk.models.Build;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonParser {

    public UserAuthorizationResponse getUserAuthorizationResponse(int responseCode, String response) {
        UserAuthorizationResponse userAuthorizationResponse = new UserAuthorizationResponse(responseCode);

        userAuthorizationResponse.setCode(responseCode);

        if (response == null) {
            return userAuthorizationResponse;
        }

        try {
            JSONObject jRoot = new JSONObject(response);

            userAuthorizationResponse.setApiVersion(JsonUtil.getIntByKeyJson(jRoot, "api_version"));

            JSONObject jResponse = JsonUtil.getObjectByKeyJson(jRoot, "response");

            userAuthorizationResponse.setErrors(parseErrors(JsonUtil.getArrayByKeyJson(jResponse, "errors")));

            userAuthorizationResponse.setAccessToken(JsonUtil.getStringByKeyJson(jResponse, "access_token"));
        } catch (JSONException e) {
            return userAuthorizationResponse;
        }

        return userAuthorizationResponse;
    }

    public ListAndroidAppsResponse getListAndroidAppsResponse(int responseCode, String response) {
        ListAndroidAppsResponse listAndroidAppsResponse = new ListAndroidAppsResponse(responseCode);

        if (response == null) {
            return listAndroidAppsResponse;
        }

        try {
            JSONObject jRoot = new JSONObject(response);

            listAndroidAppsResponse.setApiVersion(JsonUtil.getIntByKeyJson(jRoot, "api_version"));

            JSONObject jResponse = JsonUtil.getObjectByKeyJson(jRoot, "response");

            JSONArray jAndroidApps = JsonUtil.getArrayByKeyJson(jResponse, "apps");
            if (jAndroidApps != null) {
                List<AndroidApp> listAndroidApps = new ArrayList<AndroidApp>();

                for (int i = 0; i < jAndroidApps.length(); i++) {
                    JSONObject jApp = jAndroidApps.getJSONObject(i);

                    AndroidApp app = new AndroidApp();
                    app.setId(JsonUtil.getIntByKeyJson(jApp, "id"));
                    app.setName(JsonUtil.getStringByKeyJson(jApp, "name"));
                    app.setDescription(JsonUtil.getStringByKeyJson(jApp, "description"));

                    listAndroidApps.add(app);
                }

                listAndroidAppsResponse.setListAndroidApps(listAndroidApps);
            }
        } catch (JSONException e) {
            return listAndroidAppsResponse;
        }

        return listAndroidAppsResponse;
    }

    public ListBuildsResponse getListBuildsResponse(int responseCode, String response) {
        ListBuildsResponse listBuildsResponse = new ListBuildsResponse(responseCode);

        if (response == null) {
            return listBuildsResponse;
        }

        try {
            JSONObject jRoot = new JSONObject(response);

            listBuildsResponse.setApiVersion(JsonUtil.getIntByKeyJson(jRoot, "api_version"));

            JSONObject jResponse = JsonUtil.getObjectByKeyJson(jRoot, "response");

            JSONArray jBuilds = JsonUtil.getArrayByKeyJson(jResponse, "builds");
            if (jBuilds != null) {
                List<Build> listBuilds = new ArrayList<Build>();

                for (int i = 0; i < jBuilds.length(); i++) {
                    JSONObject jBuild = jBuilds.getJSONObject(i);

                    Build build = new Build();
                    build.setId(JsonUtil.getIntByKeyJson(jBuild, "id"));
                    build.setName(JsonUtil.getStringByKeyJson(jBuild, "name"));
                    build.setVersion(JsonUtil.getStringByKeyJson(jBuild, "version"));
                    build.setFixes(JsonUtil.getStringByKeyJson(jBuild, "fixes"));
                    build.setType(JsonUtil.getStringByKeyJson(jBuild, "type"));
                    build.setCreatedTime(JsonUtil.getUtcDateByKeyJson(jBuild, "created_time", "yyyy-MM-dd HH:mm:ss"));
                    build.setFileName(JsonUtil.getStringByKeyJson(jBuild, "file_name"));
                    build.setFileChecksum(JsonUtil.getStringByKeyJson(jBuild, "file_checksum"));

                    listBuilds.add(build);
                }

                listBuildsResponse.setListBuilds(listBuilds);
            }
        } catch (JSONException e) {
            return listBuildsResponse;
        }

        return listBuildsResponse;
    }

    public UserLogoutResponse getUserLogoutResponse(int responseCode, String response) {
        UserLogoutResponse userLogoutResponse = new UserLogoutResponse(responseCode);

        userLogoutResponse.setCode(responseCode);

        if (response == null) {
            return userLogoutResponse;
        }

        try {
            JSONObject jRoot = new JSONObject(response);

            userLogoutResponse.setApiVersion(JsonUtil.getIntByKeyJson(jRoot, "api_version"));

            JSONObject jResponse = JsonUtil.getObjectByKeyJson(jRoot, "response");

            userLogoutResponse.setErrors(parseErrors(JsonUtil.getArrayByKeyJson(jResponse, "errors")));
        } catch (JSONException e) {
            return userLogoutResponse;
        }

        return userLogoutResponse;
    }

    private List<String> parseErrors(JSONArray errors) throws JSONException {
        List<String> listErrors = new ArrayList<String>();

        if (errors != null) {
            for (int i = 0; i < errors.length(); i++) {
                listErrors.add(errors.getString(i));
            }
        }

        return listErrors;
    }

}
