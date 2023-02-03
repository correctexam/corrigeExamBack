package fr.istic.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "jhipster")
public interface JHipsterProperties {
    public Security security();
    public Mail mail();

    public interface Security {
        public Authentication authentication();

        public interface Authentication {
            public Jwt jwt();

            public interface Jwt {
                public String issuer();
                public long tokenValidityInSeconds();
                public long tokenValidityInSecondsForRememberMe();
                public PrivateKey privateKey();

                public interface PrivateKey {
                    public String location();
                }
            }
        }
    }

    public interface Mail {
        public String baseUrl();
    }
}
