package com.team420.kekstore.nearby.httpish;

public class ETagHeader extends Header {

    @Override
    public String getName() {
        return "etag";
    }

    public void handle(FileDetails details, String value) {
        details.setCacheTag(value);
    }

}
