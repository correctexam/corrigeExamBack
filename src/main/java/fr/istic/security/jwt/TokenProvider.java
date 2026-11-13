package fr.istic.security.jwt;

import fr.istic.config.JHipsterProperties;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import fr.istic.security.AuthoritiesConstants;
import java.security.Key;
import java.time.Instant;
import java.util.List;
import java.util.Date;
import java.util.UUID;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class TokenProvider {
    private final Logger log = LoggerFactory.getLogger(TokenProvider.class);

    private static final String AUTHORITIES_KEY = "auth"; // Claim JHiptser front-end uses
    public static final String GROUPS_KEY = "groups"; // Default claim for MP-JWT

    private final Key key;

    private final String issuer;

    private final long tokenValidityInMilliseconds;

    private final long tokenValidityInMillisecondsForRememberMe;

    @Inject
    public TokenProvider(JHipsterProperties jHipsterProperties)
        throws Exception {
        this.key = PemKeys.privateKey(PemKeys.read(jHipsterProperties.security().authentication().jwt().privateKey().location()));
        this.issuer = jHipsterProperties.security().authentication().jwt().issuer();
        this.tokenValidityInMilliseconds = jHipsterProperties.security().authentication().jwt().tokenValidityInSeconds() * 1000;
        this.tokenValidityInMillisecondsForRememberMe =
            jHipsterProperties.security().authentication().jwt().tokenValidityInSecondsForRememberMe() * 1000;
    }

    @PostConstruct
    void init() throws Exception {}

    public String createToken(QuarkusSecurityIdentity identity, boolean rememberMe) {
        String authorities = String.join(", ", identity.getRoles());
        long now = (new Date()).getTime();
        Date validity;
        if (rememberMe) {
            validity = new Date(now + tokenValidityInMillisecondsForRememberMe);
        } else {
            validity = new Date(now + tokenValidityInMilliseconds);
        }

        JwtClaims claims = new JwtClaims();
        claims.setSubject(identity.getPrincipal().getName());
        claims.setClaim(AUTHORITIES_KEY, authorities);
        claims.setClaim(GROUPS_KEY, identity.getRoles());
        claims.setIssuedAt(NumericDate.fromMilliseconds(now));
        claims.setIssuer(this.issuer);
        claims.setExpirationTime(NumericDate.fromMilliseconds(validity.getTime()));

        return sign(claims);
    }

    /**
     * Session of a student opened by the link sent to their e-mail address (ADR 0003): subject = the e-mail address,
     * single role ROLE_STUDENT (only /api/student/me accepts it), no refresh.
     */
    public String createStudentToken(String email, Instant expiresAt) {
        JwtClaims claims = new JwtClaims();
        claims.setSubject(email);
        claims.setClaim(AUTHORITIES_KEY, AuthoritiesConstants.STUDENT);
        claims.setClaim(GROUPS_KEY, List.of(AuthoritiesConstants.STUDENT));
        claims.setIssuedAtToNow();
        claims.setIssuer(this.issuer);
        claims.setExpirationTime(NumericDate.fromMilliseconds(expiresAt.toEpochMilli()));
        return sign(claims);
    }

    private String sign(JwtClaims claims) {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(key);
        jws.setKeyIdHeaderValue(UUID.randomUUID().toString());
        jws.setHeader("typ", "JWT");
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        try {
            return jws.getCompactSerialization();
        } catch (JoseException e) {
            throw new RuntimeException(e);
        }
    }
}
