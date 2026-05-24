package org.ganjp.api.cms.website;

import lombok.RequiredArgsConstructor;
import org.ganjp.api.cms.util.CmsUtil;
import org.ganjp.api.core.model.PaginatedResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WebsiteService {
    private final WebsiteRepository websiteRepository;

    @Value("${logo.base-url:}")
    private String logoBaseUrl;

    public PaginatedResponse<WebsiteResponse> getWebsites(String name, Website.Language lang, String tags, Boolean isActive, int page, int size, String sort, String direction) {
        Pageable pageable = CmsUtil.buildPageable(page, size, sort, direction);
        Page<Website> pageResult = websiteRepository.searchWebsites(name, lang, tags, isActive, pageable);
        List<WebsiteResponse> list = pageResult.getContent().stream().map(this::mapToResponse).toList();
        return PaginatedResponse.of(list, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
    }

    public List<WebsiteResponse> getAllWebsites(String name, Website.Language lang, String tags,
            Boolean isActive, String updatedAfter) {
        return websiteRepository.findAllWebsites(name, lang, tags, isActive, CmsUtil.parseLocalDateTime(updatedAfter)).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public WebsiteResponse getWebsiteById(String id) {
        return websiteRepository.findById(id)
                .map(this::mapToResponse)
                .orElse(null);
    }

    private WebsiteResponse mapToResponse(Website r) {
        String logoUrl = r.getLogoUrl();
        if (logoUrl != null && !logoUrl.isBlank() && !logoUrl.startsWith("http")) {
            logoUrl = CmsUtil.joinBaseAndPath(logoBaseUrl, logoUrl);
        }
        return WebsiteResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .url(r.getUrl())
                .logoUrl(logoUrl)
                .tags(r.getTags())
                .lang(r.getLang())
                .channel(r.getChannel())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .build();
    }
}
