package org.ganjp.api.cms.logo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.api.cms.util.CmsUtil;
import org.ganjp.api.core.model.PaginatedResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LogoService {
    private final LogoRepository logoRepository;
    private final LogoUploadProperties uploadProperties;

    @Value("${logo.base-url:}")
    private String logoBaseUrl;

    public PaginatedResponse<LogoResponse> getLogos(String name, Logo.Language lang, String tags, Boolean isActive, int page, int size, String sort, String direction) {
        Pageable pageable = CmsUtil.buildPageable(page, size, sort, direction);
        Page<Logo> pageResult = logoRepository.searchLogos(name, lang, tags, isActive, pageable);

        List<LogoResponse> publicList = pageResult.getContent().stream().map(r -> {
            String built = CmsUtil.joinBaseAndPath(logoBaseUrl, r.getFilename());
            return LogoResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .tags(r.getTags())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .url(built)
                .thumbnailUrl(built)
                .build();
        }).toList();

        return PaginatedResponse.of(publicList, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
    }

    public LogoResponse getLogoById(String id) {
        return logoRepository.findById(id)
            .map(r -> {
                String built = CmsUtil.joinBaseAndPath(logoBaseUrl, r.getFilename());
                return LogoResponse.builder()
                    .id(r.getId())
                    .name(r.getName())
                    .tags(r.getTags())
                    .lang(r.getLang())
                    .displayOrder(r.getDisplayOrder())
                    .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                    .url(built)
                    .thumbnailUrl(built)
                    .build();
            })
            .orElse(null);
    }

    public File getLogoFile(String filename) throws IOException {
        logoRepository.findByFilenameAndIsActiveTrue(filename)
            .orElseThrow(() -> new IllegalArgumentException("Logo not found or not active: " + filename));

        Path uploadDir = Paths.get(uploadProperties.getDirectory());
        Path fullPath = uploadDir.resolve(filename);

        if (!Files.exists(fullPath)) {
            throw new IOException("Logo file not found: " + filename);
        }

        File file = fullPath.toFile();
        if (!file.isFile() || !file.canRead()) {
            throw new IOException("Cannot read logo file: " + filename);
        }

        log.debug("Retrieved logo file: {}", fullPath);
        return file;
    }
}
