package com.flyingapk.utils;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.RequestBody;

public class RequestBuilder {

    public Headers getCommonHeader() {
        return new Headers.Builder()
                .add("Accept", "application/vnd.flyingapk; version=1")
                .build();
    }

    public Headers getHeaderWithToken(String accessToken) {
        return new Headers.Builder()
                .add("Accept", "application/vnd.flyingapk; version=1")
                .add("Authorization", accessToken)
                .build();
    }

    public RequestBody getAuthorizationRequest(String name, String email, String password) {
        FormEncodingBuilder body = new FormEncodingBuilder();

        if (name != null) {
            body.add("name", name);
        }

        if (email != null) {
            body.add("email", email);
        }

        if (password != null) {
            body.add("password", password);
        }

        return body.build();
    }

}
