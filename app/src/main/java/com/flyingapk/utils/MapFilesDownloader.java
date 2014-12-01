package com.flyingapk.utils;

public interface MapFilesDownloader {

    String RESPONSE = "response";
    String REQUEST = "request";

    interface Request {
        interface Params {
            String FILE = "file";
            String CHECKSUM_FILE = "checksum_file";
        }

        interface Command {
            int STOP_SERVICE = 1;
        }
    }

    interface Response {
        interface Params {
            String PROGRESS = "progress";
            String PATH_TO_APP = "path_to_app";
            String STATUS_DOWNLOADING = "status_downloading";
        }

        interface Command {
            int START_DOWNLOADING = 1;
            int STOP_DOWNLOADING = 2;
            int PROGRESS_DOWNLOADING = 3;
        }
    }

}
