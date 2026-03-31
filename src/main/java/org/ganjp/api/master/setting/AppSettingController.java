package org.ganjp.api.master.setting;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.api.core.model.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/app-settings")
@RequiredArgsConstructor
@Slf4j
public class AppSettingController {

    private final AppSettingService appSettingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppSettingDto>>> getAllAppSettings() {
        List<AppSettingDto> settings = appSettingService.getAllAppSettings();
        return ResponseEntity.ok(ApiResponse.success(settings, "Public app settings retrieved successfully"));
    }
}
