package fr.istic.service.subject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.istic.service.FichierS3Service;
import fr.istic.service.StorageException;
import fr.istic.service.dto.DiagramDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Text diagrams of the Typst subjects (T25c, ADR 0004): rendered once by Kroki, then read from the shared,
 * content-addressed cache {@code diagram-cache/{kind}/{sha256}.svg}. The Typst compilation reads the cached SVG and
 * stays offline.
 */
@ApplicationScoped
public class DiagramService {

    private final Logger log = LoggerFactory.getLogger(DiagramService.class);

    @Inject
    KrokiClient krokiClient;

    @Inject
    FichierS3Service fichierS3Service;

    @Inject
    ExamSubjectService examSubjectService;

    /** Renders (or finds in the cache) a diagram. */
    public DiagramDTO render(String rawKind, String source) {
        String kind = DiagramKinds.normalizeKind(rawKind)
                .orElseThrow(() -> new SubjectException(SubjectException.Reason.BAD_PATH, "Unknown diagram kind: " + rawKind));
        if (source == null || source.isBlank()) {
            throw new KrokiException(KrokiException.Reason.INVALID_SOURCE, "Empty diagram source");
        }
        if (source.getBytes(StandardCharsets.UTF_8).length > krokiClient.getMaxSourceBytes()) {
            throw new KrokiException(KrokiException.Reason.TOO_LARGE, "Diagram source too large");
        }
        if (!examSubjectService.isStorageAvailable()) {
            throw new SubjectException(SubjectException.Reason.STORAGE_UNAVAILABLE, "No object storage for the diagram cache");
        }
        String sha = DiagramKinds.hash(kind, source);
        String key = DiagramKinds.cacheKey(kind, sha);
        if (exists(key)) {
            return new DiagramDTO(kind, sha);
        }
        byte[] svg = krokiClient.renderSvg(kind, source);
        try {
            fichierS3Service.putObject(key, svg, "image/svg+xml");
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IllegalStateException
                | StorageException e) {
            log.error("Could not store the rendered {} diagram {}", kind, sha, e);
            throw new SubjectException(SubjectException.Reason.STORAGE_UNAVAILABLE, "Could not store the diagram", e);
        }
        return new DiagramDTO(kind, sha);
    }

    /** Opens a rendered diagram; the caller closes the stream. */
    public InputStream read(String rawKind, String sha256) {
        String kind = DiagramKinds.normalizeKind(rawKind)
                .orElseThrow(() -> new SubjectException(SubjectException.Reason.NOT_FOUND, "Unknown diagram kind"));
        if (!DiagramKinds.isSha256(sha256)) {
            throw new SubjectException(SubjectException.Reason.NOT_FOUND, "Unknown diagram");
        }
        String key = DiagramKinds.cacheKey(kind, sha256);
        if (!exists(key)) {
            throw new SubjectException(SubjectException.Reason.NOT_FOUND, "Unknown diagram");
        }
        try {
            return fichierS3Service.getObject(key);
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | IllegalStateException e) {
            throw new SubjectException(SubjectException.Reason.STORAGE_UNAVAILABLE, "Could not read the diagram", e);
        }
    }

    private boolean exists(String key) {
        try {
            return fichierS3Service.isObjectExist(key);
        } catch (RuntimeException e) {
            throw new SubjectException(SubjectException.Reason.STORAGE_UNAVAILABLE, "Object storage unavailable", e);
        }
    }
}
