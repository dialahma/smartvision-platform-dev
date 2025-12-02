package net.smart.vision.video_storage.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document("detection_metadata")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DetectionMetadata {
  @Id
  private String id;
  private String source;
  private String label;
  private double confidence;
  private Instant ts;
  private String cameraId;
  private String location;
}
