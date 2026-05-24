package org.ganjp.api.cms.logo;

import lombok.RequiredArgsConstructor;
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
@Transactional(readOnly = true)
public class LogoService {
    private final LogoRepository logoRepository;
    private final LogoUploadProperties uploadProperties;

    @Value("${logo.base-url:}")
    private String logoBaseUrl;

    public PaginatedResponse<LogoResponse> getLogos(String name, Logo.Language lang, String tags, Boolean isActive, int page, int size, String sort, String direction) {
        Pageable pageable = CmsUtil.buildPageable(page, size, sort, direction);
        Page<Logo> pageResult = logoRepository.searchLogos(name, lang, tags, isActive, pageable);
        List<LogoResponse> list = pageResult.getContent().stream().map(this::mapToResponse).toList();
        return PaginatedResponse.of(list, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
    }

    public List<LogoResponse> getAllLogos(String name, Logo.Language lang, String tags,
            Boolean isActive, String updatedAfter) {
        return logoRepository.findAllLogos(name, lang, tags, isActive, CmsUtil.parseLocalDateTime(updatedAfter)).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public LogoResponse getLogoById(String id) {
        return logoRepository.findById(id)
                .map(this::mapToResponse)
                .orElse(null);
    }

    public File getLogoFile(String filename) throws IOException {
        logoRepository.findByFilenameAndIsActiveTrue(filename)
                .orElseThrow(() -> new IllegalArgumentException("Logo not found or not active: " + filename));

        Path fullPath = Paths.get(uploadProperties.getDirectory()).resolve(filename);
        if (!Files.exists(fullPath)) {
            throw new IOException("Logo file not found: " + filename);
        }
        File file = fullPath.toFile();
        if (!file.isFile() || !file.canRead()) {
            throw new IOException("Cannot read logo file: " + filename);
        }
        return file;
    }

    private LogoResponse mapToResponse(Logo r) {
        String url = CmsUtil.joinBaseAndPath(logoBaseUrl, r.getFilename());
        return LogoResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .tags(r.getTags())
                .lang(r.getLang())
                .channel(r.getChannel())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .url(url)
                .thumbnailUrl(url)
                .build();
    }
}
