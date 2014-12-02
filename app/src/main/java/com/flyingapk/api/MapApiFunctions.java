package com.flyingapk.api;

public interface MapApiFunctions {

    String REQUEST = "request";
    String RESPONSE = "response";

    interface Request {
        interface Params {
            String TAG_CALLER = "tag_caller";
            String NAME = "name";
            String EMAIL = "email";
            String PASSWORD = "password";
            String APP_ID = "app_id";
        }

        interface Command {
            int LOGIN = 1;
            int REGISTER = 2;
            int LIST_APPS = 3;
            int LIST_BUILDS = 4;
            int LOGOUT = 5;
        }
    }

    interface Response {
        interface Params {
            String TAG_CALLER = "tag_caller";
            String AUTHORIZATION_RESULT = "authorization_result";
            String LIST_APPS_RESULT = "list_apps_result";
            String LIST_BUILDS_RESULT = "list_builds_result";
            String LOGOUT_RESULT = "logout_result";
        }

        interface Command {
            int LOGIN = 1;
            int REGISTER = 2;
            int LIST_APPS = 3;
            int LIST_BUILDS = 4;
            int LOGOUT = 5;
        }
    }

}
