package fr.istic.service.customdto;


import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class ScanRequestDto{

    @JsonProperty("pages_to_manage")
    public String pagesToManage;
    @JsonProperty("exam_id")
    public long examID;
    @JsonProperty("template_id")
    public long templateID;
    @JsonProperty("scan_id")
    public long scanID;
    @JsonProperty("algo")
    public Long algo;
    @JsonProperty("heightresolution")
    public Long heightresolution;
    @JsonProperty("corner_square_size")
    public Long corner_square_size;
    @JsonProperty("min_radius")
    public Float min_radius;

}
