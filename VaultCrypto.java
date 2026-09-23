package com.kaua.cofrebtc;

import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

final class VaultCrypto {
    private static final int SALT_BYTES = 16;
    private static final int IV_BYTES = 12;
    private static final int KEY_BITS = 256;
    private static final int ITERATIONS = 600_000;
    private static final SecureRandom RNG = new SecureRandom();

    static String encrypt(String json, char[] password) throws Exception {
        byte[] salt = new byte[SALT_BYTES]; RNG.nextBytes(salt);
        byte[] iv = new byte[IV_BYTES]; RNG.nextBytes(iv);
        SecretKey key = derive(password, salt);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] ct = c.doFinal(json.getBytes(StandardCharsets.UTF_8));
        // Entire vault payload is encoded as one opaque line; no plaintext JSON metadata is stored.
        return "CBTC1." + b64(salt) + "." + b64(iv) + "." + b64(ct);
    }

    static String decrypt(String blob, char[] password) throws Exception {
        String[] p = blob.trim().split("\\.", -1);
        if (p.length != 4 || !"CBTC1".equals(p[0])) throw new SecurityException("Formato de cofre inválido");
        byte[] salt = Base64.decode(p[1], Base64.NO_WRAP);
        byte[] iv = Base64.decode(p[2], Base64.NO_WRAP);
        byte[] ct = Base64.decode(p[3], Base64.NO_WRAP);
        SecretKey key = derive(password, salt);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
        return new String(c.doFinal(ct), StandardCharsets.UTF_8);
    }

    private static SecretKey derive(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_BITS);
        try {
            SecretKeyFactory f;
            try { f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256"); }
            catch (Exception e) { f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1"); }
            return new SecretKeySpec(f.generateSecret(spec).getEncoded(), "AES");
        } finally { spec.clearPassword(); }
    }

    private static String b64(byte[] b) { return Base64.encodeToString(b, Base64.NO_WRAP); }
    static void wipe(char[] a) { if (a != null) Arrays.fill(a, '\0'); }
    static void wipe(byte[] a) { if (a != null) Arrays.fill(a, (byte)0); }
}
