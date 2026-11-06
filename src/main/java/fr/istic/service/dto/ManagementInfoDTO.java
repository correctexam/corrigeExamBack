package fr.istic.service.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
/**
*  DTO to emulate /management/info response
*/
@RegisterForReflection
@Schema(description = "Emulates the /management/info response content.")
public class ManagementInfoDTO {

    @Schema(description = "List of active application profiles.")
    public List<String> activeProfiles = new ArrayList<>();

    @JsonProperty("display-ribbon-on-profiles")
    @Schema(description = "Profiles on which the display ribbon is enabled.")
    public String displayRibbonOnProfiles;

}
