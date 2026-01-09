package org.muzika.slskddownload.kafkaMassages;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.muzika.slskddownload.lib.Song;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoadedSong {


    private UUID uuid;

    private String filePath;
    private Status status;

    public LoadedSong(Song song,UUID uuid, Status status) {
        this.uuid = uuid;
        this.filePath = song.cleanFilepath;
        this.status = status;

    }



    public enum Status {
        COMPLETED,
        ERROR
    }

}
