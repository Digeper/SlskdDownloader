package org.muzika.slskddownload.services;


import lombok.extern.slf4j.Slf4j;
import org.muzika.SlskdDownloadResponse;
import org.muzika.SlskdSearchResponse;
import org.muzika.Song;
import org.muzika.slskddownload.SlskdSearcher;
import org.muzika.slskddownload.kafkaMassages.LoadedSong;
import org.muzika.slskddownload.kafkaMassages.RequestSlskdSong;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class KafkaConsumerService {

    private SlskdSearcher sl;

    private KafkaProducerService  kafkaProducer;

    public  KafkaConsumerService(SlskdSearcher sl, KafkaProducerService kafkaProducerService) {
        this.sl = sl;
        this.kafkaProducer = kafkaProducerService;
    }

    @KafkaListener(topics = {"request-slskd-song"} , groupId = "group-id",containerFactory = "songConcurrentKafkaListenerContainerFactory")
    public void consumeRequestSong(RequestSlskdSong requestSlskdSong) {
        log.info("Received request song: {}",requestSlskdSong);

        Song song = new Song(requestSlskdSong.getTitle(), requestSlskdSong.getArtist());

        LoadSong(song,requestSlskdSong.getId());

    }

    private void LoadSong(Song song, UUID id) {
        SlskdDownloadResponse slskdDownloadResponse = sl.search(song);
        try {
            SlskdSearchResponse res =  sl.getSearch(slskdDownloadResponse.id);
            song.addSlsk(res);

            sl.requestDownload(song,slskdDownloadResponse.id);
            song.filePath = res.getFilePathClean();

            sl.removeSearch(slskdDownloadResponse.id);

            kafkaProducer.send("loaded-song",UUID.randomUUID(),new LoadedSong(song,id, LoadedSong.Status.COMPLETED));

        }catch (Exception e){
            sl.removeSearch(slskdDownloadResponse.id);
            log.error("Error while loading song {}",song,e);
            kafkaProducer.send("loaded-song",UUID.randomUUID(),new LoadedSong(song,id, LoadedSong.Status.ERROR));


        }
    }
}
