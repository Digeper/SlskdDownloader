package org.muzika.slskddownload.kafkaMassages;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestRandomSong {

    private UUID songId;
    private String genre;

}
