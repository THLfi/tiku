package fi.thl.pivot.web.tools;

import java.security.SecureRandom;
import java.util.Base64;

public class NonceGenerator {

    private static String nonce;
    private static final String NONCE_HEADER = "nonce-";

    public static String generateNonceHeader() {
        generateNonce();
        return NONCE_HEADER + nonce;
    }

    private static void generateNonce() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[16];
        random.nextBytes(bytes);
        nonce = Base64.getEncoder().encodeToString(bytes);
    }

    public static String getNonce() {
        return nonce;
    }
}
