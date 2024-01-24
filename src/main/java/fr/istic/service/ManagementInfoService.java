package fr.istic.service;

import io.quarkus.runtime.configuration.ProfileManager;
import fr.istic.config.JHipsterInfo;
import fr.istic.service.dto.ManagementInfoDTO;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
* Provides information for management/info resource
*/
@ApplicationScoped
public class ManagementInfoService {

    private final JHipsterInfo JHipsterInfo;

    @Inject
    public ManagementInfoService(JHipsterInfo JHipsterInfo) {
        this.JHipsterInfo = JHipsterInfo;
    }

    public ManagementInfoDTO getManagementInfo(){
        var info = new ManagementInfoDTO();
        if(JHipsterInfo.isEnable()){
            info.activeProfiles.add("swagger");
            info.activeProfiles.add("api-docs");

        }
        info.activeProfiles.add(ProfileManager.getActiveProfile());
        info.displayRibbonOnProfiles = ProfileManager.getActiveProfile();
        return info;
    }
}

