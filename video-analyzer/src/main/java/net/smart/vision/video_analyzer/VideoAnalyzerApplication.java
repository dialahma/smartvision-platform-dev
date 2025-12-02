package net.smart.vision.video_analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Profile;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

//@Profile("!test")
@EnableDiscoveryClient
@SpringBootApplication
public class VideoAnalyzerApplication {
    public static void main(String[] args) {
        SpringApplication.run(VideoAnalyzerApplication.class, args);
    }
}
