package org.fdroid.kekstore.net.bluetooth.httpish.headers;

import org.fdroid.kekstore.net.bluetooth.FileDetails;

public class ContentLengthHeader extends Header {

    @Override
    public String getName() {
        return "content-length";
    }

    public void handle(FileDetails details, String value) {
        details.setFileSize(Integer.parseInt(value));
    }

}