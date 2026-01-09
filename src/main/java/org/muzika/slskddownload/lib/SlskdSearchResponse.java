package org.muzika.slskddownload.lib;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class SlskdSearchResponse implements Comparable<SlskdSearchResponse> {
    public long fileCount;
    public ArrayList<File> files;
    public boolean hasFreeUploadSlot;
    public long lockedFileCount;
    public ArrayList<File> lockedFiles;
    public long queueLength;
    public long token;
    public long uploadSpeed;
    public String username;


    @Override
    public int compareTo(SlskdSearchResponse o) {
        if (fileCount == 0){
            if (o.fileCount == 0){
                return 0;
            }
            else {
                return -1;
            }
        }else{
            if (o.fileCount == 0){
                return 1;
            }
        }
        return Long.compare(this.uploadSpeed,o.uploadSpeed);
    }
    public long getLength() {
        return files.getFirst().size;
    }
    public String getFilePath() {
        return files.getFirst().filename;
    }

    public String getFilePathClean(){
        String filename = this.files.getFirst().filename;
        String[] paths =  filename.split("\\\\");
        return paths[paths.length-2]+"/"+paths[paths.length-1];

    }
}
class File{
    public int bitDepth;
    public long code;
    public String extension;
    public String filename;
    public long length;
    public long sampleRate;
    public long size;
    public boolean isLocked;
    public long bitRate;
    public boolean isVariableBitRate;
}

class LockedFile{
    public int bitDepth;
    public int code;
    public String extension;
    public String filename;
    public int length;
    public int sampleRate;
    public int size;
    public boolean isLocked;
}

