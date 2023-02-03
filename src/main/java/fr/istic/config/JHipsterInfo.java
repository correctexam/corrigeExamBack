package fr.istic.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;



@ConfigMapping(prefix = "jhipster.info")
public interface JHipsterInfo {

    @WithName("swagger.enable")
    @WithDefault("true")
    Boolean isEnable();

}
