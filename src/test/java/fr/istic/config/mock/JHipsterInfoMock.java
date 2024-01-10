package fr.istic.config.mock;

import fr.istic.config.JHipsterInfo;
import io.quarkus.test.Mock;

import jakarta.enterprise.context.ApplicationScoped;

@Mock
@ApplicationScoped
public class JHipsterInfoMock implements JHipsterInfo {

    public static Boolean enable;

    @Override
    public Boolean isEnable() {
        return enable;
    }
}
