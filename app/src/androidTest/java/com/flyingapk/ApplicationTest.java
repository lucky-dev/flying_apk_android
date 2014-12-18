package com.flyingapk;

import android.test.ActivityTestCase;

import com.flyingapk.api.wrappers.ListAndroidAppsResponse;
import com.flyingapk.api.wrappers.ListBuildsResponse;
import com.flyingapk.models.AndroidApp;
import com.flyingapk.api.wrappers.UserAuthorizationResponse;
import com.flyingapk.models.Build;
import com.flyingapk.utils.JsonParser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ApplicationTest extends ActivityTestCase {

    private JsonParser mJsonParser;

    public void setUp() {
        mJsonParser = new JsonParser();
    }

    public void testParseLoginResponseWithoutErrors() {
        String json = "{ \"api_version\": 1, \"response\": { \"access_token\": \"xyz\" } }";

        UserAuthorizationResponse userAuthorizationResponse = mJsonParser.getUserAuthorizationResponse(200, json);
        assertEquals(userAuthorizationResponse.getAccessToken(), "xyz");
        assertEquals(userAuthorizationResponse.getApiVersion(), 1);
        assertEquals(userAuthorizationResponse.getCode(), 200);
    }

    public void testParseLoginResponseWithErrors() {
        String json = "{ \"api_version\": 1, \"response\": { \"errors\": [ \"password is wrong\" ] } }";

        UserAuthorizationResponse userAuthorizationResponse = mJsonParser.getUserAuthorizationResponse(500, json);
        assertEquals(userAuthorizationResponse.getApiVersion(), 1);
        assertEquals(userAuthorizationResponse.getCode(), 500);
        assertEquals(userAuthorizationResponse.getErrors().get(0), "password is wrong");
    }

    public void testParseListAndroidAppsResponseWithoutErrors() {
        String json = "{ \"api_version\": 1, \"response\": { \"apps\":" +
                    "[" +
                        "{ \"id\": 1, \"name\": \"My App 1\", \"description\": \"My Cool App 1\" }," +
                        "{ \"id\": 2, \"name\": \"My App 2\", \"description\": \"My Cool App 2\" }" +
                    "] } }";

        ListAndroidAppsResponse androidAppResponse = mJsonParser.getListAndroidAppsResponse(200, json);
        assertEquals(androidAppResponse.getApiVersion(), 1);
        assertEquals(androidAppResponse.getCode(), 200);

        List<AndroidApp> listAndroidApps = androidAppResponse.getListAndroidApps();

        assertEquals(listAndroidApps.get(0).getId(), 1);
        assertEquals(listAndroidApps.get(0).getName(), "My App 1");
        assertEquals(listAndroidApps.get(0).getDescription(), "My Cool App 1");

        assertEquals(listAndroidApps.get(1).getId(), 2);
        assertEquals(listAndroidApps.get(1).getName(), "My App 2");
        assertEquals(listAndroidApps.get(1).getDescription(), "My Cool App 2");
    }

    public void testParseAndroidAppsResponseWithErrors() {
        String json = "";

        ListAndroidAppsResponse userAuthorizationResponse = mJsonParser.getListAndroidAppsResponse(500, json);
        assertEquals(userAuthorizationResponse.getApiVersion(), 0);
        assertEquals(userAuthorizationResponse.getCode(), 500);
        assertEquals(userAuthorizationResponse.getListAndroidApps().size(), 0);
    }

    public void testParseListBuildsResponseWithoutErrors() {
        String json = "{ \"api_version\": 1, \"response\": { \"builds\":" +
                "[" +
                "{ \"id\": 1, \"name\": \"Build #2\", \"version\": \"1.0\", \"fixes\": \"All bugs were fixes\", \"type\": \"debug\", \"created_time\": \"2014-11-24 11:40:39.557332\", \"file_name\": \"f0f34567b7f324f1d88f901d449d6c75.apk\", \"file_checksum\": \"ea6e9d41130509444421709610432ee1\" }," +
                "{ \"id\": 2, \"name\": \"Build #1\", \"version\": \"2.0\", \"fixes\": \"All bugs were fixes\", \"type\": \"release\", \"created_time\": \"2014-11-24 11:45:50.557332\", \"file_name\": \"0d92b2ef44c41ea5264d7615d5d270ff.apk\", \"file_checksum\": \"ea6e9d41130509444421709610432ee2\" }" +
                "] } }";

        ListBuildsResponse listBuildsResponse = mJsonParser.getListBuildsResponse(200, json);
        assertEquals(listBuildsResponse.getApiVersion(), 1);
        assertEquals(listBuildsResponse.getCode(), 200);

        List<Build> listBuilds = listBuildsResponse.getListBuilds();

        assertEquals(listBuilds.get(0).getId(), 1);
        assertEquals(listBuilds.get(0).getName(), "Build #2");
        assertEquals(listBuilds.get(0).getVersion(), "1.0");
        assertEquals(listBuilds.get(0).getFixes(), "All bugs were fixes");
        assertEquals(listBuilds.get(0).getType(), "debug");
        assertEquals(convertDateToFormatString(listBuilds.get(0).getCreatedTime(), "yyyy-MM-dd HH:mm:ss"), "2014-11-24 13:40:39");
        assertEquals(listBuilds.get(0).getFileName(), "f0f34567b7f324f1d88f901d449d6c75.apk");
        assertEquals(listBuilds.get(0).getFileChecksum(), "ea6e9d41130509444421709610432ee1");

        assertEquals(listBuilds.get(1).getId(), 2);
        assertEquals(listBuilds.get(1).getName(), "Build #1");
        assertEquals(listBuilds.get(1).getVersion(), "2.0");
        assertEquals(listBuilds.get(1).getFixes(), "All bugs were fixes");
        assertEquals(listBuilds.get(1).getType(), "release");
        assertEquals(convertDateToFormatString(listBuilds.get(1).getCreatedTime(), "yyyy-MM-dd HH:mm:ss"), "2014-11-24 13:45:50");
        assertEquals(listBuilds.get(1).getFileName(), "0d92b2ef44c41ea5264d7615d5d270ff.apk");
        assertEquals(listBuilds.get(1).getFileChecksum(), "ea6e9d41130509444421709610432ee2");
    }

    public void testParseListBuildsResponseWithErrors() {
        String json = "";

        ListBuildsResponse listBuildsResponse = mJsonParser.getListBuildsResponse(500, json);
        assertEquals(listBuildsResponse.getApiVersion(), 0);
        assertEquals(listBuildsResponse.getCode(), 500);
        assertEquals(listBuildsResponse.getListBuilds().size(), 0);
    }

    private String convertDateToFormatString(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

}
