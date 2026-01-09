package org.muzika.slskddownload.lib;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.*;

public abstract class WebSearcher {

    private final String origen;
    public Map<String ,String> headers;
    protected WebSearcher(String origen) {
        this.origen = origen;
        this.headers = new HashMap<>();

    }
    protected static final ObjectMapper objectMapper = new ObjectMapper();


    public Document getDocument(String url) {
        Document dc = null;

        Connection.Response response;
        try {
            response = Jsoup.connect(origen + url).headers(headers).timeout(100000).ignoreContentType(true)
                    .header("Accept", "application/json, text/plain, */*")
                    .ignoreHttpErrors(true).execute();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (response.statusCode() ==401){
            try {
                this.login();
                return getDocument(url);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println(response.statusCode());
        try {
            return response.parse();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public Document postDocument(String url,  String data){
        Document dc = null;
        Connection.Response response = null;

        try {
            response= Jsoup.connect(origen + url).ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .method(Connection.Method.POST)
                    .headers(headers)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Sec-Fetch-Dest","empty")
                    .header("Sec-Fetch-Mode","no-cors")
                    .header("Sec-Fetch-Site","same-origin")
                    .requestBody(data)
                    .timeout(30000)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (response.statusCode() ==401){
            try {
                this.login();
                return postDocument(url, data);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println(response.statusCode());
        try {
            return response.parse();
        }catch(IOException e){
            throw new RuntimeException(e);
        }

    }

    public Document deleteDocument(String url) {
        Document dc = null;
        try {
            dc = Jsoup.connect(origen + url).ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .method(Connection.Method.DELETE)
                    .headers(headers)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Sec-Fetch-Dest","empty")
                    .header("Sec-Fetch-Mode","no-cors")
                    .header("Sec-Fetch-Site","same-origin")
                    .timeout(30000)
                    .execute().parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return dc;
    }

    public void login() throws JsonProcessingException {

    }

    public void waitFor(waitFunctionPointer fun, int trys, long delay,String url, String[] con) throws JsonProcessingException {

        for (int i = 0; i < trys; i++) {

            String doc =  Objects.requireNonNull(this
                    .getDocument(url)
                    .select("body")
                    .first()).html();

            if (fun.waitFunction(doc,con)){
                return;
            }
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("Timed out waiting for");

    }

    public interface waitFunctionPointer {
        boolean waitFunction(String doc,String[] con) throws JsonProcessingException;

    }
}

