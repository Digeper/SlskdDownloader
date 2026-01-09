package org.muzika.slskddownload.lib;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

class Directory{
    public String directory;
    public int fileCount;
    public ArrayList<File1> files;
}

class File1{
    public String id;
    public String username;
    public String direction;
    public String filename;
    public int size;
    public int startOffset;
    public String state;
    public String stateDescription;
    public Date requestedAt;
    public Date endedAt;
    public int bytesTransferred;
    public double averageSpeed;
    public String exception;
    public int bytesRemaining;
    public int percentComplete;
    public Date enqueuedAt;
    public Date startedAt;
    public String elapsedTime;
    public String remainingTime;
}

public class SlskdDownloads{
    public String username;
    public ArrayList<Directory> directories;

    public boolean isFinished(String filename){
        String dirName = filename.substring(0, filename.lastIndexOf("\\"));
        for(Directory d : directories){
            if (d.directory.equals(dirName)){
                for(File1 f : d.files){
                    if (f.filename.equals(filename)){
                        if (Objects.equals(f.state, "Completed, Succeeded")){
                            return true;
                        }
                    }
                }
            }
        }
        return false;

    }
}

