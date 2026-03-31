package org.ganjp.api.cms.website;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Transactional(readOnly = true)
public class WebsiteService {
    private final WebsiteRepository websiteRepository;

    @Value("${logo.base-url:}")
    private String logoBaseUrl;

    public PaginatedResponse<WebsiteResponse> getWebsites(String name, Website.Language lang, String tags, Boolean isActive, int page, int size, String sort, String direction) {
        Pageable pageable = CmsUtil.buildPageable(page, size, sort, direction);
        Page<Website> pageResult = websiteRepository.searchWebsites(name, lang, tags, isActive, pageable);

        List<WebsiteResponse> publicList = pageResult.getContent().stream().map(this::mapToResponse).toList();

        return PaginatedResponse.of(publicList, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
    }

    public WebsiteResponse getWebsiteById(String id) {
        return websiteRepository.findById(id)
                .map(this::mapToResponse)
                .orElse(null);
    }

    private WebsiteResponse mapToResponse(Website r) {
        String logoUrl = r.getLogoUrl();
        if (logoUrl != null && !logoUrl.isBlank() && !logoUrl.startsWith("http") && logoBaseUrl != null && !logoBaseUrl.isBlank()) {
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
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .build();
    }
}
