package piesarentsquare.ntru;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.Arrays;

public class EncryptionDecryptionTest {

    @Test
    void encryptionDecryption() {
        String msg = "hello there \uD83D\uDE00";

        NTRU ntru = NTRU.create(107, 14, 12, 5, 3, 64);
        ntru.setForeignKey(ntru.getPublicKey());
        String ciphertext = ntru.encrypt(msg);
        String plaintext = ntru.decrypt(ciphertext);
        Assertions.assertEquals(msg, plaintext);
    }

    @Test
    void encryptionDecryptionMany() {
        for (int i = 0; i < 1000; i++) {
            encryptionDecryption();
        }
    }

    void aliceBob(String msg) {
        final int N = 107, df = 14, dg = 12, dr = 5, p = 3, q = 64;
        NTRU alice, bob;
        alice = NTRU.create(N, df, dg, dr, p, q);
        bob = NTRU.create(N, df, dg, dr, p, q);
        alice.setForeignKey(bob.getPublicKey());
        bob.setForeignKey(alice.getPublicKey());
        try {
            String ciphertext = bob.encrypt(msg);
            var plaintext = alice.decrypt(ciphertext);
            if (!msg.equals(plaintext)) {
                System.out.println(Arrays.equals(bob.enc, alice.dec));
                System.out.println(Arrays.toString(bob.enc));
                System.out.println(Arrays.toString(alice.dec));
            }
            Assertions.assertEquals(msg, plaintext);
        } catch (NegativeArraySizeException e) {
            System.out.println(Arrays.equals(bob.enc, alice.dec));
            System.out.println(Arrays.toString(bob.enc));
            System.out.println(Arrays.toString(alice.dec));
            Assertions.fail();
        }
    }

    @Test
    void aliceBob() {
        aliceBob("hello there \uD83D\uDE00");
    }

    @Test
    void aliceBobMany() {
        for (int i = 0; i < 1000; i++) {
            aliceBob();
        }
    }

    @Test
    void aliceBobLargeMessage() {
        aliceBob("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
    }

    @Test
    void aliceBobLargeMessageMany() {
        for (int i = 0; i < 1000; i++) {
            aliceBob("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
        }
    }

}
