package fr.istic.security.jwt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Reads the RSA key pair used to sign (private key) and verify (public key) the JWT.
 * <p>
 * A location is either {@code file:/absolute/path.pem}, an existing file path, or a classpath resource
 * ({@code classpath:jwt/key.pem}, {@code /jwt/key.pem} or {@code jwt/key.pem}).
 */
public final class PemKeys {

    private PemKeys() {}

    public static String read(String location) throws IOException {
        if (location == null || location.isBlank()) {
            throw new IOException("No key location configured");
        }
        if (location.startsWith("file:")) {
            return Files.readString(Path.of(location.substring("file:".length())), StandardCharsets.US_ASCII);
        }
        if (!location.startsWith("classpath:")) {
            Path path = Path.of(location);
            if (path.isAbsolute() && Files.isRegularFile(path)) {
                return Files.readString(path, StandardCharsets.US_ASCII);
            }
        }
        String resource = location.replaceFirst("^classpath:", "").replaceFirst("^/+", "");
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = PemKeys.class.getClassLoader();
        }
        try (InputStream in = loader.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Key not found: " + location);
            }
            return new String(in.readAllBytes(), StandardCharsets.US_ASCII);
        }
    }

    /** Decodes a PKCS#8 PEM ({@code -----BEGIN PRIVATE KEY-----}), as written by {@code openssl genpkey}. */
    public static RSAPrivateCrtKey privateKey(String pem) throws GeneralSecurityException {
        if (pem.contains("BEGIN RSA PRIVATE KEY")) {
            throw new GeneralSecurityException(
                "PKCS#1 key: convert it with 'openssl pkcs8 -topk8 -nocrypt -in key.pem -out privateKey.pem'"
            );
        }
        var spec = new PKCS8EncodedKeySpec(der(pem));
        return (RSAPrivateCrtKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    /** Decodes an X.509 PEM ({@code -----BEGIN PUBLIC KEY-----}). */
    public static RSAPublicKey publicKey(String pem) throws GeneralSecurityException {
        var spec = new X509EncodedKeySpec(der(pem));
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    /** True when {@code publicKey} is the public half of {@code privateKey}. */
    public static boolean isPair(RSAPrivateCrtKey privateKey, RSAPublicKey publicKey) {
        return (
            privateKey.getModulus().equals(publicKey.getModulus()) &&
            privateKey.getPublicExponent().equals(publicKey.getPublicExponent())
        );
    }

    private static byte[] der(String pem) {
        String base64 = pem.replaceAll("-----(BEGIN|END) [A-Z ]+-----", "").replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }
}
