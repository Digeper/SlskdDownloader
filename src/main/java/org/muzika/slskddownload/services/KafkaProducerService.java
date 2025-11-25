package org.muzika.slskddownload.services;


import org.muzika.slskddownload.kafkaMassages.LoadedSong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class KafkaProducerService {

    private final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired
    KafkaTemplate<UUID, LoadedSong> songKafkaTemplate;

    public void send(String topic, UUID uuid, LoadedSong song) {
        var future = songKafkaTemplate.send(topic, uuid, song);
        future.whenComplete((r, e) -> {
            if (e != null) {
                logger.error(e.getMessage());
                future.completeExceptionally(e);
            }else {
                logger.info(song.toString());
                future.complete(r);
            }
        });

    }
}
