package com.flyingapk.utils;

public interface MapUpdatingManager {

    String RESPONSE = "response";
    String REQUEST = "request";

    interface Request {
        interface Params {
            String FILE = "file";
            String CHECKSUM_FILE = "checksum_file";
        }

        interface Command {
            int STOP_SERVICE = 1;
            int CHECK_NEW_VERSION_APP = 2;
            int UPDATE_APP = 3;
        }
    }

    interface Response {
        interface Params {
            String PROGRESS = "progress";
            String PATH_TO_APP = "path_to_app";
            String STATUS_UPDATING = "status_downloading";
            String IS_NEW_VERSION_APP = "is_new_version_app";
            String VERSION_APP = "version_app";
            String FILE = "file";
            String CHECKSUM_FILE = "checksum_file";
            String WHATS_NEW = "whats_new";
        }

        interface Command {
            int START_UPDATING = 1;
            int STOP_UPDATING = 2;
            int PROGRESS_UPDATING = 3;
            int START_CHECKING_NEW_VERSION_APP = 4;
            int STOP_CHECKING_NEW_VERSION_APP = 5;
        }
    }

}
