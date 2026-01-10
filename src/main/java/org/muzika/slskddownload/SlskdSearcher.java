package org.muzika.slskddownload;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.muzika.slskddownload.lib.*;
import org.muzika.slskddownload.services.SlskdSearchIdController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class SlskdSearcher extends WebSearcher {

    private SlskdSearchIdController searchIdController;
    private String slskdApiUrl;
    
    public SlskdSearcher(SlskdSearchIdController searchIdController, 
                         @Value("${slskd.api.url:http://localhost:5030}") String slskdApiUrl) {
       // Ensure URL ends with /
       super(slskdApiUrl.endsWith("/") ? slskdApiUrl : slskdApiUrl + "/");
       
       String baseUrl = slskdApiUrl.endsWith("/") ? slskdApiUrl : slskdApiUrl + "/";
       this.slskdApiUrl = baseUrl;
       this.headers = new LinkedHashMap<>();
       headers.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:128.0) Gecko/20100101 Firefox/128.0");
       headers.put("Accept-Language","en-US,en;q=0.5");
       headers.put("Origin", baseUrl);
       this.searchIdController = searchIdController;
       
       log.info("SlskdSearcher initialized with API URL: {}", baseUrl);
   }
    private String aoth2;

    @Override
    public void login() throws JsonProcessingException {
        LoginRequest request = new LoginRequest();
        request.username = "slskd";
        request.password = "slskd";

        String jsonBody = objectMapper.writeValueAsString(request);
        Document doc = this.postDocument("api/v0/session", jsonBody);
        String jsonResponse = Objects.requireNonNull(doc.select("body").first()).html();
        SlskdLoginResponse slskdLoginResponse = objectMapper.readValue(jsonResponse,SlskdLoginResponse.class);
        this.aoth2 = slskdLoginResponse.token;
        this.headers.put("Authorization","Bearer "+this.aoth2);

   }

   public SlskdDownloadResponse search(Song song)  {
        SlskdSearchRequest request = new SlskdSearchRequest();
        request.id = searchIdController.ReserveId();
        request.searchText = song.songName+" "+song.artistName;

        String jsonBody = null;
        try {
            jsonBody = objectMapper.writeValueAsString(request);
            String response = Objects.requireNonNull(this.postDocument("api/v0/searches", jsonBody).select("body").first()).html();
            return objectMapper.readValue(response,SlskdDownloadResponse.class);
       } catch (JsonProcessingException e) {
           throw new RuntimeException(e);
       }


   }


   public SlskdSearchResponse getSearch(String id) throws InterruptedException {
       waitFunctionPointer pointer1 = this::waitForSearch;
       try {
           waitFor(pointer1,10,5000,"api/v0/searches/" + id ,new String[] {id});
       } catch (JsonProcessingException e) {
           throw new RuntimeException(e);
       }

       try {
           SlskdSearchResponse[]response =objectMapper.readValue(Objects.requireNonNull(this
                   .getDocument("api/v0/searches/" + id + "/responses")
                   .select("body")
                   .first()).html(),SlskdSearchResponse[].class);
           response = Arrays.stream(response).filter(a -> a.fileCount > 0 && a.queueLength==0).toArray(SlskdSearchResponse[]::new);
           Arrays.sort(response);
           if(response.length==0){
               log.error("not-found");
           }
           return response[response.length-1];

       } catch (JsonProcessingException e) {
           throw new RuntimeException(e);
       }
   }
   public boolean waitForSearch(String doc,String[] id) throws JsonProcessingException {
        SlskdDownloadResponse res =  objectMapper.readValue(Objects.requireNonNull(this.getDocument("api/v0/searches/" + id[0] ).select("body").first()).html(),SlskdDownloadResponse.class);
       return res.isComplete;
   }

     public void requestDownload(Song song, String id) {
       SlskdDownloadRequest[] request = new SlskdDownloadRequest[] {new SlskdDownloadRequest()};
       request[0].filename = URLDecoder.decode(song.fileName, StandardCharsets.UTF_8);
       request[0].size = song.size;

        String jsonBody = null;
        try {
            jsonBody = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        Document doc = this.postDocument("api/v0/transfers/downloads/" + URLEncoder.encode(song.username,StandardCharsets.UTF_8),jsonBody);


        waitFunctionPointer pointer1 = this::waitForDownload;
         try {
             waitFor(pointer1,20,5000,"api/v0/transfers/downloads" ,new String[] {song.username,song.fileName});
         } catch (JsonProcessingException e) {
             throw new RuntimeException(e);
         }

     }

    private boolean waitForDownload(String doc,String[] data) throws JsonProcessingException {
        SlskdDownloads[] downloads = objectMapper
                    .readValue(doc, SlskdDownloads[].class);
        AtomicBoolean isFinished = new AtomicBoolean(false);
        Arrays.stream(downloads)
                    .filter(d -> d.username.equals(data[0]))
                    .findFirst()
                    .ifPresent(download -> {
                        isFinished.set(download.isFinished(data[1]));
                    });

        return isFinished.get();
    }
    public void removeSearch(String id){
        this.deleteDocument("api/v0/searches/" + id);
        searchIdController.UnReserveId(id);
    }



}
