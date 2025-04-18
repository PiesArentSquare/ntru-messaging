package piesarentsquare.backend.web.security;

public class Encryption {
    public static String encrypt(String plaintext) {
        return new StringBuilder(plaintext).reverse().toString();
    }

    public static String decrypt(String ciphertext) {
        return new StringBuilder(ciphertext).reverse().toString();
    }
}
