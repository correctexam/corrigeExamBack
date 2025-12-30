package fr.istic.service.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
*  DTO to emulate /management/info response
*/
@RegisterForReflection
public class ManagementInfoDTO {

    public List<String> activeProfiles = new ArrayList<>();

    @JsonProperty("display-ribbon-on-profiles")
    public String displayRibbonOnProfiles;

}
