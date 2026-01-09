package org.muzika.slskddownload.services;


import lombok.extern.slf4j.Slf4j;
import org.muzika.slskddownload.lib.SlskdDownloadResponse;
import org.muzika.slskddownload.lib.SlskdSearchResponse;
import org.muzika.slskddownload.lib.Song;
import org.muzika.slskddownload.SlskdSearcher;
import org.muzika.slskddownload.kafkaMassages.LoadedSong;
import org.muzika.slskddownload.kafkaMassages.RequestSlskdSong;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.Executor;

@Component
@Slf4j
public class KafkaConsumerService {

    private SlskdSearcher sl;

    private KafkaProducerService  kafkaProducer;

    private Executor downloadTaskExecutor;

    public  KafkaConsumerService(SlskdSearcher sl, KafkaProducerService kafkaProducerService, 
                                  @Qualifier("downloadTaskExecutor") Executor downloadTaskExecutor) {
        this.sl = sl;
        this.kafkaProducer = kafkaProducerService;
        this.downloadTaskExecutor = downloadTaskExecutor;
    }

    @KafkaListener(topics = {"request-slskd-song"} , groupId = "slskd-download-group",containerFactory = "songConcurrentKafkaListenerContainerFactory")
    public void consumeRequestSong(RequestSlskdSong requestSlskdSong) {
        log.info("Received request song: {}",requestSlskdSong);

        Song song = new Song(requestSlskdSong.getTitle(), requestSlskdSong.getArtist());
        UUID id = requestSlskdSong.getId();
        
        try {
            // Submit LoadSong task to thread pool executor for concurrent processing
            downloadTaskExecutor.execute(() -> {
                log.info("Starting download task for song: {} (ID: {})", song, id);
                LoadSong(song, id);
            });
        } catch (Exception e) {
            log.error("Failed to submit download task for song: {} (ID: {})", song, id, e);
            // Send error response if we can't even submit the task
            kafkaProducer.send("loaded-song", UUID.randomUUID(), new LoadedSong(song, id, LoadedSong.Status.ERROR));
        }
    }

    private Boolean LoadSong(Song song, UUID id) {
        SlskdDownloadResponse slskdDownloadResponse = null;
        try {
            slskdDownloadResponse = sl.search(song);
            SlskdSearchResponse res = sl.getSearch(slskdDownloadResponse.id);
            song.addSlsk(res);

            load(song, id, slskdDownloadResponse, res);

        } catch (Exception e) {
            log.error("Error while loading song: {} (ID: {})", song, id, e);
            // Clean up search if it was created
            if (slskdDownloadResponse != null && slskdDownloadResponse.id != null) {
                try {
                    sl.removeSearch(slskdDownloadResponse.id);
                } catch (Exception cleanupException) {
                    log.warn("Failed to remove search {} during error cleanup", slskdDownloadResponse.id, cleanupException);
                }
            }
            // Send error response
            try {
                kafkaProducer.send("loaded-song", UUID.randomUUID(), new LoadedSong(song, id, LoadedSong.Status.ERROR));
            } catch (Exception sendException) {
                log.error("Failed to send error response for song: {} (ID: {})", song, id, sendException);
            }
        }
        return null;
    }

    private boolean load(Song song, UUID id, SlskdDownloadResponse slskdDownloadResponse, SlskdSearchResponse res) {
        try {
            sl.requestDownload(song, slskdDownloadResponse.id);
            song.filePath = res.getFilePathClean();

            sl.removeSearch(slskdDownloadResponse.id);

            kafkaProducer.send("loaded-song", UUID.randomUUID(), new LoadedSong(song, id, LoadedSong.Status.COMPLETED));
            log.info("Successfully completed download for song: {} (ID: {})", song, id);
            return true;
        } catch (Exception e) {
            log.error("Error during download for song: {} (ID: {})", song, id, e);
            // Clean up search
            try {
                sl.removeSearch(slskdDownloadResponse.id);
            } catch (Exception cleanupException) {
                log.warn("Failed to remove search {} during error cleanup", slskdDownloadResponse.id, cleanupException);
            }
            // Send error response
            try {
                kafkaProducer.send("loaded-song", UUID.randomUUID(), new LoadedSong(song, id, LoadedSong.Status.ERROR));
            } catch (Exception sendException) {
                log.error("Failed to send error response for song: {} (ID: {})", song, id, sendException);
            }
            throw e; // Re-throw to be caught by LoadSong's catch block
        }
    }
}
