package org.muzika.slskddownload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication

public class SlskdDownloadApplication {

    public static void main(String[] args) {
        SpringApplication.run(SlskdDownloadApplication.class, args);
    }

}
