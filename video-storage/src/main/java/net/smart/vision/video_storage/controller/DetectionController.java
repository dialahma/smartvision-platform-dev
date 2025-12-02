package net.smart.vision.video_storage.api;

import net.smart.vision.video_storage.model.DetectionMetadata;
import net.smart.vision.video_storage.repo.DetectionMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/detections")
@RequiredArgsConstructor
public class DetectionController {
  private final DetectionMetadataRepository repo;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public DetectionMetadata save(@RequestBody DetectionMetadata m){ return repo.save(m); }

  @GetMapping("/camera/{cameraId}")
  public Page<DetectionMetadata> getByCamera(@PathVariable String cameraId, Pageable pageable) {
    return repo.findByCameraId(cameraId, pageable);
  }

  @GetMapping("/stats/label/{label}")
  public Long countByLabel(@PathVariable String label) {
    return repo.countByLabel(label);
  }
}
