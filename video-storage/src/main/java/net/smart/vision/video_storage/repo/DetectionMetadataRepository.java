package net.smart.vision.video_storage.repo;

import net.smart.vision.video_storage.model.DetectionMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.List;

public interface DetectionMetadataRepository extends MongoRepository<DetectionMetadata, String> {
  Page<DetectionMetadata> findByCameraId(String cameraId, Pageable pageable);
  List<DetectionMetadata> findByTsBetween(Instant start, Instant end);
  Long countByLabel(String label);
}
