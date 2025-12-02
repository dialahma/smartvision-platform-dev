package net.smart.vision.video_storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import org.springframework.context.annotation.Profile;

//@Profile("!test")
@SpringBootApplication
@EnableDiscoveryClient
public class VideoStorageApplication {
    public static void main(String[] args) {
        SpringApplication.run(VideoStorageApplication.class, args);
    }
}
