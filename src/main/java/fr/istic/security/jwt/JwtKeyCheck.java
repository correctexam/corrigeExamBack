package fr.istic.security.jwt;

import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

/**
 * Checks the JWT key pair at startup.
 * <p>
 * The pair under {@code jwt/dev-only/} is public (it is in the source repository): anybody holding it can sign a
 * token for any user, admins included. The application refuses to start with it unless
 * {@code correctexam.jwt.allow-dev-key=true} (dev, test and standalone profiles). It also refuses a public key that
 * does not match the private key, which would reject every token.
 */
@ApplicationScoped
public class JwtKeyCheck {

    static final String DEV_PUBLIC_KEY = "classpath:jwt/dev-only/publicKey.pem";

    private final Logger log = LoggerFactory.getLogger(JwtKeyCheck.class);

    @ConfigProperty(name = "jhipster.security.authentication.jwt.private-key.location")
    String privateKeyLocation;

    @ConfigProperty(name = "mp.jwt.verify.publickey.location")
    String publicKeyLocation;

    @ConfigProperty(name = "correctexam.jwt.allow-dev-key", defaultValue = "false")
    boolean allowDevKey;

    void onStart(@Observes StartupEvent event) throws Exception {
        RSAPrivateCrtKey privateKey = PemKeys.privateKey(PemKeys.read(privateKeyLocation));
        RSAPublicKey publicKey = PemKeys.publicKey(PemKeys.read(publicKeyLocation));
        RSAPublicKey devPublicKey = PemKeys.publicKey(PemKeys.read(DEV_PUBLIC_KEY));
        if (check(privateKey, publicKey, devPublicKey, allowDevKey) == Result.DEV_KEY_ALLOWED) {
            log.warn(
                "JWT signed with the public development key (jwt/dev-only): never expose this instance on a network. " +
                "See docs/security/jwt-keys.md"
            );
        }
    }

    enum Result {
        OWN_KEY,
        DEV_KEY_ALLOWED,
    }

    static Result check(RSAPrivateCrtKey privateKey, RSAPublicKey publicKey, RSAPublicKey devPublicKey, boolean allowDevKey) {
        if (!PemKeys.isPair(privateKey, publicKey)) {
            throw new IllegalStateException(
                "The JWT public key (mp.jwt.verify.publickey.location) does not match the private key " +
                "(jhipster.security.authentication.jwt.private-key.location)"
            );
        }
        if (PemKeys.isPair(privateKey, devPublicKey)) {
            if (!allowDevKey) {
                throw new IllegalStateException(
                    "Refusing to start with the public development JWT key: set JWT_PRIVATE_KEY_LOCATION and " +
                    "JWT_PUBLIC_KEY_LOCATION to a key pair of your own (see docs/security/jwt-keys.md)"
                );
            }
            return Result.DEV_KEY_ALLOWED;
        }
        return Result.OWN_KEY;
    }
}
