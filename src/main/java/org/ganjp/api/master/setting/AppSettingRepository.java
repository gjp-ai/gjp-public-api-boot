package org.ganjp.api.master.setting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppSettingRepository extends JpaRepository<AppSetting, String> {

    List<AppSetting> findByIsPublicTrueOrderByNameAscLangAsc();
}
