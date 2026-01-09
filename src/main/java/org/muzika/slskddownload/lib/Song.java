package org.muzika.slskddownload.lib;

public class Song {

    public String songName;
    public String artistName;
    public Long size;
    public String fileName;
    public String username;
    public String filePath;
    public String cleanFilepath;

    public Song(String songName, String artistName){
        this.songName = songName;
        this.artistName = artistName;

    }

    public void addSlsk(SlskdSearchResponse res) {
        this.size = res.getLength();
        this.username = res.username;
        this.fileName = res.getFilePath();
        this.cleanFilepath = res.getFilePathClean();
    }
}

