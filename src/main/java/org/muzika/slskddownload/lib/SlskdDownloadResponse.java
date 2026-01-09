package org.muzika.slskddownload.lib;

import java.util.ArrayList;
import java.util.Date;

public class SlskdDownloadResponse {
    public Date endedAt;
    public int fileCount;
    public String id;
    public boolean isComplete;
    public int lockedFileCount;
    public int responseCount;
    public ArrayList<Object> responses;
    public String searchText;
    public Date startedAt;
    public String state;
    public int token;
}

