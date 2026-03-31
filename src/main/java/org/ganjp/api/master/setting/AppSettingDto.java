package org.ganjp.api.master.setting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppSettingDto {

    private String name;
    private String value;
    private String lang;

    public static AppSettingDto fromEntity(AppSetting setting) {
        return AppSettingDto.builder()
                .name(setting.getName())
                .value(setting.getValue())
                .lang(setting.getLang().name())
                .build();
    }
}
