package net.smart.vision.video_core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Profile;

//@Profile("!test")
@EnableDiscoveryClient
@SpringBootApplication
public class VideoCoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(VideoCoreApplication.class, args);
    }
}
