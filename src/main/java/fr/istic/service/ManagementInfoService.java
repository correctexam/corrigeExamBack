package fr.istic.service;


import fr.istic.config.JHipsterInfo;
import fr.istic.service.dto.ManagementInfoDTO;
import io.quarkus.runtime.LaunchMode;
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
//         List<String> profiles= ConfigUtils.getProfiles();
        info.activeProfiles.add(LaunchMode.current().getProfileKey());
        info.displayRibbonOnProfiles = LaunchMode.current().getProfileKey();
        return info;
    }
}

