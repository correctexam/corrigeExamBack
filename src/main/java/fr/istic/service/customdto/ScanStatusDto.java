package fr.istic.service.customdto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ScanStatusDto{
    @JsonProperty("exam_id")
    public int examId;
    @JsonProperty("page")
    public int page;
    @JsonProperty("status")
    public String status;
    @JsonProperty("progress")
    public int progress;
    @JsonProperty("details")
    public String details;
}
