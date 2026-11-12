package fr.istic.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Plain unit tests (no Quarkus boot) of the JWT key loading and of the startup check. */
class JwtKeyCheckTest {

    private static final String DEV_PRIVATE_KEY = "jwt/dev-only/privateKey.pem";

    @Test
    void devKeyPairIsReadFromTheClasspathInEveryNotation() throws Exception {
        RSAPublicKey dev = PemKeys.publicKey(PemKeys.read(JwtKeyCheck.DEV_PUBLIC_KEY));
        for (String location : new String[] { DEV_PRIVATE_KEY, "/" + DEV_PRIVATE_KEY, "classpath:" + DEV_PRIVATE_KEY }) {
            assertTrue(PemKeys.isPair(PemKeys.privateKey(PemKeys.read(location)), dev), location);
        }
    }

    @Test
    void ownKeyPairIsReadFromFilesAndAccepted(@TempDir Path dir) throws Exception {
        KeyPair own = newKeyPair();
        Path privateFile = write(dir.resolve("privateKey.pem"), "PRIVATE KEY", own.getPrivate().getEncoded());
        Path publicFile = write(dir.resolve("publicKey.pem"), "PUBLIC KEY", own.getPublic().getEncoded());

        RSAPrivateCrtKey privateKey = PemKeys.privateKey(PemKeys.read("file:" + privateFile));
        RSAPublicKey publicKey = PemKeys.publicKey(PemKeys.read(publicFile.toString()));

        assertEquals(JwtKeyCheck.Result.OWN_KEY, JwtKeyCheck.check(privateKey, publicKey, devPublicKey(), false));
    }

    @Test
    void devKeyIsRefusedUnlessAllowed() throws Exception {
        RSAPrivateCrtKey devPrivate = PemKeys.privateKey(PemKeys.read(DEV_PRIVATE_KEY));
        RSAPublicKey devPublic = devPublicKey();

        IllegalStateException refused = assertThrows(IllegalStateException.class, () -> JwtKeyCheck.check(devPrivate, devPublic, devPublic, false));
        assertTrue(refused.getMessage().contains("JWT_PRIVATE_KEY_LOCATION"));
        assertEquals(JwtKeyCheck.Result.DEV_KEY_ALLOWED, JwtKeyCheck.check(devPrivate, devPublic, devPublic, true));
    }

    @Test
    void mismatchingPublicKeyIsRefused() throws Exception {
        KeyPair own = newKeyPair();
        RSAPrivateCrtKey ownPrivate = (RSAPrivateCrtKey) own.getPrivate();

        assertThrows(IllegalStateException.class, () -> JwtKeyCheck.check(ownPrivate, devPublicKey(), devPublicKey(), true));
    }

    @Test
    void pkcs1KeyGetsAnExplicitMessage() {
        var error = assertThrows(Exception.class, () -> PemKeys.privateKey("-----BEGIN RSA PRIVATE KEY-----\nAAAA\n-----END RSA PRIVATE KEY-----"));
        assertTrue(error.getMessage().contains("openssl pkcs8"));
    }

    @Test
    void missingKeyIsReported() {
        var error = assertThrows(Exception.class, () -> PemKeys.read("jwt/missing.pem"));
        assertTrue(error.getMessage().contains("jwt/missing.pem"));
    }

    private static RSAPublicKey devPublicKey() throws Exception {
        return PemKeys.publicKey(PemKeys.read(JwtKeyCheck.DEV_PUBLIC_KEY));
    }

    private static KeyPair newKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static Path write(Path file, String type, byte[] der) throws Exception {
        String pem = "-----BEGIN " + type + "-----\n" + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(der) +
            "\n-----END " + type + "-----\n";
        return Files.writeString(file, pem);
    }
}
